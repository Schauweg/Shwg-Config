plugins {
    id("dev.kikugie.stonecutter")
    id("gg.meza.stonecraft")
}

stonecutter active "26.2-neoforge" /* [SC] DO NOT EDIT */

tasks.register("runActiveTestmod") {
    group = "mod"
    description = "Runs the Testmod client for the active Stonecutter project."
    dependsOn(project(stonecutter.current!!.project).tasks.named("runTestmodClient"))
}
