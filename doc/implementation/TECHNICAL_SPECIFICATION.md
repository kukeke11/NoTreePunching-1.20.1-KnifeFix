# Sharp Tool Integration System - Technical Specification

---

## 1. Planning & Discovery

### 1.1 Core Purpose & Success

* **Feature Name**: Tag-Based Sharp Tool Plant Harvesting System
* **Core Purpose**: Replace knife-specific plant harvesting checks with flexible tag-based system enabling automatic cross-mod compatibility while preserving all existing knife functionality
* **Key Principles**: 
  - Preserve existing behavior completely
  - Maximize cross-mod compatibility through standard Forge tags
  - Enable datapack customization without hardcoding
* **Core Problem Analysis**: Current knife system uses hardcoded checks limiting cross-mod compatibility and preventing modpack authors from easily extending plant harvesting to other sharp tools

### 1.2 Essential Features

* **Sharp Tool Detection System**: Any item tagged as sharp tool can harvest plants with drops
* **Plant Classification System**: Unified system using standard Minecraft/Forge tags for plant identification
* **Compatibility Integration Layer**: Existing knife recipes and behavior work identically
* **Event System Integration**: Seamless integration with existing EventHandler architecture

---

## 2. Technical Architecture & Stack

* **Key Libraries**: Minecraft Forge 47.4.0, existing NoTreePunching architecture, Forge Config API
* **Core Systems Involved**: 
  - `KnifeItem.java` - current plant harvesting implementation using `isCorrectToolForDrops()` and `getDestroySpeed()` overrides
  - `ModTags.java` - tag definitions (existing `SHARP_TOOLS` tag already present)
  - `EventHandler.java` - block interaction events with `modifyHarvestCheck()` and `modifyBreakSpeed()`
  - `ToolDamageUtil.java` - centralized tool damage system
* **Data Flow**: Item tag check → Plant block validation → Apply harvest behavior → Handle tool damage via existing ToolDamageUtil
* **Folder Organization**: 
  - Core logic in `Common/src/main/java/com/alcatrazescapee/notreepunching/util/SharpToolUtil.java`
  - Tag definitions in `Common/src/main/java/com/alcatrazescapee/notreepunching/common/ModTags.java`
  - Event handling integration in `EventHandler.java`
  - Data files in `Common/src/main/resources/data/notreepunching/tags/`
* **Naming Conventions**: `SharpToolUtil.java`, tag names follow existing patterns (`sharp_tools`, `requires_sharp_tool`, `plant_fiber_sources`)

---

## 3. Game Mechanics & Feature Breakdown

#### **Feature 1: Sharp Tool Detection System**
* **User Story**: As a player, I want any sharp tool (knife, modded blade, etc.) to harvest plants so that I can use tools from other mods interchangeably
* **Implementation Details**: 
  1. Create `SharpToolUtil.isSharpTool(ItemStack)` method checking `#notreepunching:sharp_tools` tag
  2. Extend existing `KnifeItem.isCorrectToolForDrops()` logic to use tag-based detection
  3. Preserve exact knife behavior by including all NTP knives in sharp_tools tag
  4. Add destroy speed optimization (15.0f) for tagged sharp tools on plant blocks
* **Edge Cases & Error Handling**: 
  - Handle null/empty ItemStack gracefully with early returns
  - Performance optimization with caching for frequently accessed items
  - Fallback to vanilla behavior if tag system fails

#### **Feature 2: Plant Block Classification System**
* **User Story**: As a modpack author, I want to add modded plants to the sharp tool system via datapacks without code changes
* **Implementation Details**:
  1. Create `REQUIRES_SHARP_TOOL` block tag referencing standard Minecraft tags
  2. Include `#minecraft:flowers`, `#minecraft:crops`, `#minecraft:sword_efficient` by reference
  3. Extend existing `PLANT_FIBER_SOURCES` tag for fiber generation
  4. Use tag hierarchy: specific NTP tags → general Minecraft tags → vanilla fallback
* **Edge Cases & Error Handling**:
  - Handle missing tags gracefully with vanilla behavior fallback
  - Validate tag contents during mod loading with error logging
  - Performance caching for frequently accessed tag checks with cache invalidation

#### **Feature 3: Compatibility Integration Layer**  
* **User Story**: As an existing user, I want knife behavior to remain exactly the same while gaining cross-mod compatibility
* **Implementation Details**:
  1. Modify `KnifeItem.java` methods to use `SharpToolUtil` internally while preserving signatures
  2. Maintain exact durability behavior through existing `ToolDamageUtil` integration
  3. Keep all knife-specific recipe functionality unchanged in `ModRecipes.java`
  4. Preserve destroy speed (15.0f) and damage calculation behavior
* **Edge Cases & Error Handling**:
  - Comprehensive testing to ensure behavior parity with existing implementation
  - Version compatibility handling for existing save files and configurations
  - Graceful degradation if sharp tool system fails, falling back to original knife logic

#### **Feature 4: Event System Integration**
* **User Story**: As a developer, I want the sharp tool system to integrate seamlessly with existing event handling
* **Implementation Details**:
  1. Extend `EventHandler.modifyHarvestCheck()` to include sharp tool logic using `SharpToolUtil`
  2. Add sharp tool validation to `EventHandler.modifyBreakSpeed()` for consistent behavior
  3. Preserve existing event priority and cancellation behavior patterns
  4. Add debug logging for sharp tool interactions when config debugging enabled
* **Edge Cases & Error Handling**:
  - Handle event cancellation properly without breaking existing mod compatibility
  - Maintain event ordering compatibility with other mods
  - Error recovery if sharp tool detection fails during events, falling back to vanilla

---

## 4. Data & Assets

* **Tags**: 
  - `data/notreepunching/tags/items/sharp_tools.json` - includes all NTP knives (`flint_knife`, `iron_knife`, etc.) plus cross-mod compatibility references (`#forge:tools/knives`, `#c:knives`)
  - `data/notreepunching/tags/blocks/requires_sharp_tool.json` - references `#minecraft:flowers`, `#minecraft:crops`, `#minecraft:sword_efficient`, includes existing `#notreepunching:plant_fiber_sources`
  - Extend existing `data/notreepunching/tags/blocks/plant_fiber_sources.json` for fiber generation
* **Recipes**: No new recipes needed, existing `tool_damaging_shapeless` recipes in `ModRecipes.java` continue working unchanged
* **Loot Tables**: No changes to loot tables, behavior controlled through `isCorrectToolForDrops()` mechanics in items
* **Configs**: Minimal additions to existing `ForgeConfig.java`:
  - `enableSharpToolSystem` boolean (default: true) - master toggle for entire system
  - `requireSharpToolForPlants` boolean (default: true) - matches current knife requirement behavior
  - Integration with existing `doInstantBreakBlocksDamageKnives` for tool damage behavior
* **Assets**: No new textures, models, or sounds required - uses existing knife behavior patterns

---

## 5. Compatibility & Integration

* **Vanilla Interaction**: 
  - Sharp tools work on vanilla plants tagged with `minecraft:flowers`, `minecraft:crops`, `minecraft:sword_efficient`
  - Non-sharp tools maintain vanilla behavior (break blocks but no drops) via `isCorrectToolForDrops()` checks
  - Shears continue working normally through vanilla mechanics (unaffected by system)
  - Leaves maintain existing vanilla drop behavior completely (no interference)
* **Mixin Requirements**: No new Mixins needed - uses existing event system in `EventHandler.java` and item method overrides in `KnifeItem.java`
* **Mod Compatibility**:
  - Automatic compatibility with mods using standard Forge tags (`forge:tools/knives`, `c:tools/sharp`, etc.)
  - Modded plants work automatically if properly tagged with standard Minecraft plant tags
  - Conflicts unlikely as system extends rather than replaces existing mechanics
  - Integration with existing NoTreePunching recipe system and tool damage mechanics
  - No interference with other tool mods (swords, axes, etc.) - only affects plant harvesting

---

## 6. Implementation Plan & Validation

* **Task Breakdown**:
  1. **Research Phase** (1-2 hours): Analyze existing Forge tag conventions for sharp tools and plants in mod ecosystem
  2. **Create SharpToolUtil** (2-3 hours): Implement core sharp tool detection and plant validation logic with caching
  3. **Extend ModTags** (1 hour): Add new tag definitions and populate with appropriate defaults
  4. **Integrate with KnifeItem** (2-3 hours): Modify existing knife behavior to use new system internally while preserving all functionality
  5. **Event System Integration** (1-2 hours): Extend EventHandler to support sharp tool validation
  6. **Data Generation** (1-2 hours): Create tag data files with proper plant and tool classifications
  7. **Testing Phase** (3-4 hours): Comprehensive testing to ensure behavior parity and cross-mod compatibility

* **Validation Strategy**:
  - Unit testing for `SharpToolUtil` methods with various ItemStack inputs
  - Integration testing comparing old vs new knife behavior on all plant types
  - Cross-mod compatibility testing with popular plant mods (Botania, Mystical Agriculture) and tool mods
  - Performance benchmarks to ensure no regression on vanilla block breaking (target: <1ms overhead)
  - User acceptance testing to verify completely transparent migration from existing system

---

## 7. Implementation Details

### Core Utility Class Design

```java
public final class SharpToolUtil {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    // Cache for performance optimization
    private static final Map<Item, Boolean> SHARP_TOOL_CACHE = new ConcurrentHashMap<>();
    private static final Map<Block, Boolean> PLANT_BLOCK_CACHE = new ConcurrentHashMap<>();
    
    /**
     * Check if an ItemStack is a sharp tool capable of harvesting plants
     */
    public static boolean isSharpTool(ItemStack stack) {
        if (stack.isEmpty()) return false;
        
        return SHARP_TOOL_CACHE.computeIfAbsent(stack.getItem(), item -> 
            Helpers.isItem(item, ModTags.Items.SHARP_TOOLS)
        );
    }
    
    /**
     * Check if a block requires a sharp tool for drops
     */
    public static boolean requiresSharpTool(BlockState state) {
        return PLANT_BLOCK_CACHE.computeIfAbsent(state.getBlock(), block ->
            state.is(ModTags.Blocks.REQUIRES_SHARP_TOOL) || 
            state.is(BlockTags.SWORD_EFFICIENT) ||
            state.is(ModTags.Blocks.PLANT_FIBER_SOURCES)
        );
    }
    
    /**
     * Get destroy speed for sharp tools on applicable blocks
     */
    public static float getDestroySpeed(ItemStack stack, BlockState state) {
        if (isSharpTool(stack) && requiresSharpTool(state)) {
            return 15.0f; // Match existing knife behavior
        }
        return 1.0f; // Default speed
    }
    
    /**
     * Check if tool should take damage when mining block
     */
    public static boolean shouldDamageToolOnPlant(ItemStack stack, BlockState state) {
        return isSharpTool(stack) && requiresSharpTool(state) && 
               Config.INSTANCE.doInstantBreakBlocksDamageKnives.getAsBoolean();
    }
    
    /**
     * Clear caches when tags reload
     */
    public static void clearCache() {
        SHARP_TOOL_CACHE.clear();
        PLANT_BLOCK_CACHE.clear();
    }
}