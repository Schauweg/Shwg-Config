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

// Autotest tasks (autotestAll, autotestActive, autotestChecks, ...) - see gradle/autotest.gradle.kts.
// The script can't see the stonecutter extension, so the active version is handed over first.
extra["autotest.activeProject"] = stonecutter.current!!.project
apply(from = "gradle/autotest.gradle.kts")
