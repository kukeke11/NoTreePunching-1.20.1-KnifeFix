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
        if (stack.isEmpty())
        {
            return false;
        }
        
        // Check if sharp tool system is enabled
        if (!Config.INSTANCE.enableSharpToolSystem.getAsBoolean())
        {
            return false; // System disabled, no items are considered sharp tools
        }
        
        return SHARP_TOOL_CACHE.computeIfAbsent(stack.getItem(), item -> 
            Helpers.isItem(item, ModTags.Items.SHARP_TOOLS)
        );
    }
    
    /**
     * Check if a block requires a sharp tool for drops
     * @param state The block state to check
     * @return true if the block requires a sharp tool for item drops and the requirement is enabled
     */
    public static boolean requiresSharpTool(BlockState state)
    {
        // Check if sharp tool requirement is enabled
        if (!Config.INSTANCE.requireSharpToolForPlants.getAsBoolean())
        {
            return false; // Requirement disabled, no blocks require sharp tools
        }
        
        // Use state-based checking to be more accurate (not cached by block since states can vary)
        return state.is(ModTags.Blocks.REQUIRES_SHARP_TOOL) || 
               state.is(ModTags.Blocks.PLANT_FIBER_SOURCES) ||
               state.is(BlockTags.SWORD_EFFICIENT);
    }
    
    /**
     * Get destroy speed for sharp tools on applicable blocks
     * @param stack The item stack being used
     * @param state The block state being broken
     * @return The destroy speed (15.0f for sharp tools on applicable blocks when system is enabled, 1.0f otherwise)
     */
    public static float getDestroySpeed(ItemStack stack, BlockState state)
    {
        if (isSharpTool(stack) && requiresSharpTool(state))
        {
            return 15.0f; // Match existing knife behavior
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
        return isSharpTool(stack) && requiresSharpTool(state) && 
               Config.INSTANCE.doInstantBreakBlocksDamageKnives.getAsBoolean();
    }
    
    /**
     * Clear caches when tags reload or configuration changes
     * Called from mod lifecycle events to ensure cache consistency
     */
    public static void clearCache()
    {
        SHARP_TOOL_CACHE.clear();
        LOGGER.debug("Sharp tool caches cleared");
    }
    
    /**
     * Clear caches when configuration changes to ensure new settings take effect
     * This should be called from config reload events
     */
    public static void onConfigReload()
    {
        clearCache();
        LOGGER.info("Sharp tool system configuration reloaded");
    }
    
    private SharpToolUtil()
    {
        // Utility class - no instantiation
    }
}