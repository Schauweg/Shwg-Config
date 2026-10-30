// Autotest tasks (group "autotest"), applied from stonecutter.gradle.kts.
// Always add --continue, so one failing version doesn't stop the rest:
//
//     ./gradlew autotestAll --continue           every version and loader: checks + screenshots
//     ./gradlew autotestFabric --continue        only Fabric projects (same for autotestNeoforge / autotestForge)
//     ./gradlew autotestActive                   only the active Stonecutter version - quick check while developing
//     ./gradlew autotestChecks --continue        every version, checks only (no screenshots, fast)
//     ./gradlew autotestScreenshots --continue   every version, screenshots only
//
// Optional properties, combine freely with any task:
//
//     -Pautotest.filter=<regex>      only projects whose name matches, e.g. -Pautotest.filter="^1\.21"
//     -Pautotest.loader=<loader>     only one loader, e.g. -Pautotest.loader=fabric
//     -Pautotest.suites=<a,b,...>    only these parts: "checks", "screenshots" or check suite names
//                                    (e.g. registry-biomes,suggestion-providers - see CheckSuites in the testmod)
//
// Projects are discovered automatically: every subproject whose name ends in -fabric, -forge or -neoforge
// and that has the run task below (e.g. 1.21.1-fabric). Check `./gradlew projects` if nothing is found.
//
// Output: every run writes its reports and screenshots to autotest-report/<project>/ in the project root
// (e.g. autotest-report/1.21.1-fabric/registry-biomes.txt). The folder of a version is cleared right
// before that version runs. After each run, the project's verifyAutotestReports task fails if a report
// contains a FAIL (no reports: skipped with a warning). Compare screenshots with ./autotest.py autotest-report

val autotestRunTask = "runTestmodClient"
val autotestVerifyTask = "verifyAutotestReports"

val loaders = listOf("fabric", "neoforge", "forge")

// Fixed window size for the autotest runs (with GUI scale 1 this is also the GUI size).
val autotestWidth = "476"
val autotestHeight = "270"

val autotestFilter = providers.gradleProperty("autotest.filter").orNull?.let { Regex(it) }
val autotestLoader = providers.gradleProperty("autotest.loader").orNull?.lowercase()
val autotestSuitesProperty = providers.gradleProperty("autotest.suites").orNull

// Set in stonecutter.gradle.kts before this file is applied (this script can't see the stonecutter extension).
val activeProjectPath = findProperty("autotest.activeProject") as String?

val reportRoot = layout.projectDirectory.dir("autotest-report").asFile

// Which autotest tasks were asked for. The game only runs the tests when one of them was,
// so a normal run task during development still just opens the game.
val requestedTasks = gradle.startParameter.taskNames.map { it.substringAfterLast(':') }
val autotestRequested = requestedTasks.any { it.startsWith("autotest") }

// What the game runs (SHWG_AUTOTEST_SUITES, read by AutoTestSelection): the property wins; otherwise
// autotestChecks / autotestScreenshots limit it, and any other autotest task means everything ("").
val autotestSuites: String = autotestSuitesProperty ?: run {
    val parts = requestedTasks.filter { it.startsWith("autotest") }.map {
        when (it) {
            "autotestChecks" -> "checks"
            "autotestScreenshots" -> "screenshots"
            else -> ""
        }
    }.toSet()
    if (parts.isEmpty() || "" in parts) "" else parts.joinToString(",")
}

// "1.21.11-fabric" -> [1, 21, 11], so 1.21.2 sorts before 1.21.11 (plain text sorting would not).
fun versionParts(projectName: String): List<Int> =
    projectName.substringBefore('-').split('.').map { it.toIntOrNull() ?: 0 }

fun compareVersions(a: String, b: String): Int {
    val x = versionParts(a)
    val y = versionParts(b)
    for (i in 0 until maxOf(x.size, y.size)) {
        val c = x.getOrElse(i) { 0 }.compareTo(y.getOrElse(i) { 0 })
        if (c != 0) return c
    }
    return 0
}

val versionOrder = Comparator<Project> { a, b ->
    compareVersions(a.name, b.name).takeIf { it != 0 } ?: a.name.compareTo(b.name)
}

val root = project

fun loaderOf(p: Project): String = loaders.first { p.name.endsWith("-$it") }

// Wait until every subproject has been configured, so we can see which ones have the run task.
gradle.projectsEvaluated {
    val candidates = root.subprojects
        .filter { p -> loaders.any { p.name.endsWith("-$it") } }
        .filter { p -> p.tasks.findByName(autotestRunTask) != null }
        .filter { p -> autotestFilter?.containsMatchIn(p.name) ?: true }
        .filter { p -> autotestLoader == null || loaderOf(p) == autotestLoader }
        .sortedWith(versionOrder)

    val activeProject = activeProjectPath?.let { path -> candidates.firstOrNull { it.path == ":$path" || it.name == path } }

    if (autotestRequested) {
        if (candidates.isEmpty()) {
            logger.warn("autotest: no projects found (names must end in -fabric/-forge/-neoforge, have the run task and match the filters)")
        } else {
            logger.lifecycle("autotest: ${candidates.size} project(s): ${candidates.joinToString { it.name }}")
            logger.lifecycle("autotest: running ${autotestSuites.ifEmpty { "everything" }}")
        }
        if ("autotestActive" in requestedTasks && activeProject == null) {
            logger.warn("autotest: the active version ($activeProjectPath) isn't among the projects (filtered out?)")
        }
    }

    // The tasks you run. They do no work themselves, they just pull in the matching run tasks.
    fun registerAutotest(name: String, description: String, projects: List<Project>) {
        root.tasks.register(name) {
            group = "autotest"
            this.description = description
            projects.forEach { dependsOn("${it.path}:$autotestRunTask") }
        }
    }

    registerAutotest("autotestAll", "Runs the checks and screenshots on every version and loader", candidates)
    loaders.forEach { loader ->
        registerAutotest(
            "autotest" + loader.replaceFirstChar { it.uppercase() },
            "Runs the checks and screenshots on every $loader version",
            candidates.filter { loaderOf(it) == loader }
        )
    }
    registerAutotest("autotestActive", "Runs the checks and screenshots on the active Stonecutter version", listOfNotNull(activeProject))
    registerAutotest("autotestChecks", "Runs only the checks (no screenshots) on every version", candidates)
    registerAutotest("autotestScreenshots", "Runs only the screenshots on every version", candidates)

    // Configure each run task.
    candidates.forEachIndexed { index, p ->
        val reportDir = reportRoot.resolve(p.name)

        // Checks the reports the game wrote. Can also be run on its own to re-check the last results.
        val verify = p.tasks.register(autotestVerifyTask) {
            group = "autotest"
            description = "Fails if an autotest report for ${p.name} contains a FAIL"
            val projectName = p.name

            doLast {
                // No reports: skip instead of failing. A run that crashed before writing one still
                // fails, because the game then exits with a non-zero code and the run task fails.
                val reports = reportDir.listFiles { file -> file.extension == "txt" }.orEmpty().sortedBy { it.name }
                if (reports.isEmpty()) {
                    logger.warn("autotest [$projectName]: no reports in $reportDir, skipping the check")
                    return@doLast
                }

                // Only the check lines go to the console. Everything after the first empty line
                // (e.g. the lists of IDs) stays in the file.
                reports.forEach { report ->
                    val summary = report.readLines().takeWhile { it.isNotBlank() }
                    logger.lifecycle("autotest [$projectName] ${report.name}:\n${summary.joinToString("\n")}")
                }

                val failed = reports.filter { it.readLines().firstOrNull() != "RESULT PASS" }
                if (failed.isNotEmpty()) {
                    throw GradleException("autotest [$projectName]: failed: ${failed.joinToString { it.name }}")
                }
            }
        }

        p.tasks.named<JavaExec>(autotestRunTask) {
            // Never treat a finished game as "up to date" - launch it every time.
            outputs.upToDateWhen { false }

            if (autotestRequested) {
                // The game reads these on startup (System.getenv), see AutoTest in the testmod.
                environment("SHWG_AUTOTEST", "1")
                environment("SHWG_AUTOTEST_ID", p.name)
                environment("SHWG_AUTOTEST_REPORT_DIR", reportDir.absolutePath)
                environment("SHWG_AUTOTEST_SUITES", autotestSuites)
                doFirst {
                    // Old results must not make a broken run look green.
                    reportDir.deleteRecursively()
                    // Minecraft's own command line arguments for the window size.
                    args("--width", autotestWidth, "--height", autotestHeight)
                }
                // Runs after the game closes, also when the game crashed.
                finalizedBy(verify)
            }

            // Run strictly one at a time, in version order, even when only a subset is selected:
            // wait for every earlier project (tasks that aren't part of this run are simply ignored).
            candidates.take(index).forEach { earlier -> mustRunAfter("${earlier.path}:$autotestRunTask") }
        }
    }
}
