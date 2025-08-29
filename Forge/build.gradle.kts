plugins {
    java
    idea
    id("com.github.johnrengelman.shadow") version "8.1.1"
    // ForgeGradle and other plugins are not accessible due to network restrictions
    // Will need to be configured manually when network access is available
}

// Configure Shadow plugin to shade Epsilon dependency
tasks.shadowJar {
    configurations = listOf(project.configurations.runtimeClasspath.get())
    relocate("com.alcatrazescapee.epsilon", "com.alcatrazescapee.notreepunching.shaded.epsilon")
    archiveClassifier.set("")
    mergeServiceFiles()
}

tasks.jar {
    // Disable the regular jar task as we'll use shadowJar
    enabled = false
    dependsOn(tasks.shadowJar)
}

// When ForgeGradle is available, this configuration should be updated to use:
// 1. ForgeGradle minecraft{} block with enableJarJar()
// 2. implementation(jarJar("com.alcatrazescapee:epsilon:$epsilonVersion"))
// 3. Proper reobfJar integration

val modId: String by project
val minecraftVersion: String by project        // e.g., 1.20.1
val forgeVersion: String by project            // e.g., 47.4.0
val parchmentVersion: String by project        // e.g., 2023.09.03
val parchmentMinecraftVersion: String by project // e.g., 1.20.1
val epsilonVersion: String by project

base { archivesName.set("${modId}-forge-${minecraftVersion}") }

repositories {
    // maven("https://maven.minecraftforge.net") // Not accessible in this environment
    // maven("https://maven.parchmentmc.org") // Not accessible in this environment
    // maven("https://alcatrazescapee.jfrog.io/artifactory/mods") // May not be accessible
    mavenCentral()
    maven("https://repo.spongepowered.org/repository/maven-public/")
}

dependencies {
    // ForgeGradle dependencies would go here when network access is available
    // For now, we'll configure the Shadow plugin to shade Epsilon
    
    // Epsilon dependency - using Shadow plugin to shade into final jar 
    // (Alternative to ForgeGradle JarJar when ForgeGradle is not available)
    implementation("com.alcatrazescapee:epsilon:$epsilonVersion") {
        isTransitive = false
    }

    // Other dependencies as needed
    if (System.getProperty("idea.sync.active") != "true") {
        // Mixin processor would be configured here
        // annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
    }
}

sourceSets {
    main {
        java.srcDir("../Common/src/main/java")
        resources.srcDir("../Common/src/main/resources")
    }
}

// ForgeGradle configuration would go here when network access is restored
// minecraft {
//     mappings("parchment", "$parchmentVersion-$parchmentMinecraftVersion")
//     enableJarJar()
//     runs { ... }
// }

// mixin {
//     add(sourceSets.main.get(), "${modId}.refmap.json")
//     config("${modId}.mixins.json")
//     config("${modId}.common.mixins.json")
// }

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
    // Remove slim classifier to enable reobfJar task generation
    // archiveClassifier.set("slim") - REMOVED
}

// Remove ForgeGradle-specific task configuration
// AfterEvaluate configuration would go here when ForgeGradle is available:
// afterEvaluate {
//     tasks.named("jar") {
//         dependsOn("jarJar")
//         finalizedBy("reobfJar")
//     }
// }

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(17)
}

idea {
    module {
        excludeDirs.addAll(listOf("run", "out", "logs").map(::file))
    }
}