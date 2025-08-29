# No Tree Punching - Configuration Migration Guide

## Overview

This version of No Tree Punching has undergone a major configuration system overhaul, replacing the deprecated Epsilon config system with Minecraft Forge's native configuration API. This change provides better compatibility, performance, and maintainability while preserving all existing configuration options.

## What Changed

### Configuration System
- **Old System**: Epsilon config library (deprecated and no longer maintained)
- **New System**: Forge Config API with TOML format
- **File Location**: Same location (`config/notreepunching.toml`)
- **Format**: TOML (more standard and widely supported)

### Compatibility
- **Backward Compatibility**: All existing config values are preserved
- **Same Option Names**: No need to reconfigure your settings
- **Automatic Migration**: The mod will automatically handle the transition

## Configuration Sections

### Recipes
```toml
[recipes]
    # Enables dynamic replacement of log -> plank recipes with axe/saw variants
    enableDynamicRecipeReplacement = true
```

### World Generation
```toml
[worldgen]
    # Enables loose rock world generation in biomes
    enableLooseRocksWorldGen = true
```

### Block Harvesting
```toml
[blockHarvesting]
    # Controls whether blocks can be mined without correct tools
    doBlocksMineWithoutCorrectTool = false
    doBlocksDropWithoutCorrectTool = false
    
    # Instant-break block behavior
    doInstantBreakBlocksDropWithoutCorrectTool = false
    doInstantBreakBlocksMineWithoutCorrectTool = true
    doInstantBreakBlocksDamageKnives = true
```

### Balance
```toml
[balance]
    # Flint knapping chances (0.0 to 1.0)
    flintKnappingConsumeChance = 0.4
    flintKnappingSuccessChance = 0.7
    
    # Fire starter settings
    fireStarterFireStartChance = 0.3
    fireStarterCanMakeCampfire = true
    fireStarterCanMakeSoulCampfire = true
    
    # Large vessel behavior
    largeVesselKeepsContentsWhenBroken = true
    
    # Pottery progression sequence
    potteryBlockSequences = [
        "minecraft:clay",
        "notreepunching:pottery_worked",
        "notreepunching:pottery_large_vessel",
        "notreepunching:pottery_small_vessel",
        "notreepunching:pottery_bucket",
        "notreepunching:pottery_flower_pot",
        "minecraft:air"
    ]
```

## Key Improvements

### Performance
- **Config Caching**: Frequently accessed values are cached for better performance
- **Lazy Loading**: Complex config values (like pottery sequences) are parsed once and cached
- **Memory Efficient**: No memory leaks from the old system

### Compatibility
- **Forge Integration**: Uses Forge's native config system for better mod compatibility
- **Hot Reloading**: Config changes are detected and applied automatically
- **Type Safety**: Better validation and error handling for config values

### Debugging
- **Better Logging**: Improved error messages and debug information
- **Config Validation**: Invalid values are caught early with helpful error messages
- **Reload Command**: Use `/notreepunchingReloadConfig` to reload configuration

## Technical Changes

### For Modpack Authors
- No changes needed - all existing configurations will work
- Better integration with config management tools
- Clearer error messages for invalid configurations

### For Mod Developers
- Recipe injection now uses proper Forge events instead of unsafe mixins
- Block modification is limited to vanilla blocks for better compatibility
- Centralized tool damage system for consistent behavior

## Migration Process

1. **Automatic**: The mod will automatically detect and use your existing config
2. **Manual**: If needed, copy your old settings to the new TOML format
3. **Validation**: The mod will validate all settings on startup

## Troubleshooting

### Config Not Loading
- Check the log file for validation errors
- Ensure the TOML syntax is correct
- Use the reload command to test changes

### Performance Issues
- The new system is more efficient than the old one
- Config caching reduces repeated file reads
- Report any performance regressions as issues

## Support

If you encounter any issues with the configuration migration:
1. Check the debug log for error messages
2. Verify your TOML syntax is correct
3. Use the default values as a reference
4. Report bugs with your config file and log output

## Changelog

### Major Changes
- Replaced Epsilon config system with Forge Config API
- Improved recipe injection system with proper event handling
- Enhanced block compatibility with selective modification
- Added comprehensive error handling throughout
- Optimized performance with caching and early filtering

### Bug Fixes
- Fixed knife durability logic for instant-break blocks
- Fixed memory leaks in config system
- Fixed unsafe casting throughout codebase
- Fixed block modification breaking mod compatibility
- Fixed knapping sound detection to use block tags

This migration ensures No Tree Punching remains compatible with modern Minecraft and Forge versions while providing a more robust and maintainable codebase.