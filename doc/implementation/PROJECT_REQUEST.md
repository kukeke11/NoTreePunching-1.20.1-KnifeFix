# Sharp Tool Integration System - Final Project Specification

## Project Overview

Refactor NoTreePunching's plant harvesting system from knife-specific checks to a flexible tag-based sharp tool system that maintains existing knife functionality while enabling automatic cross-mod compatibility through proper Forge tag conventions.

## Implementation Strategy: Option B
Create new sharp tool system alongside existing knife functionality, then migrate knife behavior to use the new system while maintaining complete backward compatibility.

## Core Objectives

### Sharp Tool System Implementation
- [ ] **Replace knife-specific checks with tag-based sharp tool detection**
  - [ ] Maintain all existing knife functionality (recipes, durability, crafting uses)
  - [ ] Create `sharp_tools` item tag that includes all NTP knives by default
  - [ ] Any item with the sharp tool tag can harvest plants/flowers with drops
  - [ ] Preserve existing knife behavior exactly as-is during migration

### Cross-Mod Integration Priority
- [ ] **Automatic compatibility through standard Forge conventions**
  - [ ] Use established Forge tags for plant identification (`minecraft:flowers`, `minecraft:crops`, etc.)
  - [ ] Research and use established Forge tags for tool classification
  - [ ] No manual mod support needed if mods follow proper tagging
  - [ ] **Primary Success Metric**: Modded plants tagged properly work automatically
  - [ ] For unsupported plants: modpack authors can add conventional tags via datapacks

### Existing System Preservation
- [ ] **All current knife functionality remains unchanged**
  - [ ] Plant fiber generation works exactly as before (if it exists)
  - [ ] Knife crafting recipes work as before
  - [ ] Knife durability behavior unchanged
  - [ ] Knife-based crafting recipes continue working
- [ ] **Non-plant blocks maintain vanilla behavior**
  - [ ] Leaves still drop sticks/saplings as expected
  - [ ] Other harvesting mechanics unaffected

## Design Architecture

### Tag-Based Implementation with Minimal Hardcoding
- [ ] **Three-tier tag system for clean organization**
  - [ ] `sharp_tools` (items) - defines what can harvest plants
  - [ ] `requires_sharp_tool` (blocks) - defines what needs sharp tools for drops
  - [ ] `plant_fiber_sources` (blocks) - defines what generates plant fiber when cut (only if system doesn't already exist)
- [ ] **Datapack-friendly design**
  - [ ] Should be easily extensible via datapacks
  - [ ] Research and use existing Forge plant/tool tag conventions
  - [ ] Minimize hardcoded lists, maximize tag-based flexibility

### Event System Integration Strategy
- [ ] **Create new sharp tool system alongside existing code**
  - [ ] Determine if current system uses `EventHandler.java` or `KnifeItem.java` overrides
  - [ ] Build new tag-based system without modifying existing knife logic initially
  - [ ] Migrate knife behavior to use new system once stable
  - [ ] Preserve performance characteristics of current system
  - [ ] Early exit optimization for non-applicable blocks

### Configuration Integration
- [ ] **Minimal configuration additions**
  - [ ] Integration with existing Forge Config system
  - [ ] Toggle for sharp tool requirement system (enable/disable)
  - [ ] Backward compatibility maintained
  - [ ] No additional configuration complexity beyond existing system

## Behavior Specifications (Unchanged Requirements)

| Scenario | Block Breaking | Item Drops | Notes |
|----------|----------------|------------|--------|
| Sharp tool on flowers | ✅ Success | ✅ Normal drops | Current knife behavior preserved |
| Sharp tool on tall grass | ✅ Success | ✅ Normal drops + plant fiber | Fiber generation (if exists) |
| Hand on flowers | ✅ Success | ❌ No drops | Current behavior maintained |
| Hand on tall grass | ✅ Success | ❌ No drops | Current behavior maintained |
| Any tool on leaves | ✅ Success | ✅ Vanilla behavior | Completely unchanged |
| Shears on blocks | ✅ Success | ✅ Vanilla behavior | Completely unchanged |

## Research Requirements for Implementation

### Tag Convention Investigation (Critical)
- [ ] **Identify existing Forge tags for plants**
  - [ ] Standard Minecraft tags (`minecraft:flowers`, `minecraft:crops`)
  - [ ] Common Forge conventions for plant classification
  - [ ] Popular mod ecosystems' tagging approaches

- [ ] **Identify existing Forge tags for tools**
  - [ ] Research if `forge:tools/knives` or similar exists
  - [ ] Common tool classification tags in mod ecosystem
  - [ ] Best practices for tool categorization

### Current System Analysis (Essential)
- [ ] **Determine existing plant harvesting mechanism**
  - [ ] Does knife use `mineBlock()` override or event system?
  - [ ] How does current plant fiber generation work (if it exists)?
  - [ ] What blocks currently require knives vs. those that don't?
  - [ ] Integration points between existing knife system and block harvesting

## Success Criteria

### Primary Success Metrics
1. **Existing knife functionality works identically** - users notice no difference in knife behavior
2. **Modded items tagged as sharp tools can harvest plants** - automatic cross-mod compatibility
3. **Modded plants tagged properly work with system** - no manual configuration needed by users
4. **No performance regression** on vanilla block breaking
5. **Clean, maintainable code** following NoTreePunching's current architectural patterns

### Cross-Mod Compatibility Priorities
- **Standard Forge conventions preferred** - avoid reinventing existing systems
- **Datapack friendly** - modpack authors can customize through standard tag modification
- **Performance conscious** - minimal impact on existing systems
- **Automatic integration** - properly tagged mods work without additional configuration

## Implementation Notes for Architect Role

### Architecture Decision Points
- **Research First**: Identify existing tag conventions before creating new ones
- **Preserve Everything**: This is about extending compatibility, not changing behavior  
- **Tag-Based Approach**: Minimize hardcoded lists, maximize datapack customization
- **Option B Strategy**: Create new system, migrate existing functionality to use it
- **Performance Focus**: No impact on vanilla block breaking performance

### Key Questions for Technical Specification
1. What specific Forge tags already exist for plant classification?
2. How does the current knife system handle plant breaking - events or item overrides?
3. Does the current codebase have plant fiber generation, or would this be new?
4. What's the current integration point between tools and block harvesting?
5. Which existing tag conventions would provide the best cross-mod compatibility?

### Integration Strategy
- **Phase 1**: Build new tag-based sharp tool system alongside existing knife code
- **Phase 2**: Test new system with existing knives included in sharp_tools tag
- **Phase 3**: Gradually migrate knife-specific logic to use new system
- **Phase 4**: Deprecate old knife-specific paths while maintaining compatibility

## Open Architecture Questions for Research

- How does the current `KnifeItem.java` implement plant harvesting?
- What is the current event handling architecture in `EventHandler.java`?
- Are there existing plant fiber mechanics that need to be integrated?
- What performance characteristics must be maintained?
- Which cross-mod compatibility patterns are most important to support?

## Final Notes

This specification focuses on the core goal of achieving cross-mod compatibility through proper tag conventions while making absolutely no changes to existing functionality. The implementation should prioritize research of existing Forge conventions to maximize automatic compatibility with the broader modding ecosystem.

**Key Principle**: Extend compatibility and functionality without changing existing behavior or user experience.