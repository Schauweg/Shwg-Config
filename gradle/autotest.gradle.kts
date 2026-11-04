// Autotest tasks (group "autotest"), applied from stonecutter.gradle.kts.
// Always add --continue, so one failing version doesn't stop the rest:
//
//     ./gradlew autotestAll --continue           every version and loader: checks + screenshots
//     ./gradlew autotestFabric --continue        only Fabric runs (same for autotestNeoforge / autotestForge)
//     ./gradlew autotestActive --continue        only the runs of the active Stonecutter project
//     ./gradlew autotestChecks --continue        every version, checks only (no screenshots, fast)
//     ./gradlew autotestScreenshots --continue   every version, screenshots only
//
// Optional properties, combine freely with any task:
//
//     -Pautotest.filter=<regex>      only runs whose id matches, e.g. -Pautotest.filter="^1\.19\.2"
//     -Pautotest.loader=<loader>     only one loader, e.g. -Pautotest.loader=fabric
//     -Pautotest.suites=<a,b,...>    only these parts: "checks", "screenshots" or check suite names
//                                    (e.g. registry-biomes,suggestion-providers - see CheckSuites in the testmod)
//     -Pautotest.timeout=<minutes>   kill a run after this long (default 15, includes the first download)
//     -Pautotest.portablemc=<path>   the portablemc executable (default: "portablemc" from the PATH)
//     -Pautotest.dir=<path>          cache and run folders (default: .gradle/autotest)
//
// Runs: every project whose name ends in -fabric, -forge or -neoforge (e.g. 1.21-fabric) is tested on
// its own Minecraft version and on every additional_versions entry of versions/dependencies/<version>.properties.
// One run = the built jars (lib + testmod) of that project, launched with portablemc in a real instance:
//
//     own version:         the exact loader version from the properties (Fabric API / ModMenu: the pinned ones)
//     additional versions: the latest stable Forge / NeoForge build, the newest Fabric API / ModMenu for that version
//
// Folders (in -Pautotest.dir):
//     minecraft/                  portablemc's shared folder: versions, libraries, assets, Java runtimes
//     mods/                       downloaded Fabric API / ModMenu jars
//     runs/<version>-<loader>/    the game folder of one run, recreated for every run
//
// Output: every run writes to autotest-report/<version>-<loader>/ in the project root, cleared right before
// the run: the testmod's reports and screenshots, launcher.log (launcher + game output), latest.log and crash
// reports. A run fails if the game didn't write the FINISHED marker (crashed, failed to load, timed out) or a
// report contains a FAIL. Compare screenshots with ./autotest.py autotest-report

val loaders = listOf("fabric", "neoforge", "forge")

// Fixed window size for the autotest runs (with GUI scale 1 this is also the GUI size).
val autotestWidth = "476"
val autotestHeight = "270"

val autotestFilter = providers.gradleProperty("autotest.filter").orNull?.let { Regex(it) }
val autotestLoader = providers.gradleProperty("autotest.loader").orNull?.lowercase()
val autotestSuitesProperty = providers.gradleProperty("autotest.suites").orNull
val autotestTimeoutMinutes = providers.gradleProperty("autotest.timeout").orNull?.toLong() ?: 15L
val portablemc = providers.gradleProperty("autotest.portablemc").orNull ?: "portablemc"
val skipProcessorChecks = providers.gradleProperty("autotest.skipProcessorChecks").orNull?.toBoolean() ?: false

// Set in stonecutter.gradle.kts before this file is applied (this script can't see the stonecutter extension).
val activeProjectPath = findProperty("autotest.activeProject") as String?

val reportRoot = layout.projectDirectory.dir("autotest-report").asFile
val dependenciesDir = layout.projectDirectory.dir("versions/dependencies").asFile
val templateDir = layout.projectDirectory.dir("versions/instance_layout/minecraft").asFile

val autotestDir = providers.gradleProperty("autotest.dir").orNull?.let { file(it) }
    ?: layout.projectDirectory.dir(".gradle/autotest").asFile
val minecraftDir = autotestDir.resolve("minecraft")
val modCacheDir = autotestDir.resolve("mods")
val runsDir = autotestDir.resolve("runs")

// Written by the testmod right before it quits normally (AutoTest#markFinished).
val finishedMarker = "FINISHED"

val modrinthUserAgent = "Schauweg/Shwg-Config autotest"

// Which autotest tasks were asked for.
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
fun versionParts(id: String): List<Int> =
    id.substringBefore('-').split('.').map { it.toIntOrNull() ?: 0 }

fun compareVersions(a: String, b: String): Int {
    val x = versionParts(a)
    val y = versionParts(b)
    for (i in 0 until maxOf(x.size, y.size)) {
        val c = x.getOrElse(i) { 0 }.compareTo(y.getOrElse(i) { 0 })
        if (c != 0) return c
    }
    return 0
}

val root = project

fun loaderOf(p: Project): String = loaders.first { p.name.endsWith("-$it") }

// ---------------------------------------------------------------------------------------------------------
// Runs
// ---------------------------------------------------------------------------------------------------------

// One game launch: the jars built by `project`, started on Minecraft `mcVersion`.
data class AutotestRun(
    val project: Project,
    val loader: String,
    val mcVersion: String,
    val primary: Boolean,
    val launchVersion: String,
    val props: Map<String, String>
) {
    val id = "$mcVersion-$loader"
    val taskName = "autotestRun-$id"
}

fun readProperties(file: File): Map<String, String> {
    val props = java.util.Properties()
    file.reader().use { props.load(it) }
    return props.stringPropertyNames().associateWith { props.getProperty(it).trim() }
}

// The VERSION argument for "portablemc start". Own version: the exact loader version from the
// properties. Additional versions: the latest stable loader build for that Minecraft version.
fun launchVersion(loader: String, mcVersion: String, primary: Boolean, props: Map<String, String>): String? =
    when (loader) {
        "fabric" -> props["loader_version"]?.let { "fabric:$mcVersion:$it" }
        "forge" -> if (primary) props["forge_version"]?.let { "forge::$it" } else "forge:$mcVersion"
        "neoforge" -> if (primary) props["neoforge_version"]?.let { "neoforge::$it" } else "neoforge:$mcVersion:unstable"
        else -> null
    }

fun runsOf(p: Project): List<AutotestRun> {
    val loader = loaderOf(p)
    val projectVersion = p.name.removeSuffix("-$loader")
    val file = dependenciesDir.resolve("$projectVersion.properties")
    if (!file.exists()) {
        logger.warn("autotest [${p.name}]: no $file, skipping")
        return emptyList()
    }
    val props = readProperties(file)
    val mainVersion = props["minecraft_version"] ?: projectVersion
    val additional = props["additional_versions"].orEmpty()
        .split(',').map { it.trim() }.filter { it.isNotEmpty() }

    return (listOf(mainVersion) + additional).mapNotNull { version ->
        val primary = version == mainVersion
        val launch = launchVersion(loader, version, primary, props)
        if (launch == null) {
            logger.warn("autotest [${p.name}]: no $loader version in ${file.name}, skipping $version")
            null
        } else {
            AutotestRun(p, loader, version, primary, launch, props)
        }
    }
}

// Before 26.1 the real jars come from the remap tasks, from 26.1 on from the plain jar tasks (see build.gradle.kts).
fun jarTasks(p: Project): Pair<String, String> =
    if (p.tasks.findByName("remapTestmodJar") != null) "remapJar" to "remapTestmodJar" else "jar" to "testmodJar"

fun archiveOf(p: Project, taskName: String): File =
    (p.tasks.getByName(taskName) as org.gradle.api.tasks.bundling.AbstractArchiveTask).archiveFile.get().asFile

// ---------------------------------------------------------------------------------------------------------
// Fabric API / ModMenu from Modrinth (same choice as prism-instances.py)
// ---------------------------------------------------------------------------------------------------------

val modrinthCache = mutableMapOf<String, List<Map<String, Any?>>>()

fun openUrl(url: String): java.io.InputStream {
    val connection = java.net.URI(url).toURL().openConnection()
    connection.setRequestProperty("User-Agent", modrinthUserAgent)
    return connection.getInputStream()
}

@Suppress("UNCHECKED_CAST")
fun modrinthVersions(slug: String, mcVersion: String): List<Map<String, Any?>> =
    modrinthCache.getOrPut("$slug@$mcVersion") {
        fun encode(value: String) = java.net.URLEncoder.encode(value, Charsets.UTF_8)
        val query = "loaders=" + encode("[\"fabric\"]") + "&game_versions=" + encode("[\"$mcVersion\"]")
        openUrl("https://api.modrinth.com/v2/project/$slug/version?$query").use {
            groovy.json.JsonSlurper().parse(it) as List<Map<String, Any?>>
        }
    }

fun download(url: String, target: File) {
    target.parentFile.mkdirs()
    val part = File(target.path + ".part")
    openUrl(url).use { input -> part.outputStream().use { input.copyTo(it) } }
    if (!part.renameTo(target)) {
        part.copyTo(target, overwrite = true)
        part.delete()
    }
}

// Own version: the pinned version (falls back to the newest if Modrinth doesn't list it).
// Additional versions: the newest version for that Minecraft version (Modrinth lists newest first).
@Suppress("UNCHECKED_CAST")
fun modrinthJar(slug: String, run: AutotestRun, pinned: String?): File? {
    val versions = modrinthVersions(slug, run.mcVersion)
    var version = if (run.primary && pinned != null) versions.firstOrNull { it["version_number"] == pinned } else null
    if (version == null && run.primary && pinned != null) {
        logger.warn("autotest [${run.id}]: $slug $pinned not found on Modrinth, using the newest for ${run.mcVersion}")
    }
    version = version ?: versions.firstOrNull() ?: return null

    val files = version["files"] as List<Map<String, Any?>>
    val file = files.firstOrNull { it["primary"] == true } ?: files.firstOrNull() ?: return null
    val target = modCacheDir.resolve(file["filename"] as String)
    if (!target.exists()) {
        logger.lifecycle("autotest [${run.id}]: downloading ${target.name}")
        download(file["url"] as String, target)
    }
    return target
}

// ---------------------------------------------------------------------------------------------------------
// One run: prepare the game folder, launch, check the results
// ---------------------------------------------------------------------------------------------------------

fun killTree(process: Process) {
    val children = process.descendants().toList()
    process.destroyForcibly()
    children.forEach { it.destroyForcibly() }
}

fun copyLogs(runDir: File, reportDir: File) {
    runDir.resolve("logs/latest.log").takeIf { it.exists() }?.copyTo(reportDir.resolve("latest.log"), overwrite = true)
    runDir.resolve("crash-reports").takeIf { it.isDirectory }?.copyRecursively(reportDir.resolve("crash-reports"), overwrite = true)
}

fun launch(run: AutotestRun) {
    val reportDir = reportRoot.resolve(run.id)
    val runDir = runsDir.resolve(run.id)

    // Old results must not make a broken run look green.
    reportDir.deleteRecursively()
    reportDir.mkdirs()
    runDir.deleteRecursively()
    if (templateDir.isDirectory) templateDir.copyRecursively(runDir)
    val modsDir = runDir.resolve("mods").apply { mkdirs() }

    val (libTask, testmodTask) = jarTasks(run.project)
    val mods = mutableListOf(archiveOf(run.project, libTask), archiveOf(run.project, testmodTask))
    if (run.loader == "fabric") {
        mods += modrinthJar("fabric-api", run, run.props["fabric_version"])
            ?: throw GradleException("autotest [${run.id}]: no Fabric API on Modrinth for ${run.mcVersion}")
        if (run.props["modmenu_version"] != null) {
            val modmenu = modrinthJar("modmenu", run, run.props["modmenu_version"])
            if (modmenu != null) mods += modmenu
            else logger.warn("autotest [${run.id}]: no ModMenu on Modrinth for ${run.mcVersion}, running without it")
        }
    }
    mods.forEach { it.copyTo(modsDir.resolve(it.name), overwrite = true) }

    val command = buildList {
        addAll(listOf(portablemc, "--main-dir", minecraftDir.absolutePath, "start"))
        addAll(listOf("--mc-dir", runDir.absolutePath))
        addAll(listOf("--jvm-policy", "mojang-then-system"))
        addAll(listOf("--resolution", "${autotestWidth}x$autotestHeight"))
        if (skipProcessorChecks && run.loader != "fabric") add("--no-check-processor-outputs")
        addAll(listOf("-u", "Autotest", run.launchVersion))
    }

    val launcherLog = reportDir.resolve("launcher.log")
    val builder = ProcessBuilder(command)
        .directory(runDir)
        .redirectErrorStream(true)
        .redirectOutput(launcherLog)
    // The game reads these on startup (System.getenv), see AutoTest in the testmod.
    builder.environment().putAll(
        mapOf(
            "SHWG_AUTOTEST" to "1",
            "SHWG_AUTOTEST_ID" to run.id,
            "SHWG_AUTOTEST_REPORT_DIR" to reportDir.absolutePath,
            "SHWG_AUTOTEST_SUITES" to autotestSuites
        )
    )
    builder.environment().remove("WAYLAND_DISPLAY")
    builder.environment()["XDG_SESSION_TYPE"] = "x11"

    logger.lifecycle("autotest [${run.id}]: launching ${run.launchVersion} with the jars of ${run.project.name}")
    logger.lifecycle("autotest [${run.id}]: output in $launcherLog")

    val process = try {
        builder.start()
    } catch (e: java.io.IOException) {
        throw GradleException("autotest: could not start '$portablemc' - is it installed (pip install portablemc) or set with -Pautotest.portablemc?", e)
    }

    // Also kills the game when Gradle is cancelled (Ctrl+C interrupts waitFor).
    val exitedInTime = try {
        process.waitFor(autotestTimeoutMinutes, java.util.concurrent.TimeUnit.MINUTES)
    } finally {
        if (process.isAlive) killTree(process)
    }

    copyLogs(runDir, reportDir)
    verify(run, reportDir, exitedInTime, if (exitedInTime) process.exitValue() else null)
}

fun verify(run: AutotestRun, reportDir: File, exitedInTime: Boolean, exitCode: Int?) {
    // Only the check lines go to the console. Everything after the first empty line
    // (e.g. the lists of IDs) stays in the file.
    val reports = reportDir.listFiles { file -> file.extension == "txt" }.orEmpty().sortedBy { it.name }
    reports.forEach { report ->
        val summary = report.readLines().takeWhile { it.isNotBlank() }
        logger.lifecycle("autotest [${run.id}] ${report.name}:\n${summary.joinToString("\n")}")
    }

    val problems = mutableListOf<String>()
    if (!exitedInTime) {
        problems += "timed out after $autotestTimeoutMinutes minutes and was killed"
    } else if (!reportDir.resolve(finishedMarker).exists()) {
        problems += "the game didn't finish the run (exit code $exitCode): crashed, failed to load the mods, or the testmod failed"
    }
    if (reportDir.resolve("crash-reports").isDirectory) {
        problems += "the game wrote a crash report"
    }
    val failed = reports.filter { it.readLines().firstOrNull() != "RESULT PASS" }
    if (failed.isNotEmpty()) {
        problems += "failed: ${failed.joinToString { it.name }}"
    }

    if (problems.isNotEmpty()) {
        throw GradleException("autotest [${run.id}]: ${problems.joinToString("; ")} - see $reportDir")
    }
    logger.lifecycle("autotest [${run.id}]: PASS")
}

// ---------------------------------------------------------------------------------------------------------
// Tasks
// ---------------------------------------------------------------------------------------------------------

// Wait until every subproject has been configured, so we can see which ones have the jar tasks.
gradle.projectsEvaluated {
    val projects = root.subprojects
        .filter { p -> loaders.any { p.name.endsWith("-$it") } }
        .filter { p -> p.tasks.findByName("testmodJar") != null }

    val runs = projects.flatMap { runsOf(it) }
        .filter { run -> autotestFilter?.containsMatchIn(run.id) ?: true }
        .filter { run -> autotestLoader == null || run.loader == autotestLoader }
        .sortedWith { a, b -> compareVersions(a.id, b.id).takeIf { it != 0 } ?: a.id.compareTo(b.id) }

    val activeProject = activeProjectPath?.let { path -> projects.firstOrNull { it.path == ":$path" || it.name == path } }
    val activeRuns = runs.filter { it.project == activeProject }

    if (autotestRequested) {
        if (runs.isEmpty()) {
            logger.warn("autotest: no runs found (project names must end in -fabric/-forge/-neoforge, have a testmodJar task, a properties file, and match the filters)")
        } else {
            logger.lifecycle("autotest: ${runs.size} run(s): ${runs.joinToString { it.id }}")
            logger.lifecycle("autotest: running ${autotestSuites.ifEmpty { "everything" }}")
        }
        if ("autotestActive" in requestedTasks && activeRuns.isEmpty()) {
            logger.warn("autotest: no runs for the active project ($activeProjectPath) (filtered out?)")
        }
    }

    // One task per run. Strictly one at a time, in version order, even when only a subset is selected:
    // each waits for every earlier run (tasks that aren't part of this build are simply ignored).
    runs.forEachIndexed { index, run ->
        val (libTask, testmodTask) = jarTasks(run.project)
        root.tasks.register(run.taskName) {
            group = "autotest"
            description = "Launches ${run.project.name}'s jars on ${run.mcVersion} (${run.loader}) and checks the results"
            dependsOn("${run.project.path}:$libTask", "${run.project.path}:$testmodTask")
            runs.take(index).forEach { earlier -> mustRunAfter(earlier.taskName) }
            doLast { launch(run) }
        }
    }

    // The tasks you run. They do no work themselves, they just pull in the matching runs.
    fun registerAutotest(name: String, description: String, selected: List<AutotestRun>) {
        root.tasks.register(name) {
            group = "autotest"
            this.description = description
            selected.forEach { dependsOn(it.taskName) }
        }
    }

    registerAutotest("autotestAll", "Runs the checks and screenshots on every version and loader", runs)
    loaders.forEach { loader ->
        registerAutotest(
            "autotest" + loader.replaceFirstChar { it.uppercase() },
            "Runs the checks and screenshots on every $loader version",
            runs.filter { it.loader == loader }
        )
    }
    registerAutotest("autotestActive", "Runs the checks and screenshots on the active Stonecutter project's versions", activeRuns)
    registerAutotest("autotestChecks", "Runs only the checks (no screenshots) on every version", runs)
    registerAutotest("autotestScreenshots", "Runs only the screenshots on every version", runs)
}
