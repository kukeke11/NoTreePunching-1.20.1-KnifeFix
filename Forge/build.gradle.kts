plugins {
    java
    idea
    id("net.minecraftforge.gradle") version "6.+"
    id("org.spongepowered.mixin") version "0.7-SNAPSHOT"
    id("org.parchmentmc.librarian.forgegradle") version "1.+"
    // Keep Shadow only if you actually shade something:
    // id("com.github.johnrengelman.shadow") version "8.1.1"
}

val modId: String by project
val minecraftVersion: String by project        // e.g., 1.20.1
val forgeVersion: String by project            // e.g., 47.4.0
val parchmentVersion: String by project        // e.g., 2023.09.03
val parchmentMinecraftVersion: String by project // e.g., 1.20.1
val epsilonVersion: String by project

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

    // Epsilon on dev classpath, not bundled
    "minecraftLibrary"("com.alcatrazescapee:epsilon:$epsilonVersion") {
        isTransitive = false
    }

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
    // keep your reobf flow
    archiveClassifier.set("slim")
    finalizedBy("reobfJar")
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