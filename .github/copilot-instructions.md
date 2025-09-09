# No Tree Punching - Coding Agent Instructions
Always use: agentic-tools, sequential-thinking and context7
# Agent policy for Agentic Tools storage
- Always call `agentic-tools` with `workingDirectory` set to the repo root **absolute path** (e.g., `$GITHUB_WORKSPACE`), not `"."`.
- Read/write only in `REPO_ROOT/.agentic-tools-mcp/`;
- After any change under `.agentic-tools-mcp/`, stage & commit those files with the current issue in the message, and include them in the same PR as code changes.

## Repository Overview
**No Tree Punching** is a Minecraft Forge mod for version 1.20.1 that adds realistic tool requirements
**Key Technologies:** Java 17, Minecraft Forge 47.4.0, Gradle 8.1.1, Parchment mappings, Mixin framework
**Project Size:** ~84 Java files, multi-module architecture (Common + Forge)

## Build Instructions

### Prerequisites & Environment Setup
**Required:** Java 17 (OpenJDK Temurin recommended), Git, Internet connection
**Always verify Java version first:** `java --version` (must show Java 17)

### Primary Build Method (Recommended - TESTED)
**Execute from repository root:**

```bash
# 1. Clean previous build artifacts
./gradlew clean

# 2. Build the Forge module (includes all dependencies)
./gradlew Forge:build

# 3. Verify build artifacts created
ls -la Forge/build/libs/
# Should contain: notreepunching-forge-{version}.jar
```

**Build timing:** 3-5 minutes for initial setup, 1-2 minutes for subsequent builds

### Alternative Build Commands
```bash
# Compile Java sources only
./gradlew Forge:compileJava

# Process resources and metadata  
./gradlew Forge:processResources

# Create mod jar file
./gradlew Forge:jar

# Full build with all checks
./gradlew Forge:build
```

### Troubleshooting Failed Builds
**If the primary method fails, use this advanced approach:**

1. **Clear ForgeGradle caches:**
   ```bash
   rm -rf ~/.gradle/caches/forge_gradle
   rm -rf ~/.gradle/caches/minecraft
   ./gradlew clean
   ```

2. **Build with memory optimization:**
   ```bash
   export GRADLE_OPTS="-Xmx4G -Xms1G"
   ./gradlew --refresh-dependencies build --no-daemon
   ```

3. **If still failing, full cache purge:**
   ```bash
   rm -rf ~/.gradle/caches/
   export GRADLE_OPTS="-Xmx4G -Xms1G"  
   ./gradlew --refresh-dependencies build --no-daemon --stacktrace
   ```

### Memory Configuration
**Option 1:** Export in shell (temporary)
```bash
export GRADLE_OPTS="-Xmx4G -Xms1G"
```

**Option 2:** Configure in `gradle.properties` (persistent)
```properties
org.gradle.jvmargs=-Xmx4G -Xms1G
org.gradle.daemon=true
```

### Development Tasks & Validation

**Development client/server:**
```bash
# Launch Minecraft server with mod loaded
./gradlew Forge:runServer

# Run GameTests for harvesting mechanics validation
./gradlew Forge:runGameTestServer
```

### Critical Validation After Java Changes
**After any Java code changes, ALWAYS run:**
```bash
./gradlew Forge:runGameTestServer
```
- Must load to main menu without crashes (local development)
- Check console for mod loading errors during startup
- Required even for minor changes to verify mod loads correctly

**Expected behavior in different environments:**
- **Local development:** Should launch Minecraft successfully


**Alternative validation for CI/headless environments:**
```bash
# Compile-only validation
./gradlew Forge:compileJava
./gradlew Forge:processResources

# Or run full build to verify everything works
./gradlew Forge:build

# Run functional tests (GameTests) - headless compatible
./gradlew Forge:runGameTestServer
```

### GameTest System for Harvesting Mechanics

**Running GameTests:**
```bash
# Execute comprehensive harvesting mechanics tests
./gradlew Forge:runGameTestServer
```

**GameTest Coverage:**
- **Sharp Tool vs Hand Testing:** Validates knife/hand behavior on flowers and grass
- **Drop Logic Verification:** Ensures correct item drops (flowers, plant_fiber) with knives
- **No-Drop Validation:** Confirms no drops when using bare hands  
- **Performance Regression Detection:** Batch testing (5x5 area) for timing observation
- **Configuration Respect:** Tests that system respects enabled/disabled states

**Success/Failure Conditions:**
- **PASS:** All GameTests complete without assertion failures
- **FAIL:** Any test fails assertion (wrong drops, incorrect tool behavior)
- **PERFORMANCE WARNING:** Batch test exceeds 5-second threshold (regression indicator)

**Note:** GameTests are for *functional* validation of harvesting mechanics. For in-depth performance analysis, use dedicated profilers like JProfiler or async-profiler.

### Build Warnings (Safe to Ignore)
- `[removal]` warnings about deprecated Forge APIs - planned migration items
- `MixinGradle is skipping eclipse integration` - IDE integration warning  
- `[Fatal Error] client-1.20.1.pom:2:10: Already seen doctype` - Maven parsing issue

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

## Tested and Validated Instructions
These instructions have been comprehensively tested and validated on the current codebase. All build commands, cache clearing procedures, and validation steps have been verified to work. Only search for additional information if:
1. Commands fail with errors not covered in the troubleshooting section above
2. Project structure differs from documented layout after future updates
3. New build requirements are introduced by dependency updates

**IMPORTANT:** Always start with cache clearing and memory optimization for reliable ForgeGradle builds.
