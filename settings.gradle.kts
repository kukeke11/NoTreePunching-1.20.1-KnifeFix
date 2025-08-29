pluginManagement {
    repositories {
        fun exclusiveMaven(url: String, filter: Action<InclusiveRepositoryContentDescriptor>) =
            exclusiveContent {
                forRepository { maven(url) }
                filter(filter)
            }

        exclusiveMaven("https://maven.minecraftforge.net") {
            includeGroupByRegex("net\\.minecraftforge.*")
        }
        exclusiveMaven("https://maven.parchmentmc.org") {
            includeGroupByRegex("org\\.parchmentmc.*")
        }
        exclusiveMaven("https://repo.spongepowered.org/repository/maven-public/") {
            includeGroupByRegex("org\\.spongepowered.*")
        }
        exclusiveMaven("https://alcatrazescapee.jfrog.io/artifactory/mods") {
            includeGroupByRegex("com\\.alcatrazescapee.*")
        }
        gradlePluginPortal()
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

includeBuild("C:\\Users\\Martin\\Downloads\\New folder\\Epsilon-0.5") {
    dependencySubstitution {
        // If Epsilon is a single-module build (root project publishes the artifact):
        substitute(module("com.alcatrazescapee:epsilon"))
            .using(project(":"))
    }
}