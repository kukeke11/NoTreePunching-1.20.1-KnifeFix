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
}

minecraft {
    // Correct order: date-first then MC version (e.g., 2023.09.03-1.20.1)
    mappings("parchment", "$parchmentVersion-$parchmentMinecraftVersion")

    runs {
        create("client") {
            workingDirectory(file("run"))
            arg("-mixin.config=$modId.mixins.json")
            
            // Enhanced logging for agents - can be overridden via system properties
            property("forge.logging.console.level", System.getProperty("forge.logging.level", "info"))
            property("forge.logging.markers", "REGISTRIES")
            
            // Debug flags for better error visibility
            if (System.getProperty("mixin.debug") == "true") {
                property("mixin.debug.export", "true")
                property("mixin.debug.verbose", "true")
            }
            
            // Performance monitoring for automation
            if (System.getProperty("client.monitor.performance") == "true") {
                jvmArgs("-XX:+PrintGCDetails", "-XX:+PrintGCTimeStamps", "-XX:+PrintGCApplicationStoppedTime")
            }
            
            // Remote debugging support (disabled by default for security)
            if (System.getProperty("debug.client") == "true") {
                jvmArgs("-Xdebug", "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005")
            }
            
            // Memory management - configurable via system properties
            val clientMemory = System.getProperty("client.memory", "2G")
            val initialMemory = System.getProperty("client.memory.initial", "1G")
            jvmArgs("-Xmx$clientMemory", "-Xms$initialMemory")
            
            // Garbage collection tuning for better performance in automation
            jvmArgs("-XX:+UseG1GC", "-XX:+ParallelRefProcEnabled", "-XX:MaxGCPauseMillis=200")
            
            // JVM options for headless and CI environments
            if (System.getProperty("headless.mode") == "true") {
                jvmArgs("-Djava.awt.headless=false") // Keep false for Minecraft GUI components
                jvmArgs("-Dorg.lwjgl.system.allocator=system")
            }
            
            // CI-friendly options
            if (System.getProperty("ci.mode") == "true") {
                jvmArgs("-XX:+HeapDumpOnOutOfMemoryError")
                jvmArgs("-XX:HeapDumpPath=${project.buildDir}/heap-dumps/")
                property("forge.logging.console.level", "info")
                property("mixin.checks", "false") // Skip some expensive checks in CI
            }
            
            // Agent-specific system properties
            if (System.getProperty("agent.mode") == "true") {
                // Disable some interactive features that might hang in automation
                property("fml.earlyprogresswindow", "false")
                jvmArgs("-Djava.awt.headless=false", "-Dnet.minecraftforge.fml.loading.FMLLoader.EARLY_WINDOW=false")
            }
            
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
            
            // Enhanced server logging
            property("forge.logging.console.level", System.getProperty("forge.logging.level", "info"))
            
            // Server-specific memory settings
            val serverMemory = System.getProperty("server.memory", "1G")
            jvmArgs("-Xmx$serverMemory", "-Xms${serverMemory}")
            
            mods {
                create(modId) {
                    source(sourceSets.main.get())
                }
            }
        }
        
        // Additional run configuration for automated testing
        create("clientTest") {
            workingDirectory(file("run"))
            arg("-mixin.config=$modId.mixins.json")
            
            // Test-optimized settings
            property("forge.logging.console.level", "warn") // Reduce noise in tests
            property("fml.earlyprogresswindow", "false")
            
            // Faster startup for testing
            jvmArgs("-Xmx2G", "-Xms1G", "-XX:+UseG1GC")
            jvmArgs("-Dfml.readTimeout=180") // Increase timeout for slow CI systems
            
            // Test-specific properties
            property("test.mode", "true")
            
            mods {
                create(modId) {
                    source(sourceSets.main.get())
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

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(17)
}

// Create convenience tasks for different run modes
tasks.register("runClientHeadless") {
    group = "forgegradle runs"
    description = "Run the client in headless mode suitable for CI/automation"
    dependsOn("classes")
    doLast {
        exec {
            commandLine("./gradlew", "runClient", 
                "-Dheadless.mode=true", 
                "-Dci.mode=true", 
                "-Dagent.mode=true")
        }
    }
}

tasks.register("runClientDebug") {
    group = "forgegradle runs"
    description = "Run the client with debug logging and remote debugging enabled"
    dependsOn("classes") 
    doLast {
        exec {
            commandLine("./gradlew", "runClient",
                "-Dforge.logging.level=debug",
                "-Dmixin.debug=true", 
                "-Ddebug.client=true")
        }
    }
}

idea {
    module {
        excludeDirs.addAll(listOf("run", "out", "logs").map(::file))
    }
}