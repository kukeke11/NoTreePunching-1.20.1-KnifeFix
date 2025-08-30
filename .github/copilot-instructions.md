# No Tree Punching - Coding Agent Instructions

## Repository Overview
**No Tree Punching** is a Minecraft Forge mod for version 1.20.1 that adds realistic tool requirements - blocks only drop items when broken with correct tools. The mod includes progression features like flint knapping, pottery, fire starting, and specialized tools (knives, saws, mattocks). This is a recently refactored codebase (84 Java files) that replaced deprecated Epsilon config system with modern Forge Config API.

**Key Technologies:** Java 17, Minecraft Forge 47.4.0, Gradle 8.1.1, Parchment mappings, Mixin framework
**Project Size:** ~84 Java files, multi-module architecture (Common + Forge)

## Critical Build Instructions

### Prerequisites & Environment Setup
**Required:** Java 17 (OpenJDK Temurin recommended), Git
**Always verify Java version first:** `java --version` (must show Java 17)

### Build Commands (Execute in Project Root)
**CRITICAL: Always run commands from repository root, not from submodules**

```bash
# Clean build (use if encountering issues)
./gradlew clean

# Main build (includes jar generation and reobfuscation)
./gradlew build

# Development client (required test after Java code changes)
./gradlew :Forge:runClient

# Development server
./gradlew :Forge:runServer
```

### Build Timing & Common Issues
- **Initial setup:** 2-5 minutes (downloads dependencies)
- **Regular builds:** 30-60 seconds
- **runClient startup:** 1-2 minutes (loads Minecraft)

**Common Build Failures & Solutions:**
1. **"Could not find net.minecraftforge:forge" or "zip END header not found"**
   ```bash
   # Clear corrupted ForgeGradle caches
   rm -rf ~/.gradle/caches/forge_gradle
   rm -rf ~/.gradle/caches/minecraft
   ./gradlew --refresh-dependencies clean
   ./gradlew :Forge:build
   ```

2. **Dependency resolution timeouts:**
   ```bash
   # Retry with offline mode disabled
   ./gradlew --refresh-dependencies build --no-daemon
   ```

3. **Out of memory errors:**
   ```bash
   export GRADLE_OPTS="-Xmx4G -Xms1G"
   ./gradlew build
   ```

**First-time setup expects failures** - ForgeGradle downloads and processes large amounts of data.

**Environment Dependencies:**
- **Local development:** May require multiple attempts due to ForgeGradle's complex dependency resolution
- **CI environments:** Build success depends on network stability and cache state
- **Build verification:** Use GitHub Actions workflow for reliable builds: `.github/workflows/build.yml`

**Working build verification:**
```bash
# If local build fails, check GitHub Actions build logs
# The workflow is designed to handle ForgeGradle issues better
```

### Validation Requirements
**After any Java code changes, ALWAYS run:**
```bash
# Critical test - must complete without crashes
./gradlew :Forge:runClient
```
- Must load to main menu without crashes
- Check console for mod loading errors  
- Required even for minor changes
- **Note:** In CI/remote environments, runClient may fail due to display requirements

**Alternative validation for CI environments:**
```bash
# Compile-only validation when runClient unavailable
./gradlew :Forge:compileJava
./gradlew :Forge:processResources
```

**Build artifact verification:**
```bash
# Check successful build output
ls -la Forge/build/libs/
# Should contain: notreepunching-forge-{version}.jar
```

## Project Architecture & Layout

### Module Structure
```
/                           # Root build configuration
├── Common/                 # Platform-agnostic code
│   └── src/main/java/com/alcatrazescapee/notreepunching/
│       ├── NoTreePunching.java        # Main mod class
│       ├── Config.java                # Config compatibility layer  
│       ├── ForgeConfig.java          # New Forge config implementation
│       ├── EventHandler.java         # Cross-platform event handling
│       ├── common/                   # Core game mechanics
│       │   ├── blocks/ModBlocks.java # Block registration
│       │   ├── items/ModItems.java   # Item registration  
│       │   ├── recipes/              # Recipe system & injection
│       │   └── blockentity/          # Block entity implementations
│       ├── mixin/                    # Mixin injections for vanilla changes
│       ├── client/                   # Client-side code
│       └── util/                     # Helper utilities
└── Forge/                  # Forge-specific implementations
    ├── src/main/java/      # Forge platform code
    └── src/main/resources/ # Mod metadata, mixins config
```

### Key Configuration Files
- **gradle.properties:** Mod metadata, versions, memory settings
- **Common/src/main/resources/notreepunching.common.mixins.json:** Mixin configurations
- **Forge/src/main/resources/META-INF/mods.toml:** Forge mod manifest

### GitHub Workflows
- **Manual Build (workflow_dispatch):** `.github/workflows/build.yml`
- Produces forge jar in `Forge/build/libs/`
- Artifact naming: `notreepunching-forge-{version}.jar`

## Code Change Guidelines

### Where to Make Changes
- **Core logic:** `Common/src/main/java/com/alcatrazescapee/notreepunching/common/`
- **Client features:** `Common/src/main/java/com/alcatrazescapee/notreepunching/client/`
- **Configuration:** `ForgeConfig.java` (new system) or `Config.java` (compatibility)
- **Forge-specific:** `Forge/src/main/java/` (platform implementations only)

### Architecture Notes
- **Cross-platform design:** Common module contains shared logic, Forge implements platform specifics
- **Registry system:** Uses deferred registries via `XPlatform` interface
- **Configuration:** Dual system - new ForgeConfig.java with Config.java compatibility layer
- **Recipe injection:** Safe event-based system (no unsafe mixins)
- **Tool damage:** Centralized in utility classes

### Recent Major Changes (From COMPREHENSIVE_FIX_SUMMARY.md)
- Removed Epsilon dependency, replaced with Forge Config API
- Improved memory management and performance optimizations
- Centralized tool damage system with proper error handling
- Enhanced block modification system with safety checks

### Build Output Location
**Development:** `Forge/build/libs/notreepunching-forge-{version}.jar`
**CI Build:** Artifacts uploaded as `notreepunching-forge.jar`

### Dependencies
- **No external dependencies** (removed Epsilon/JarJar)
- **Uses standard Forge APIs** for maximum compatibility
- **Parchment mappings** for readable code

## Trust These Instructions
These instructions are comprehensive and validated. Only search for additional information if:
1. Commands fail with errors not listed above
2. Project structure differs from documented layout
3. New build requirements are introduced

For any build issues, first try cleaning (gradle clean) and verify Java 17 before exploring further.