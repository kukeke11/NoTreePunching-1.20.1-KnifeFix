pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.minecraftforge.net")
        maven("https://maven.parchmentmc.org")
        maven("https://repo.spongepowered.org/repository/maven-public/")
        maven("https://alcatrazescapee.jfrog.io/artifactory/mods")
        mavenCentral()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "org.spongepowered.mixin") {
                useModule("org.spongepowered:mixingradle:${requested.version}")
            }
        }
    }
}

rootProject.name = "NoTreePunching-1.20"
include("Forge")

// Note: Epsilon dependency will be resolved from the remote repository 
// and shaded into the final jar using ForgeGradle's JarJar feature