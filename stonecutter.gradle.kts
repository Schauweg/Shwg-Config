plugins {
    id("dev.kikugie.stonecutter")
    id("gg.meza.stonecraft")
}

stonecutter active "1.21.11-fabric" /* [SC] DO NOT EDIT */

tasks.register("runActiveTestmod") {
    group = "mod"
    description = "Runs the Testmod client for the active Stonecutter project."
    dependsOn(project(stonecutter.current!!.project).tasks.named("runTestmodClient"))
}

// Tasks (group "autotest"). Always add --continue so one failing version doesn't stop the rest:
//
//     ./gradlew autotestAll --continue        every version and loader
//     ./gradlew autotestFabric --continue     only Fabric projects
//     ./gradlew autotestNeoforge --continue   only NeoForge projects
//     ./gradlew autotestForge --continue      only Forge projects
//
// Optional properties:
//
//     -Pautotest.filter=<regex>   only projects whose name matches, e.g. -Pautotest.filter="^1\.21"
//
// Projects are discovered automatically: every subproject whose name ends in
// -fabric, -forge or -neoforge and that has the run task above (e.g. 1.21.1-fabric).
// Check `./gradlew projects` if nothing is found.
val autotestRunTask = "runTestmodClient"

val loaders = listOf("fabric", "neoforge", "forge")

// Fixed window size for the autotest runs (with GUI scale 1 this is also the GUI size).
val autotestWidth = "476"
val autotestHeight = "270"
val autotestFilter = providers.gradleProperty("autotest.filter").orNull?.let { Regex(it) }

// Only switch the auto-run on when an autotest task was asked for, so a normal
// run task during development still just opens the game.
val autotestRequested = gradle.startParameter.taskNames.any {
    it.substringAfterLast(':').startsWith("autotest")
}

// "1.21.11-fabric" -> [1, 21, 11], so 1.21.2 sorts before 1.21.11 (plain text sorting would not).
fun versionParts(projectName: String): List<Int> =
    projectName.substringBefore('-').split('.').map { it.toIntOrNull() ?: 0 }

val versionOrder = Comparator<Project> { a, b ->
    val x = versionParts(a.name)
    val y = versionParts(b.name)
    for (i in 0 until maxOf(x.size, y.size)) {
        val c = x.getOrElse(i) { 0 }.compareTo(y.getOrElse(i) { 0 })
        if (c != 0) return@Comparator c
    }
    a.name.compareTo(b.name)
}

val root = project

// Wait until every subproject has been configured, so we can see which ones have the run task.
gradle.projectsEvaluated {
    val candidates = root.subprojects
        .filter { p -> loaders.any { p.name.endsWith("-$it") } }
        .filter { p -> p.tasks.findByName(autotestRunTask) != null }
        .filter { p -> autotestFilter?.containsMatchIn(p.name) ?: true }
        .sortedWith(versionOrder)

    fun loaderOf(p: Project): String = loaders.first { p.name.endsWith("-$it") }

    if (autotestRequested) {
        if (candidates.isEmpty()) {
            logger.warn("autotest: no projects found (names must end in -fabric/-forge/-neoforge and have the run task)")
        } else {
            logger.lifecycle("autotest: ${candidates.size} project(s): ${candidates.joinToString { it.name }}")
        }
    }

    // The tasks you run. They do no work themselves, they just pull in the matching run tasks.
    root.tasks.register("autotestAll") {
        group = "autotest"
        description = "Runs the render test on every version and loader"
        candidates.forEach { dependsOn("${it.path}:$autotestRunTask") }
    }
    loaders.forEach { loader ->
        root.tasks.register("autotest" + loader.replaceFirstChar { it.uppercase() }) {
            group = "autotest"
            description = "Runs the render test on every $loader version"
            candidates.filter { loaderOf(it) == loader }.forEach { dependsOn("${it.path}:$autotestRunTask") }
        }
    }

    // Configure each run task.
    candidates.forEachIndexed { index, p ->
        p.tasks.named<JavaExec>(autotestRunTask) {
            // Never treat a finished game as "up to date" - launch it every time.
            outputs.upToDateWhen { false }

            if (autotestRequested) {
                // The game reads these on startup (System.getenv).
                environment("SHWG_AUTOTEST", "1")
                environment("SHWG_AUTOTEST_ID", p.name) // e.g. "1.21.1-fabric", use it for the screenshot folder
                // Minecraft's own command line arguments for the window size.
                doFirst { args("--width", autotestWidth, "--height", autotestHeight) }
            }

            // Run strictly one at a time, in version order, even when only a subset is selected:
            // wait for every earlier project (tasks that aren't part of this run are simply ignored).
            candidates.take(index).forEach { earlier -> mustRunAfter("${earlier.path}:$autotestRunTask") }
        }
    }
}
