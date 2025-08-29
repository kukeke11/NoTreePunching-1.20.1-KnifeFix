// Root build script (Forge only)
plugins {
    java
}
val modName: String by extra
val modAuthor: String by extra
val modId: String by extra
val modGroup: String by extra
val modIssueUrl: String by extra
val modHomeUrl: String by extra
val modDescription: String by extra
val modJavaVersion: String by extra

val minecraftVersion: String by extra
val minecraftVersionRange: String by extra
val forgeVersion: String by extra
val forgeVersionRange: String by extra
val parchmentVersion: String by extra
val parchmentMinecraftVersion: String by extra
val epsilonVersion: String by extra

val modVersion: String = System.getenv("VERSION") ?: "0.0.0-indev"

subprojects {
    version = modVersion
    group = modGroup

    plugins.withId("java") {
        extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(modJavaVersion))
            }
            withSourcesJar()
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.withType<Jar> {
        manifest {
            attributes(
                "Implementation-Title" to modName,
                "Implementation-Version" to modVersion,
                "Implementation-Vendor" to modAuthor
            )
        }
    }

    tasks.withType<ProcessResources> {
        filesMatching(listOf("META-INF/mods.toml", "pack.mcmeta")) {
            expand(
                mapOf(
                    "modName" to modName,
                    "modAuthor" to modAuthor,
                    "modId" to modId,
                    "modGroup" to modGroup,
                    "modIssueUrl" to modIssueUrl,
                    "modHomeUrl" to modHomeUrl,
                    "modDescription" to modDescription,
                    "modJavaVersion" to modJavaVersion,
                    "modVersion" to modVersion,
                    "minecraftVersion" to minecraftVersion,
                    "minecraftVersionRange" to minecraftVersionRange,
                    "forgeVersion" to forgeVersion,
                    "forgeVersionRange" to forgeVersionRange
                )
            )
        }
    }

    tasks.withType<GenerateModuleMetadata> {
        enabled = false
    }
}