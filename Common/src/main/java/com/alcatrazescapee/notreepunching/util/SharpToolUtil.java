package com.alcatrazescapee.notreepunching.util;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.Item;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alcatrazescapee.notreepunching.Config;
import com.alcatrazescapee.notreepunching.ForgeConfig;
import com.alcatrazescapee.notreepunching.common.ModTags;

/**
 * Centralized sharp tool utility for tag-based plant harvesting system.
 * Provides caching and performance optimization for frequently accessed tag checks.
 */
public final class SharpToolUtil
{
    private static final Logger LOGGER = LogUtils.getLogger();
    
    // Cache for performance optimization
    private static final Map<Item, Boolean> SHARP_TOOL_CACHE = new ConcurrentHashMap<>();
    
    /**
     * Check if an ItemStack is a sharp tool capable of harvesting plants
     * @param stack The item stack to check
     * @return true if the item is tagged as a sharp tool and the system is enabled
     */
    public static boolean isSharpTool(ItemStack stack)
    {
        if (ForgeConfig.enableSharpToolDebugLogging.get())
        {
            LOGGER.debug("SharpToolUtil.isSharpTool() called with stack: {} (item: {})", 
                        stack, stack.isEmpty() ? "EMPTY" : stack.getItem().getDescriptionId());
        }
        
        if (stack.isEmpty())
        {
            if (ForgeConfig.enableSharpToolDebugLogging.get())
            {
                LOGGER.debug("SharpToolUtil.isSharpTool() returning false - stack is empty");
            }
            return false;
        }
        
        // Check if sharp tool system is enabled
        boolean systemEnabled = Config.INSTANCE.enableSharpToolSystem.getAsBoolean();
        if (ForgeConfig.enableSharpToolDebugLogging.get())
        {
            LOGGER.debug("SharpToolUtil.isSharpTool() system enabled: {}", systemEnabled);
        }
        
        if (!systemEnabled)
        {
            if (ForgeConfig.enableSharpToolDebugLogging.get())
            {
                LOGGER.debug("SharpToolUtil.isSharpTool() returning false - system disabled");
            }
            return false; // System disabled, no items are considered sharp tools
        }
        
        Item item = stack.getItem();
        Boolean cachedResult = SHARP_TOOL_CACHE.get(item);
        if (ForgeConfig.enableSharpToolDebugLogging.get())
        {
            LOGGER.debug("SharpToolUtil.isSharpTool() cache lookup for item {}: {}", 
                        item.getDescriptionId(), cachedResult != null ? cachedResult : "CACHE_MISS");
        }
        
        boolean result = SHARP_TOOL_CACHE.computeIfAbsent(item, itm -> {
            boolean isTagged = Helpers.isItem(itm, ModTags.Items.SHARP_TOOLS);
            if (ForgeConfig.enableSharpToolDebugLogging.get())
            {
                LOGGER.debug("SharpToolUtil.isSharpTool() computed tag check for {}: {}", 
                           itm.getDescriptionId(), isTagged);
            }
            return isTagged;
        });
        
        if (ForgeConfig.enableSharpToolDebugLogging.get())
        {
            LOGGER.debug("SharpToolUtil.isSharpTool() returning: {} for item: {}", result, item.getDescriptionId());
        }
        
        return result;
    }
    
    /**
     * Check if a block requires a sharp tool for drops
     * @param state The block state to check
     * @return true if the block requires a sharp tool for item drops and the requirement is enabled
     */
    public static boolean requiresSharpTool(BlockState state)
    {
        if (ForgeConfig.enableSharpToolDebugLogging.get())
        {
            LOGGER.debug("SharpToolUtil.requiresSharpTool() called with state: {} (block: {})", 
                        state, state.getBlock().getDescriptionId());
        }
        
        // Check if sharp tool requirement is enabled
        boolean requirementEnabled = Config.INSTANCE.requireSharpToolForPlants.getAsBoolean();
        if (ForgeConfig.enableSharpToolDebugLogging.get())
        {
            LOGGER.debug("SharpToolUtil.requiresSharpTool() requirement enabled: {}", requirementEnabled);
        }
        
        if (!requirementEnabled)
        {
            if (ForgeConfig.enableSharpToolDebugLogging.get())
            {
                LOGGER.debug("SharpToolUtil.requiresSharpTool() returning false - requirement disabled");
            }
            return false; // Requirement disabled, no blocks require sharp tools
        }
        
        // Use state-based checking to be more accurate (not cached by block since states can vary)
        boolean isRequiredTag = state.is(ModTags.Blocks.REQUIRES_SHARP_TOOL);
        boolean isPlantFiberSource = state.is(ModTags.Blocks.PLANT_FIBER_SOURCES);
        boolean isSwordEfficient = state.is(BlockTags.SWORD_EFFICIENT);
        
        if (ForgeConfig.enableSharpToolDebugLogging.get())
        {
            LOGGER.debug("SharpToolUtil.requiresSharpTool() tag checks for {}: REQUIRES_SHARP_TOOL={}, PLANT_FIBER_SOURCES={}, SWORD_EFFICIENT={}", 
                        state.getBlock().getDescriptionId(), isRequiredTag, isPlantFiberSource, isSwordEfficient);
        }
        
        boolean result = isRequiredTag || isPlantFiberSource || isSwordEfficient;
        
        if (ForgeConfig.enableSharpToolDebugLogging.get())
        {
            LOGGER.debug("SharpToolUtil.requiresSharpTool() returning: {} for block: {}", result, state.getBlock().getDescriptionId());
        }
        
        return result;
    }
    
    /**
     * Get destroy speed for sharp tools on applicable blocks
     * @param stack The item stack being used
     * @param state The block state being broken
     * @return The destroy speed (15.0f for sharp tools on applicable blocks when system is enabled, 1.0f otherwise)
     */
    public static float getDestroySpeed(ItemStack stack, BlockState state)
    {
        if (ForgeConfig.enableSharpToolDebugLogging.get())
        {
            LOGGER.debug("SharpToolUtil.getDestroySpeed() called with stack: {} (item: {}), state: {} (block: {})", 
                        stack, stack.isEmpty() ? "EMPTY" : stack.getItem().getDescriptionId(),
                        state, state.getBlock().getDescriptionId());
        }
        
        boolean isSharp = isSharpTool(stack);
        boolean requiresSharp = requiresSharpTool(state);
        
        if (ForgeConfig.enableSharpToolDebugLogging.get())
        {
            LOGGER.debug("SharpToolUtil.getDestroySpeed() checks: isSharpTool={}, requiresSharpTool={}", isSharp, requiresSharp);
        }
        
        if (isSharp && requiresSharp)
        {
            if (ForgeConfig.enableSharpToolDebugLogging.get())
            {
                LOGGER.debug("SharpToolUtil.getDestroySpeed() returning 15.0f - sharp tool on applicable block");
            }
            return 15.0f; // Match existing knife behavior
        }
        
        if (ForgeConfig.enableSharpToolDebugLogging.get())
        {
            LOGGER.debug("SharpToolUtil.getDestroySpeed() returning 1.0f - default speed");
        }
        return 1.0f; // Default speed
    }
    
    /**
     * Check if tool should take damage when mining a plant block
     * @param stack The item stack being used
     * @param state The block state being mined
     * @return true if the tool should take damage (respects configuration settings)
     */
    public static boolean shouldDamageToolOnPlant(ItemStack stack, BlockState state)
    {
        if (ForgeConfig.enableSharpToolDebugLogging.get())
        {
            LOGGER.debug("SharpToolUtil.shouldDamageToolOnPlant() called with stack: {} (item: {}), state: {} (block: {})", 
                        stack, stack.isEmpty() ? "EMPTY" : stack.getItem().getDescriptionId(),
                        state, state.getBlock().getDescriptionId());
        }
        
        boolean isSharp = isSharpTool(stack);
        boolean requiresSharp = requiresSharpTool(state);
        boolean damageEnabled = Config.INSTANCE.doInstantBreakBlocksDamageKnives.getAsBoolean();
        
        if (ForgeConfig.enableSharpToolDebugLogging.get())
        {
            LOGGER.debug("SharpToolUtil.shouldDamageToolOnPlant() checks: isSharpTool={}, requiresSharpTool={}, damageEnabled={}", 
                        isSharp, requiresSharp, damageEnabled);
        }
        
        boolean result = isSharp && requiresSharp && damageEnabled;
        
        if (ForgeConfig.enableSharpToolDebugLogging.get())
        {
            LOGGER.debug("SharpToolUtil.shouldDamageToolOnPlant() returning: {}", result);
        }
        
        return result;
    }
    
    /**
     * Clear caches when tags reload or configuration changes
     * Called from mod lifecycle events to ensure cache consistency
     */
    public static void clearCache()
    {
        int cacheSize = SHARP_TOOL_CACHE.size();
        SHARP_TOOL_CACHE.clear();
        
        if (ForgeConfig.enableSharpToolDebugLogging.get())
        {
            LOGGER.debug("SharpToolUtil.clearCache() cleared {} cached entries", cacheSize);
        }
        LOGGER.debug("Sharp tool caches cleared");
    }
    
    /**
     * Clear caches when configuration changes to ensure new settings take effect
     * This should be called from config reload events
     */
    public static void onConfigReload()
    {
        if (ForgeConfig.enableSharpToolDebugLogging.get())
        {
            LOGGER.debug("SharpToolUtil.onConfigReload() called - clearing caches");
        }
        clearCache();
        LOGGER.info("Sharp tool system configuration reloaded");
    }
    
    private SharpToolUtil()
    {
        // Utility class - no instantiation
    }
}