import gg.meza.stonecraft.mod

plugins {
    id("gg.meza.stonecraft")
    `maven-publish`
}

// Version helpers

// 26.1+ ships with Mojang mappings directly, no Loom remap step needed -
// everything below that branches on obfuscation keys off this one flag.
val isDeobfuscated = stonecutter.current.parsed >= "26.1"

// Minimum Java version required per targeted MC version. Forge below 26.1
// crashes with "Unsupported class file major version" if this doesn't match
// the toolchain actually installed for that run.
// Taken from MidnightLib https://github.com/TeamMidnightDust/MidnightLib/blob/multiversion/build.fabric.gradle.kts, thank you!
val requiredJava: JavaVersion = when {
    stonecutter.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    stonecutter.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    stonecutter.current.parsed >= "1.18" -> JavaVersion.VERSION_17
    stonecutter.current.parsed >= "1.17" -> JavaVersion.VERSION_16
    else -> JavaVersion.VERSION_1_8
}

// Testmod source set
//
// A second, separately-named source set (compiled against `main`'s output)
// used to host TestConfig and other scratch/example code, kept out of the
// library's own published jar. Runs as its own mod (`shwgconfigtestmod`)
// alongside the real one (`shwgconfig`) via the `testmodClient` run below.
//
// NOTE: its resources need their own pack.mcmeta with pack_format.
sourceSets {
    register("testmod") {
        compileClasspath += sourceSets.main.get().output + sourceSets.main.get().compileClasspath
        runtimeClasspath += sourceSets.main.get().output + sourceSets.main.get().runtimeClasspath
    }
}

loom {
    runConfigs.all {
        preferGradleTask = true
        generateRunConfig = true
    }

    runs {
        register("testmodClient") {
            client()
            displayName = "Testmod Client"
            sourceSet = "testmod"

            mods {
                register("shwgconfig") {
                    sourceSet(sourceSets.main.get())
                }
                register("shwgconfigtestmod") {
                    sourceSet(sourceSets["testmod"])
                }
            }
        }
    }

    // Registered separately so the library still declares as a normal mod
    // outside of the testmod run config too (e.g. for the regular `client` run).
    mods {
        register("shwgconfig") {
            sourceSet(sourceSets.main.get())
        }
    }

    if (mod.isForge) {
        forge {
            mixinConfig("${mod.id}.client.mixins.json")
        }
    }
}

// Mod settings (Stonecraft)
modSettings {
    clientOptions {
        fov = 90
        guiScale = 3
        narrator = false
        darkBackground = true
        musicVolume = 0.0
    }

    val authorList = project.property("mod.authors").toString()
        .split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    val authorsFormatted = if (mod.isFabric) {
        authorList.joinToString("\", \"")   // Dev1", "Dev2", "Dev3
    } else {
        authorList.joinToString(", ")       // Dev1, Dev2, Dev3
    }

    variableReplacements = mapOf(
        "fabricId" to when {
            stonecutter.current.parsed > "1.19.1" -> "fabric-api"
            else -> "fabric"
        },
        "authors" to authorsFormatted,
        "license" to project.property("mod.license").toString(),
        "issueUrl" to project.property("mod.issueUrl").toString()
    )
}

// Jar packaging - strip per-version-obsolete resources so the published jar
// doesn't carry assets/code paths that no longer apply above a given version.
tasks.jar {
    if (stonecutter.current.parsed >= "1.19.4") {
        exclude("assets/minecraft/textures/gui/slider.png")
        exclude("dev/shwg/shwgconfig/render/scissor/")
        exclude("dev/shwg/shwgconfig/gui/navigation/")
    }

    if (stonecutter.current.parsed >= "1.20.2") {
        exclude("assets/minecraft/textures/gui/sprites/widget/text_field**")
    }

    if (stonecutter.current.parsed >= "1.21.2") {
        exclude("assets/minecraft/textures/gui/sprites/tooltip/")
    }
}

// Stonecutter string replacements - renamed/relocated vanilla classes across
// versions that aren't expressible as simple `//?` conditionals.
stonecutter {
    replacements.string(stonecutter.current.parsed < "1.21.11") {
        replace("Identifier", "ResourceLocation")
    }
    replacements.string(stonecutter.current.parsed > "1.19.4") {
        replace("ItemStack.isSame(", "ItemStack.isSameItem(")
    }
    replacements.string(stonecutter.current.parsed > "1.21.11") {
        replace("GuiGraphics", "GuiGraphicsExtractor")
    }

    replacements.string(stonecutter.current.parsed < "26.2") {
        replace("Minecraft.getInstance().gui.screen()", "Minecraft.getInstance().screen")
        replace("Minecraft.getInstance().gui.setScreen(", "Minecraft.getInstance().setScreen(")
        replace("minecraft.gui.screen()", "minecraft.screen")
        replace("client.gui.setScreen(", "client.setScreen(")
    }

    replacements.string(stonecutter.current.parsed > "26.2") {
        replace("InputConstants.Type.KEYSYM", "InputConstants.Type.KEYBOARD")
    }
}

// Repositories & dependencies
repositories {
    mavenLocal()
    mavenCentral()
    maven("https://maven.terraformersmc.com/")
    maven("https://maven.shedaniel.me/")
    maven("https://maven.meza.gg/releases/")
    maven("https://maven.fabricmc.net/")
}

dependencies {
    // Pre-26.1 (obfuscated) needs the `mod*` configurations for Loom's remap
    // step; 26.1+ (deobfuscated) uses plain Gradle configurations instead.
    val implementationConfiguration = if (isDeobfuscated) "implementation" else "modImplementation"
    val apiConfiguration = if (isDeobfuscated) "api" else "modApi"

    // Required by the testmod's ModMenu entrypoint on Fabric.
    if (mod.isFabric && mod.hasProp("modmenu_version")) {
        add(apiConfiguration, "com.terraformersmc:modmenu:${mod.prop("modmenu_version")}")
    }
}

// Mod publishing (Modrinth / CurseForge)
publishMods {
    modrinth {
        if (mod.isFabric) requires("fabric-api")
    }

    curseforge {
        client = true
        server = false
        if (mod.isFabric) requires("fabric-api")
    }
}

// runTestmodActive (the "run whatever version is currently active" version
// of this) lives in stonecutter.gradle.kts instead, not here
tasks.register("runTestmod") {
    group = "minecraft"
    description = "Runs the Testmod client."
    dependsOn("testmodClient")
}

val testmodJar = tasks.register<Jar>("testmodJar") {
    group = "build"
    description = "Builds the testmod as its own distributable jar."
    // Pre-26.1 this is only the unremapped dev jar; remapTestmodJar makes the real "-testmod" one.
    archiveClassifier.set(if (isDeobfuscated) "testmod" else "testmod-dev")
    from(sourceSets["testmod"].output)
}

// Pre-26.1 output is obfuscated and needs the same remap step as the main jar.
if (isDeobfuscated) {
    tasks.named("assemble") { dependsOn(testmodJar) }
} else {
    val remapTestmodJar = tasks.register<net.fabricmc.loom.task.RemapJarTask>("remapTestmodJar") {
        group = "build"
        dependsOn(testmodJar)
        inputFile.set(testmodJar.flatMap { it.archiveFile })
        archiveClassifier.set("testmod")
        classpath.from(sourceSets["testmod"].compileClasspath)
    }
    tasks.named("assemble") { dependsOn(remapTestmodJar) }
}

// without this, gradle complains javadoc/testmod compile read from
// generatePackMCMetaJson's output without depending on it
tasks.named("javadoc") {
    dependsOn(tasks.matching { it.name == "generatePackMCMetaJson" })
}

tasks.named("compileTestmodJava") {
    dependsOn(tasks.matching { it.name == "generatePackMCMetaJson" })
}

// Java toolchain & published artifacts
java {
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava

    toolchain {
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }

    // has to be this, not a manual sourcesJar task, or publishing just skips it
    withSourcesJar()
}

val javadocJar = tasks.register<Jar>("javadocJar") {
    description = "Builds JavaDoc jar"
    archiveClassifier.set("javadoc")
    from(tasks.named("javadoc"))
}

// Maven publishing (local + Modrinth Maven)
publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "dev.shwg"
            artifactId = "shwgconfig-${mod.loader}"
            version = "${mod.version}+${stonecutter.current.version}"

            // below 26.1, jar = unremapped dev jar, remapJar = the real one
            artifact(if (isDeobfuscated) tasks.named("jar") else tasks.named("remapJar"))
            artifact(tasks.named("sourcesJar"))
            artifact(javadocJar)
        }
    }
}

publishMods {
    modrinth {
        projectId = providers.gradleProperty("MODRINTH_ID")
            .orElse(providers.environmentVariable("MODRINTH_ID"))
        accessToken = providers.gradleProperty("MODRINTH_TOKEN")
            .orElse(providers.environmentVariable("MODRINTH_TOKEN"))

        if (mod.isFabric) {
            requires("fabric-api")
        }

        additionalFile(tasks.named("sourcesJar")) {
            type = SOURCES_JAR
        }

        additionalFile(javadocJar){
            type = JAVADOC_JAR
        }


    }

//    curseforge {
//        clientRequired = true
//        serverRequired = false
//        if (mod.isFabric) {
//            requires("fabric-api")
//            optional("modmenu")
//        }
//        requires("cloth-config")
//    }
}
