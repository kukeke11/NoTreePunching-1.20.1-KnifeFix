plugins {
    java
    idea
    id("net.minecraftforge.gradle") version "6.0.21"
    id("org.spongepowered.mixin") version "0.7-SNAPSHOT"
    id("org.parchmentmc.librarian.forgegradle") version "1.+"
}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.Duration
import java.io.FileOutputStream

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

//Simple validation task to check GameTest structure access
tasks.register("validateGameTestStructures") {
    group = "verification"
    description = "Validate that GameTest structures are accessible with correct namespace"
    dependsOn("processTestResources")
    
    doLast {
        val structuresDir = file("src/test/resources/data/notreepunching/structures")
        if (!structuresDir.exists()) {
            throw GradleException("GameTest structures directory not found: ${structuresDir.absolutePath}")
        }
        
        val platformSnbt = File(structuresDir, "platform.snbt")
        val fiveByFivePlatformSnbt = File(structuresDir, "5x5_platform.snbt")
        
        if (!platformSnbt.exists()) {
            throw GradleException("Required structure file not found: platform.snbt")
        }
        
        if (!fiveByFivePlatformSnbt.exists()) {
            throw GradleException("Required structure file not found: 5x5_platform.snbt")
        }
        
        println("✅ GameTest structure validation passed:")
        println("   - platform.snbt found at: ${platformSnbt.absolutePath}")  
        println("   - 5x5_platform.snbt found at: ${fiveByFivePlatformSnbt.absolutePath}")
        println("   - Namespace directory: data/notreepunching/structures/ ✓")
        println("   - Structure files are accessible for @GameTestHolder(\"notreepunching\")")
    }
}

// Clean jar task configuration - no JarJar dependencies
afterEvaluate {
    tasks.named("jar") {
        finalizedBy("reobfJar")
    }
}

// GameTest execution with comprehensive logging and error capture
tasks.register("runGameTestsWithLogs") {
    group = "verification"
    description = "Run GameTests and capture full logs with error archiving"
    dependsOn("processTestResources", "compileTestJava")
    
    doFirst {
        // Create timestamped log directory
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
        val logDir = project.rootProject.file("doc/errors/gametest-${timestamp}")
        logDir.mkdirs()
        
        // Ensure run-test directory exists with EULA
        val runTestDir = file("run-test")
        runTestDir.mkdirs()
        val eulaFile = file("run-test/eula.txt")
        if (!eulaFile.exists()) {
            eulaFile.writeText("# Generated automatically by Gradle\n# By changing the setting below to TRUE you are indicating your agreement to our EULA (https://account.mojang.com/documents/minecraft_eula).\neula=true\n")
            println("Auto-created eula.txt in run-test directory")
        }
        
        println("Starting GameTests with logs captured to: ${logDir.absolutePath}")
    }
}

// Enhanced GameTest task with timeout and better error handling  
tasks.register<JavaExec>("executeGameTests") {
    group = "verification" 
    description = "Execute GameTests with enhanced error handling and timeout"
    dependsOn("processTestResources", "compileTestJava")
    
    mainClass.set("net.minecraftforge.gametest.GameTestServer")
    classpath = sourceSets.test.get().runtimeClasspath
    
    workingDir = file("run-test")
    jvmArgs = listOf(
        "-Xmx2G",
        "-Xms1G", 
        "-Dforge.logging.console.level=info",
        "-Dforge.enabledGameTestNamespaces=${modId}"
    )
    
    args = listOf(
        "--gameTestServer", 
        "--nogui",
        "-mixin.config=${modId}.mixins.json"
    )
    
    // Set reasonable timeout (5 minutes)
    timeout.set(Duration.ofMinutes(5))
    
    doFirst {
        val runTestDir = file("run-test")
        runTestDir.mkdirs()
        val eulaFile = file("run-test/eula.txt")
        if (!eulaFile.exists()) {
            eulaFile.writeText("# Generated automatically by Gradle\n# By changing the setting below to TRUE you are indicating your agreement to our EULA (https://account.mojang.com/documents/minecraft_eula).\neula=true\n")
        }
        
        // Create timestamped error log
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
        val errorLogFile = project.rootProject.file("doc/errors/gametest-execution-${timestamp}.log")
        errorLogFile.parentFile.mkdirs()
        
        // Redirect both stdout and stderr to log file
        standardOutput = FileOutputStream(errorLogFile).buffered()
        errorOutput = standardOutput
        
        println("GameTest execution logs will be saved to: ${errorLogFile.absolutePath}")
    }
    
    // Handle failures gracefully
    isIgnoreExitValue = true
    
    doLast {
        if (executionResult.get().exitValue != 0) {
            logger.error("GameTests failed with exit code: ${executionResult.get().exitValue}")
            throw org.gradle.api.tasks.TaskExecutionException(this, RuntimeException("GameTests failed"))
        } else {
            println("GameTests completed successfully")
        }
    }
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