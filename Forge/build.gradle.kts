plugins {
    java
    idea
    id("net.minecraftforge.gradle") version "6.0.21"
    id("org.spongepowered.mixin") version "0.7-SNAPSHOT"
    id("org.parchmentmc.librarian.forgegradle") version "1.+"
}

val modId: String by project
val minecraftVersion: String by project        // e.g., 1.20.1
val forgeVersion: String by project            // e.g., 47.4.0
val parchmentVersion: String by project        // e.g., 2023.09.03
val parchmentMinecraftVersion: String by project // e.g., 1.20.1

base { archivesName.set("${modId}-forge-${minecraftVersion}") }

repositories {
    maven("https://maven.minecraftforge.net")
    maven("https://maven.parchmentmc.org")
    maven("https://alcatrazescapee.jfrog.io/artifactory/mods")
    mavenCentral()
}

dependencies {
    // Forge userdev
    "minecraft"("net.minecraftforge:forge:$minecraftVersion-$forgeVersion")

    // No project(":Common") — we inline the sources below
    if (System.getProperty("idea.sync.active") != "true") {
        annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
    }
}

sourceSets {
    main {
        java.srcDir("../Common/src/main/java")
        resources.srcDir("../Common/src/main/resources")
    }
    test {
        java.srcDir("src/test/java")
        resources.srcDir("src/test/resources")
    }
}

minecraft {
    // Correct order: date-first then MC version (e.g., 2023.09.03-1.20.1)
    mappings("parchment", "$parchmentVersion-$parchmentMinecraftVersion")

    runs {
        create("client") {
            workingDirectory(file("run"))
            arg("-mixin.config=$modId.mixins.json")
            property("forge.logging.console.level", "info")
            mods {
                create(modId) {
                    source(sourceSets.main.get())
                }
            }
        }
        create("server") {
            workingDirectory(file("run"))
            arg("--nogui")
            arg("-mixin.config=$modId.mixins.json")
            property("forge.logging.console.level", "info")
            mods {
                create(modId) {
                    source(sourceSets.main.get())
                }
            }
        }
        create("gameTestServer") {
            workingDirectory(file("run-test"))
            arg("--nogui")
            arg("-mixin.config=$modId.mixins.json")
            property("forge.logging.console.level", "info")
            property("forge.enabledGameTestNamespaces", modId)
            mods {
                create(modId) {
                    source(sourceSets.main.get())
                    source(sourceSets.test.get())
                }
            }
        }
    }
}

// EULA automation tasks
tasks.register("acceptServerEula") {
    description = "Automatically accepts the Minecraft EULA for server runs"
    doLast {
        val runDir = file("run")
        runDir.mkdirs()
        val eulaFile = file("run/eula.txt")
        eulaFile.writeText("# Generated automatically by Gradle\n# By changing the setting below to TRUE you are indicating your agreement to our EULA (https://account.mojang.com/documents/minecraft_eula).\neula=true\n")
        println("Created eula.txt in run directory")
    }
}

tasks.register("acceptGameTestEula") {
    description = "Automatically accepts the Minecraft EULA for GameTest server runs"
    doLast {
        val runTestDir = file("run-test")
        runTestDir.mkdirs()  
        val eulaFile = file("run-test/eula.txt")
        eulaFile.writeText("# Generated automatically by Gradle\n# By changing the setting below to TRUE you are indicating your agreement to our EULA (https://account.mojang.com/documents/minecraft_eula).\neula=true\n")
        println("Created eula.txt in run-test directory")
    }
}

// Add EULA acceptance directly to run tasks
tasks.configureEach {
    if (name == "runServer") {
        doFirst {
            val runDir = file("run")
            runDir.mkdirs()
            val eulaFile = file("run/eula.txt")
            if (!eulaFile.exists()) {
                eulaFile.writeText("# Generated automatically by Gradle\n# By changing the setting below to TRUE you are indicating your agreement to our EULA (https://account.mojang.com/documents/minecraft_eula).\neula=true\n")
                println("Auto-created eula.txt in run directory")
            }
        }
    }
    if (name == "runGameTestServer") {
        doFirst {
            val runTestDir = file("run-test")
            runTestDir.mkdirs()
            val eulaFile = file("run-test/eula.txt")
            if (!eulaFile.exists()) {
                eulaFile.writeText("# Generated automatically by Gradle\n# By changing the setting below to TRUE you are indicating your agreement to our EULA (https://account.mojang.com/documents/minecraft_eula).\neula=true\n")
                println("Auto-created eula.txt in run-test directory")
            }
            // Copy template files to multiple locations GameTest might look for them
            val src = file("src/test/resources/gameteststructures")
            val dst = file("run-test/gameteststructures").apply { mkdirs() }
            copy {
                from(src)
                into(dst)
                include("**/*.snbt")
            }
            
            // Also copy from the data directory if it exists
            val srcData = file("src/test/resources/data/${modId}/gameteststructures")
            if (srcData.exists()) {
                copy {
                    from(srcData)
                    into(dst)
                    include("**/*.snbt")
                }
            }
            
            // Copy with simple names for GameTest framework
            val srcGt = file("src/test/resources/gameteststructures")
            if (srcGt.exists()) {
                copy {
                    from(srcGt) {
                        rename { fileName ->
                            fileName.replace("sharptoolharvesttests.", "")
                        }
                    }
                    into(dst)
                    include("**/*.snbt")
                }
            }
        }
    }
}

mixin {
    add(sourceSets.main.get(), "${modId}.refmap.json")
    config("${modId}.mixins.json")
    config("${modId}.common.mixins.json")
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    // If you template metadata, keep this:
    filesMatching(listOf("META-INF/mods.toml", "pack.mcmeta")) {
        expand(
            mapOf(
                "modId" to modId,
                "modVersion" to project.version,
                "minecraftVersionRange" to project.findProperty("minecraftVersionRange"),
                "forgeVersionRange" to project.findProperty("forgeVersionRange"),
                "modName" to project.findProperty("modName"),
                "modAuthor" to project.findProperty("modAuthor"),
                "modDescription" to project.findProperty("modDescription"),
                "modIssueUrl" to project.findProperty("modIssueUrl"),
                "modHomeUrl" to project.findProperty("modHomeUrl")
            )
        )
    }
}

tasks.processTestResources {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.jar {
    // Ensure single distributable jar output
    archiveClassifier.set("")
}

// Clean jar task configuration - no JarJar dependencies
afterEvaluate {
    tasks.named("jar") {
        finalizedBy("reobfJar")
    }
}
tasks.register<Copy>("copyGameTestStructures") {
    from("src/test/resources/gameteststructures") // <-- put your .snbt here in source
    into("run-test/gameteststructures")
}
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(17)
}

idea {
    module {
        excludeDirs.addAll(listOf("run", "out", "logs").map(::file))
    }
}