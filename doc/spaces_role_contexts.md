# Copilot Spaces Role Context Boundaries

This document defines which files and folders should be included as context for each Copilot Spaces role in the No Tree Punching mod repository. Each role has specific goals and requires different levels of access to the codebase.

## Repository Overview

**No Tree Punching** is a Minecraft Forge mod for version 1.20.1 that adds realistic tool requirements. The repository contains:
- **~83 Java files** across multi-module architecture (Common + Forge)
- **Build system:** Gradle with Forge toolchain
- **Documentation:** Comprehensive technical and user docs
- **Data generation:** Python scripts for assets/recipes
- **Project size:** Recently refactored codebase with modern Forge Config API

## Role Definitions

### 1. Project Master

**Goal:** Full overview of the project, general info, deep understanding of all parts.

**Context:** **ALL** folders and files in the repository should be included.

**Justification:** The Project Master needs complete access to understand every aspect of the project, from high-level concepts to implementation details, build configuration, and technical documentation.

**Included:**
```
/ (everything)
├── .github/                    # CI/CD workflows, repo configuration
├── Common/                     # Platform-agnostic mod code
├── Forge/                      # Forge-specific implementations
├── Data/                       # Data generation scripts
├── doc/                        # All documentation
├── gradle/                     # Build system configuration
├── img/                        # Project images
├── *.md                        # All markdown documentation
├── *.gradle.kts               # Build scripts
├── *.properties               # Configuration files
└── All other files and folders
```

---

### 2. Idea Refiner

**Goal:** Collaborate on developing or refining project/feature concepts. Helps create and refine PRDs (Product Requirement Documents) based on rough ideas.

**Context:**
- High-level documentation
- Existing feature/idea documentation
- Conceptual design documents
- **Exclude detailed code** unless specifically needed for PRD refinement

**Included:**
```
/
├── README.md                           # Mod overview, features, user perspective
├── COMPREHENSIVE_FIX_SUMMARY.md        # High-level technical changes summary
├── CONFIG_MIGRATION.md                 # Configuration system overview
├── LICENSE                             # Project licensing
├── doc/
│   ├── spaces_role_contexts.md         # This document
│   ├── Mod Design Analysis.md.txt      # Detailed technical design concepts
│   └── *.md                           # Any future concept/design docs
├── img/                               # Visual assets for understanding mod features
└── gradle.properties                  # High-level project metadata only
```

**Excluded:**
- All Java source code (`/Common/src/`, `/Forge/src/`)
- Build scripts and detailed configuration
- Data generation scripts
- Resource files (unless needed for specific PRD work)

**Justification:** The Idea Refiner focuses on conceptual work and needs to understand the mod's purpose, existing features, and design philosophy without getting bogged down in implementation details. The excluded technical files would create noise when working on high-level feature concepts and PRDs.

---

### 3. Architect

**Goal:** Create comprehensive technical specifications based on the PRD/project request.

**Context:**
- All docs accessible to Idea Refiner
- Technical specifications and architecture docs
- Top-level code structure and build configuration
- Interfaces and API documentation
- Core implementation summaries (not all code details)

**Included:**
```
/
├── README.md                           # Full context from Idea Refiner
├── COMPREHENSIVE_FIX_SUMMARY.md        # Technical changes and architecture
├── CONFIG_MIGRATION.md                 # System architecture changes
├── LICENSE                             
├── doc/                               # All documentation
├── img/                               # Visual context
├── gradle.properties                  # Project configuration
├── build.gradle.kts                   # Build architecture
├── settings.gradle.kts                # Multi-module structure
├── Common/
│   ├── src/main/java/com/alcatrazescapee/notreepunching/
│   │   ├── NoTreePunching.java        # Main mod class
│   │   ├── Config.java                # Configuration interface
│   │   ├── ForgeConfig.java           # Configuration implementation
│   │   ├── EventHandler.java          # Core event handling
│   │   ├── platform/                  # Cross-platform abstractions
│   │   │   └── XPlatform.java         # Platform interface
│   │   └── common/
│   │       ├── blocks/ModBlocks.java  # Block registry structure
│   │       ├── items/ModItems.java    # Item registry structure  
│   │       └── recipes/               # Recipe system architecture
│   └── src/main/resources/
│       ├── *.mixins.json              # Mixin configuration
│       └── META-INF/                  # Mod metadata
├── Forge/
│   ├── src/main/java/com/alcatrazescapee/notreepunching/
│   │   ├── ForgeNoTreePunching.java   # Forge entry point
│   │   └── platform/ForgePlatform.java # Platform implementation
│   └── src/main/resources/META-INF/mods.toml # Forge mod manifest
└── Data/main.py                       # Data generation overview
```

**Excluded:**
- Detailed implementation of individual recipes, blocks, items
- Asset files (textures, models, sounds) unless architecturally relevant
- Language files and localization
- Generated data files

**Justification:** The Architect needs to understand the overall system architecture, key interfaces, module boundaries, and build structure to create technical specifications. They need enough implementation context to understand patterns but not every detail. Full source code would be overwhelming and unnecessary for architectural decisions.

---

### 4. Implementation Planner

**Goal:** Create comprehensive, step-by-step implementation plans for building features based on PRD and Technical Specification.

**Context:**
- All docs accessible to Architect
- Implementation planning documentation
- Build scripts and development workflow
- Directory and file structure
- Key entry point code files and class names

**Included:**
```
/
├── All files from Architect role        # Complete architectural context
├── .github/workflows/                   # CI/CD for implementation planning
├── Common/src/main/java/com/alcatrazescapee/notreepunching/
│   ├── All architectural files from above
│   ├── util/                           # Utility classes and helpers
│   ├── client/                         # Client-side entry points
│   ├── mixin/                          # Mixin implementations (entry points)
│   └── common/
│       ├── blockentity/               # Block entity implementations
│       │   ├── ModBlockEntities.java  # Registry patterns
│       │   └── ModBlockEntity.java    # Base class patterns
│       ├── items/                     # Item implementation patterns
│       │   ├── ModItems.java          # Registry structure
│       │   ├── FlintToolItem.java     # Tool implementation patterns
│       │   └── KnifeItem.java         # Core tool example
│       ├── blocks/                    # Block implementation patterns
│       │   ├── ModBlocks.java         # Registry structure  
│       │   └── PotteryBlock.java      # Complex block example
│       └── recipes/                   # Recipe system details
├── Forge/src/main/java/               # All Forge implementations
├── gradle/                            # Complete build system
├── gradlew, gradlew.bat              # Build execution
└── Data/                             # Data generation for implementation
```

**Excluded:**
- Asset files (textures, models, sounds) unless they impact implementation structure
- Language files and localization (unless implementing i18n features)
- IDE-specific files (.idea/)

**Justification:** The Implementation Planner needs comprehensive access to understand existing patterns, entry points, and implementation approaches to create detailed step-by-step plans. They need to see how similar features are implemented, understand the build process, and know the exact file structure for planning new implementations. Unlike other roles, they need deeper code access but still don't need every asset file or localization detail.

---

## Maintenance Guidelines

**Updating Context Boundaries:**
1. **When adding new top-level documentation:** Include in all roles
2. **When adding new Java packages:** Include package structure in Architect, full implementation in Implementation Planner
3. **When adding new build tools:** Include in Architect and Implementation Planner
4. **When adding new asset types:** Generally exclude unless architecturally significant

**Role Evolution:**
- **Idea Refiner:** May need sample implementations added if PRD work becomes more technical
- **Architect:** May need deeper implementation access if architecture decisions require understanding implementation patterns
- **Implementation Planner:** May need additional utility files as codebase grows

**File Type Guidelines:**
- **Documentation (.md, .txt):** Include in all roles unless purely technical implementation
- **Configuration (.properties, .toml, .json):** Include in Architect+ unless asset-specific
- **Source Code (.java):** Architect gets interfaces/key classes, Implementation Planner gets full access
- **Assets (textures, sounds, models):** Generally exclude unless needed for specific feature work
- **Build files:** Include in Architect+ for understanding system structure

## Future Considerations

As the codebase evolves, consider:
1. **API Documentation:** If external APIs are added, include in Architect+
2. **Test Files:** May need inclusion in Implementation Planner if TDD approaches are adopted
3. **Modpack Integration Docs:** Would be relevant for Idea Refiner if community features expand
4. **Performance Profiling Data:** Could be relevant for Architect if optimization becomes a focus

This document should be reviewed and updated whenever major structural changes are made to the repository or when role requirements evolve.