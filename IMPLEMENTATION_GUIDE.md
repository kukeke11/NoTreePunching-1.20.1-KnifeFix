# No Tree Punching - Implementation Guide

## Overview

This implementation guide provides comprehensive documentation for understanding and working with the modernized NoTreePunching codebase. The mod has undergone significant refactoring to replace deprecated systems with modern, safe, and performant implementations.

**For technical details about the modernization process, see [COMPREHENSIVE_FIX_SUMMARY.md](./COMPREHENSIVE_FIX_SUMMARY.md)**

**For user configuration migration information, see [CONFIG_MIGRATION.md](./CONFIG_MIGRATION.md)**

## Architecture

### Configuration System
The mod now uses **Forge Config API** with TOML format, replacing the deprecated Epsilon config system:

- **Native Integration**: Uses Forge's built-in config system for better compatibility
- **TOML Format**: Standard configuration format with proper validation
- **Caching Layer**: Performance-optimized with automatic cache invalidation
- **Backward Compatibility**: Maintains same interface through compatibility layer

### Build System
- **Forge-Only**: Simplified build targeting Forge platform exclusively
- **Modern Dependencies**: Eliminated deprecated Epsilon and JarJar shading
- **Single Output**: Clean distributable jar without dependency conflicts
- **Standard Structure**: Follows modern Forge project conventions

### Recipe System
- **Safe Injection**: Event-based recipe injection using proper Forge events
- **Error Handling**: Comprehensive validation and recovery mechanisms
- **Performance Optimized**: Efficient recipe generation with caching
- **Compatibility Safe**: No direct RecipeManager mutation

## Key Components Documentation

### Configuration System

#### ForgeConfig.java
Native Forge config implementation with performance optimizations:

```java
/**
 * Forge Config API replacement for Epsilon config system.
 * Maintains same config structure and variable names for user compatibility.
 */
public final class ForgeConfig {
    // Config value definitions with validation
    public static final ForgeConfigSpec.BooleanValue enableDynamicRecipeReplacement;
    public static final ForgeConfigSpec.BooleanValue enableLooseRocksWorldGen;
    // ... other config values
    
    // Performance caching
    private static volatile List<Block> cachedPotteryBlocks = null;
    
    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, spec, NoTreePunching.MOD_ID + ".toml");
    }
    
    public static void clearCache() {
        // Invalidate caches when config reloads
    }
}
```

#### Config.java
Compatibility layer maintaining original Epsilon interface:

```java
/**
 * Compatibility layer that maintains the same interface as the original Epsilon config
 * while using Forge Config API under the hood.
 */
public enum Config {
    INSTANCE;
    
    // Wrapped config values maintaining original interface
    public final ConfigValue<Boolean> enableDynamicRecipeReplacement = 
        new ConfigValue<>(() -> ForgeConfig.enableDynamicRecipeReplacement.get());
    
    // ConfigValue wrapper maintains backward compatibility
    public static class ConfigValue<T> {
        public T get() { return supplier.get(); }
        public boolean getAsBoolean() { /* type-safe conversion */ }
        public float getAsFloat() { /* type-safe conversion */ }
    }
}
```

### Tool System

#### ToolDamageUtil.java
Centralized tool damage logic with proper validation:

```java
/**
 * Centralized tool damage logic with proper validation and error handling.
 * Replaces scattered tool damage code throughout the mod.
 */
public final class ToolDamageUtil {
    
    public static boolean damageToolSafely(ItemStack stack, LivingEntity entity, int amount, InteractionHand hand) {
        if (stack.isEmpty() || !stack.isDamageableItem()) return false;
        
        try {
            if (hand != null && entity instanceof Player) {
                stack.hurtAndBreak(amount, entity, livingEntity -> 
                    livingEntity.broadcastBreakEvent(hand));
            } else {
                return damageToolWithoutEntity(stack, amount);
            }
            return !stack.isEmpty();
        } catch (Exception e) {
            // Log error but don't crash
            return false;
        }
    }
    
    public static int calculateToolDamage(ItemStack stack, ToolUsage usage) {
        // Usage-based damage calculation
        return switch (usage) {
            case BLOCK_MINING -> 1;
            case CRAFTING -> 1;
            case ENTITY_INTERACTION -> 1;
            case SPECIAL_ABILITY -> 2;
        };
    }
    
    public enum ToolUsage {
        BLOCK_MINING, CRAFTING, ENTITY_INTERACTION, SPECIAL_ABILITY
    }
}
```

### Recipe System

#### RecipeInjectionHandler.java
Safe recipe injection using proper Forge events:

```java
/**
 * Safe recipe injection system using proper Forge events instead of unsafe mixin approach.
 * Replaces the dangerous direct RecipeManager mutation with event-based injection.
 */
@Mod.EventBusSubscriber(modid = NoTreePunching.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RecipeInjectionHandler {
    
    public static void injectRecipes(RecipeManager recipeManager, RegistryAccess registryAccess) {
        if (!Config.INSTANCE.enableDynamicRecipeReplacement.getAsBoolean()) {
            return;
        }
        
        try {
            // Safe recipe generation with comprehensive error handling
            final Set<Item> logItems = new HashSet<>();
            final Set<Item> plankItems = new HashSet<>();
            
            // Safely gather items from tags with validation
            // Generate recipes with proper error handling
            // Track injected recipes for debugging
        } catch (Exception e) {
            LOGGER.error("Failed to inject recipes", e);
        }
    }
    
    private static Recipe<?> createSawLogToPlankRecipe(ResourceLocation id, TagKey<Item> saw, 
                                                      Ingredient log, Item plank, int count) {
        try {
            return XPlatform.INSTANCE.shapedToolDamagingRecipe(/* ... */);
        } catch (Exception e) {
            LOGGER.error("Failed to create saw recipe with ID {}", id, e);
            throw e;
        }
    }
}
```

### Block System

#### HarvestBlockHandler.java
Selective harvest requirements with namespace filtering:

```java
public final class HarvestBlockHandler {
    
    public static void setup() {
        // Performance optimization with early filtering
        final List<Block> blocksToProcess = new ArrayList<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            final ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
            
            if (shouldModifyBlock(blockId)) {
                blocksToProcess.add(block);
            }
        }
        
        // Process only filtered blocks for safety
        for (Block block : blocksToProcess) {
            try {
                if (processBlock(block)) {
                    // Successfully modified block
                }
            } catch (Exception e) {
                LOGGER.error("Failed to modify block", e);
            }
        }
    }
    
    private static boolean shouldModifyBlock(ResourceLocation blockId) {
        final String namespace = blockId.getNamespace();
        
        // Only modify vanilla blocks and our own blocks
        return "minecraft".equals(namespace) || NoTreePunching.MOD_ID.equals(namespace);
    }
}
```

## File Structure Documentation

```
Common/src/main/java/com/alcatrazescapee/notreepunching/
├── Config.java                      # Compatibility layer for Forge config
├── ForgeConfig.java                 # Native Forge config implementation  
├── NoTreePunching.java              # Main mod class
├── EventHandler.java                # Game event handling
├── common/
│   ├── items/
│   │   └── KnifeItem.java           # Uses centralized ToolDamageUtil
│   ├── recipes/
│   │   ├── ModRecipes.java          # Recipe registration
│   │   ├── RecipeInjectionHandler.java  # Safe event-based recipe injection
│   │   └── ToolDamagingRecipe.java  # Enhanced error handling
│   └── blocks/                      # Block mechanics and definitions
├── util/
│   ├── ToolDamageUtil.java         # Centralized tool damage logic
│   ├── Helpers.java                # Safe utilities with proper casting
│   └── HarvestBlockHandler.java    # Selective block modification
├── mixin/                          # Minecraft integration mixins
├── world/                          # World generation features  
└── client/                         # Client-side functionality
```

## Implementation Workflow

### Understanding Requests
1. **Review Technical Documentation**: Start with [COMPREHENSIVE_FIX_SUMMARY.md](./COMPREHENSIVE_FIX_SUMMARY.md) for context
2. **Check Configuration Changes**: Reference [CONFIG_MIGRATION.md](./CONFIG_MIGRATION.md) for config-related work
3. **Understand Architecture**: Review this guide's architecture section
4. **Identify Systems**: Determine which centralized systems are affected

### Code Modification Guidelines
1. **Use Centralized Systems**: 
   - Tool damage: Use `ToolDamageUtil.damageToolSafely()`
   - Recipe injection: Use `RecipeInjectionHandler` events
   - Config access: Use `Config.INSTANCE` compatibility layer
   
2. **Safety First**:
   - Always validate inputs and handle exceptions
   - Use proper type checking and casting
   - Implement comprehensive logging for debugging
   
3. **Performance Considerations**:
   - Leverage config caching where appropriate
   - Use early filtering for expensive operations
   - Avoid repeated registry access

4. **Compatibility**:
   - Only modify vanilla blocks in `HarvestBlockHandler`
   - Use namespace filtering for mod compatibility
   - Maintain backward compatibility in config interface

### Testing Strategy
1. **Build System**: `./gradlew build` - verify compilation
2. **Config Validation**: Test config loading with various TOML files
3. **Recipe Injection**: Verify safe recipe generation
4. **Tool Damage**: Test centralized damage logic
5. **Block Compatibility**: Verify selective block modification

## Common Implementation Patterns

### Configuration Access
```java
// Always use the compatibility layer
boolean enabled = Config.INSTANCE.enableDynamicRecipeReplacement.getAsBoolean();
float chance = Config.INSTANCE.flintKnappingConsumeChance.getAsFloat();

// For frequently accessed values, consider caching
List<Block> pottery = ForgeConfig.getPotteryBlockSequence(); // Already cached
```

### Tool Damage
```java
// Use centralized tool damage utility
ToolDamageUtil.damageToolSafely(stack, player, 1, InteractionHand.MAIN_HAND);

// Calculate appropriate damage based on usage
int damage = ToolDamageUtil.calculateToolDamage(stack, ToolUsage.BLOCK_MINING);
```

### Recipe Injection
```java
// Use event-based injection instead of direct manipulation
@SubscribeEvent
public static void onRecipeManagerReady(RecipeManagerLoadEvent event) {
    RecipeInjectionHandler.injectRecipes(event.getRecipeManager(), event.getRegistryAccess());
}
```

### Block Modification
```java
// Only modify compatible blocks
private static boolean shouldModifyBlock(ResourceLocation blockId) {
    String namespace = blockId.getNamespace();
    return "minecraft".equals(namespace) || NoTreePunching.MOD_ID.equals(namespace);
}
```

### Error Handling
```java
// Always include comprehensive error handling
try {
    // Risky operation
} catch (Exception e) {
    LOGGER.error("Operation failed with context", e);
    // Graceful recovery or safe failure
}
```

## Performance Considerations

### Config Caching
- **Cached Values**: Frequently accessed config values are cached automatically
- **Cache Invalidation**: Caches clear on config reload events
- **Memory Efficient**: No memory leaks from old Epsilon system

### Early Filtering
- **Block Processing**: Filter blocks by namespace before expensive operations
- **Recipe Generation**: Pre-filter eligible items before recipe creation
- **Event Handling**: Use early returns to avoid unnecessary processing

### Memory Management
- **No Memory Leaks**: Eliminated problematic supplier caching
- **Proper Resource Management**: All resources properly closed/cleaned
- **Cache Management**: Automatic cache clearing prevents memory bloat

## Integration Guidelines

### Event System
Use proper Forge event bus for all integrations:

```java
@Mod.EventBusSubscriber(modid = NoTreePunching.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class IntegrationHandler {
    
    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        // Safe server integration
    }
}
```

### Compatibility
- **Namespace Filtering**: Only modify vanilla and own blocks
- **Safe Casting**: Use proper type validation throughout
- **Error Recovery**: Graceful handling of missing dependencies

### Error Handling
```java
// Comprehensive error handling pattern
try {
    // Main operation
} catch (SpecificException e) {
    LOGGER.warn("Expected issue occurred: {}", e.getMessage());
    // Specific recovery
} catch (Exception e) {
    LOGGER.error("Unexpected error in operation", e);
    // General recovery or safe failure
}
```

## Migration Information

### From Epsilon Config
- **Automatic Migration**: Old config values are automatically detected
- **Interface Compatibility**: Same method signatures maintained
- **Performance Improvement**: New system is faster and more efficient

### Key Changes
- **Config Format**: JSON → TOML (more standard)
- **Recipe Injection**: Mixin → Event-based (safer)
- **Tool Damage**: Scattered → Centralized (consistent)
- **Block Modification**: Universal → Selective (compatible)

### Upgrade Path
1. **Users**: No action required - automatic migration
2. **Developers**: Use new centralized utilities
3. **Modpack Authors**: Update any direct config references

## Troubleshooting

### Common Issues

#### Build Failures
- **Dependency Issues**: Ensure Forge version compatibility
- **Missing Dependencies**: Check that all Maven repositories are accessible
- **Version Conflicts**: Verify Minecraft and Forge version alignment

#### Config Problems
- **TOML Syntax**: Validate TOML format using online validators
- **Missing Values**: Check that all required config sections exist
- **Type Mismatches**: Ensure config values match expected types

#### Runtime Errors
- **Recipe Injection**: Check logs for recipe generation errors
- **Tool Damage**: Verify items are damageable before applying damage
- **Block Compatibility**: Ensure block modifications only target vanilla blocks

### Debug Information
- **Comprehensive Logging**: All systems include structured debug information
- **Error Context**: Exception messages include relevant context
- **Performance Metrics**: Important operations include timing information

### Support Resources
- **Technical Details**: [COMPREHENSIVE_FIX_SUMMARY.md](./COMPREHENSIVE_FIX_SUMMARY.md)
- **Config Migration**: [CONFIG_MIGRATION.md](./CONFIG_MIGRATION.md)
- **Issue Tracking**: Use GitHub issues for bug reports
- **Development**: This implementation guide for technical questions

## Best Practices

### Code Quality
1. **Use Centralized Systems**: Always prefer centralized utilities over scattered implementations
2. **Validate Inputs**: Check all inputs before processing
3. **Handle Errors**: Include comprehensive error handling with meaningful messages
4. **Log Appropriately**: Use structured logging with proper log levels

### Performance
1. **Cache Wisely**: Use existing caches rather than creating new ones
2. **Filter Early**: Apply filters before expensive operations
3. **Batch Operations**: Process multiple items together when possible
4. **Monitor Memory**: Be conscious of memory usage patterns

### Compatibility
1. **Namespace Awareness**: Always consider mod compatibility
2. **Safe Defaults**: Provide sensible defaults for all operations
3. **Graceful Degradation**: Continue operation even when some features fail
4. **Version Tolerance**: Handle version differences gracefully

---

This implementation guide reflects the current state of the modernized, production-ready NoTreePunching codebase. All deprecated systems have been replaced with safe, performant, and maintainable alternatives while preserving backward compatibility.