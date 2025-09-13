package com.alcatrazescapee.notreepunching.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

import com.alcatrazescapee.notreepunching.ForgeConfig;

/**
 * Comprehensive debug utility for action tracking, logging, and tool identification
 * across the sharp tool harvesting system. Provides multiple logging levels,
 * action correlation, and centralized formatting with config integration.
 */
public final class DebugUtil
{
    private static final Logger LOGGER = LogUtils.getLogger();
    
    // Thread-safe map to track active actions for proper start/end pairing
    private static final Map<String, ActionContext> ACTIVE_ACTIONS = new ConcurrentHashMap<>();
    
    /**
     * Debug logging levels for different types of messages
     */
    public enum Level
    {
        DEBUG,   // Detailed technical information
        INFO,    // General informational messages
        WARN,    // Warning conditions that might indicate issues
        ERROR    // Error conditions that indicate failures
    }
    
    /**
     * Log a message with specified level, respecting ForgeConfig.enableSharpToolDebugLogging
     * @param level The logging level
     * @param actionId Optional action ID for correlation (can be null)
     * @param message The message to log
     * @param args Optional arguments for string formatting
     */
    public static void log(Level level, @Nullable String actionId, String message, Object... args)
    {
        // Always show ERROR and WARN levels, check config for DEBUG and INFO
        boolean shouldLog = level == Level.ERROR || level == Level.WARN || 
                           ForgeConfig.enableSharpToolDebugLogging.get();
        
        if (!shouldLog) return;
        
        String formattedMessage = args.length > 0 ? String.format(message, args) : message;
        String actionPrefix = actionId != null ? String.format("[%s] ", actionId) : "";
        String fullMessage = String.format("=== %s%s %s ===", actionPrefix, level.name(), formattedMessage);
        
        // Force print for GameTests and console visibility
        System.out.println(fullMessage);
        
        // Also log through SLF4J system based on level
        switch (level)
        {
            case DEBUG -> LOGGER.debug(fullMessage);
            case INFO -> LOGGER.info(fullMessage);
            case WARN -> LOGGER.warn(fullMessage);
            case ERROR -> LOGGER.error(fullMessage);
        }
    }
    
    /**
     * Convenience method for DEBUG level logging
     */
    public static void debug(@Nullable String actionId, String message, Object... args)
    {
        if (!ForgeConfig.enableSharpToolDebugLogging.get()) {
            return;
        }
        log(Level.DEBUG, actionId, message, args);
    }
    
    /**
     * Convenience method for INFO level logging
     */
    public static void info(@Nullable String actionId, String message, Object... args)
    {
        if (!ForgeConfig.enableSharpToolDebugLogging.get()) {
            return;
        }
        log(Level.INFO, actionId, message, args);
    }
    
    /**
     * Convenience method for WARN level logging
     */
    public static void warn(@Nullable String actionId, String message, Object... args)
    {
        if (!ForgeConfig.enableSharpToolDebugLogging.get()) {
            return;
        }
        log(Level.WARN, actionId, message, args);
    }
    
    /**
     * Convenience method for ERROR level logging
     */
    public static void error(@Nullable String actionId, String message, Object... args)
    {
        if (!ForgeConfig.enableSharpToolDebugLogging.get()) {
            return;
        }
        log(Level.ERROR, actionId, message, args);
    }
    
    /**
     * Get comprehensive tool information for debug output
     * @param stack The ItemStack to analyze
     * @return Formatted string with detailed tool information, or empty string if debug disabled
     */
    public static String getDetailedToolInfo(ItemStack stack)
    {
        // Early return for performance if debugging is disabled
        if (!ForgeConfig.enableSharpToolDebugLogging.get())
        {
            return "";
        }
        
        if (stack.isEmpty())
        {
            return "EMPTY_HAND";
        }
        
        String registryName = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        String displayName = stack.getDisplayName().getString();
        boolean isSharpTool = SharpToolUtil.isSharpTool(stack);
        int count = stack.getCount();
        int durability = stack.isDamageableItem() ? (stack.getMaxDamage() - stack.getDamageValue()) : -1;
        
        if (durability >= 0)
        {
            return String.format("%s (display:'%s', count:%d, durability:%d/%d, sharp_tool:%s)", 
                               registryName, displayName, count, durability, stack.getMaxDamage(), isSharpTool);
        }
        else
        {
            return String.format("%s (display:'%s', count:%d, sharp_tool:%s)", 
                               registryName, displayName, count, isSharpTool);
        }
    }
    
    /**
     * Get player information for debug output
     * @param player The player to analyze
     * @return Formatted string with player information, or empty string if debug disabled
     */
    public static String getPlayerInfo(@Nullable Player player)
    {
        // Early return for performance if debugging is disabled
        if (!ForgeConfig.enableSharpToolDebugLogging.get())
        {
            return "";
        }
        
        if (player == null)
        {
            return "NULL_PLAYER";
        }
        
        String playerName = player.getName().getString();
        String playerType = player.getClass().getSimpleName();
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        String position = player.blockPosition().toString();
        String gameMode = player.isCreative() ? "CREATIVE" : player.isSpectator() ? "SPECTATOR" : "SURVIVAL";
        
        return String.format("%s (%s) [pos:%s, mode:%s, main:%s, off:%s]", 
                           playerName, playerType, position, gameMode,
                           getDetailedToolInfo(mainHand), 
                           getDetailedToolInfo(offHand));
    }
    
    /**
     * Get block information for debug output
     * @param state The block state to analyze
     * @param pos The block position (can be null)
     * @return Formatted string with block information, or empty string if debug disabled
     */
    public static String getBlockInfo(BlockState state, @Nullable BlockPos pos)
    {
        // Early return for performance if debugging is disabled
        if (!ForgeConfig.enableSharpToolDebugLogging.get())
        {
            return "";
        }
        
        String registryName = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
        String displayName = state.getBlock().getName().getString();
        String positionInfo = pos != null ? pos.toString() : "UNKNOWN_POS";
        boolean requiresSharpTool = SharpToolUtil.requiresSharpTool(state);
        float hardness = state.getDestroySpeed(null, null);
        
        return String.format("%s (display:'%s', pos:%s, hardness:%.1f, requires_sharp:%s)", 
                           registryName, displayName, positionInfo, hardness, requiresSharpTool);
    }
    
    /**
     * Start tracking an action with a unique ID and comprehensive context capture
     * @param player The player performing the action
     * @param state The block being acted upon
     * @param pos The position of the block
     * @param actionType Description of the action (e.g., "HARVEST", "MINE", "BREAK")
     * @return Unique action ID for tracking throughout the call chain, or null if debug disabled
     */
    public static String startAction(Player player, BlockState state, @Nullable BlockPos pos, String actionType)
    {
        // Early return for performance if debugging is disabled
        if (!ForgeConfig.enableSharpToolDebugLogging.get())
        {
            return null;
        }
        
        String actionId = UUID.randomUUID().toString().substring(0, 8);
        
        // Capture immutable context at action start to prevent changes during execution
        String playerInfo = getPlayerInfo(player);
        String blockInfo = getBlockInfo(state, pos);
        String toolInfo = getDetailedToolInfo(player.getMainHandItem());
        
        ActionContext context = new ActionContext(actionId, actionType, playerInfo, blockInfo, toolInfo);
        ACTIVE_ACTIONS.put(actionId, context);
        
        info(actionId, "ACTION START %s: Player %s using %s on Block %s", 
             actionType, playerInfo, toolInfo, blockInfo);
        
        return actionId;
    }
    
    /**
     * End tracking an action and log the comprehensive result
     * @param actionId The action ID returned from startAction
     * @param result The final result of the action
     * @param location Description of where the action ended (e.g., method name)
     */
    public static void endAction(String actionId, Object result, String location)
    {
        // Early return for performance if debugging is disabled or actionId is null
        if (!ForgeConfig.enableSharpToolDebugLogging.get() || actionId == null)
        {
            return;
        }
        
        ActionContext context = ACTIVE_ACTIONS.remove(actionId);
        if (context == null)
        {
            warn(actionId, "ACTION END WARNING: No matching start found! Result: %s at %s", result, location);
            return;
        }
        
        long duration = System.currentTimeMillis() - context.startTime;
        
        info(actionId, "ACTION END %s: Result=%s at %s | Duration=%dms | Tool=%s | Block=%s", 
             context.actionType, result, location, duration, context.toolInfo, context.blockInfo);
    }
    
    /**
     * End an action with an error condition
     * @param actionId The action ID
     * @param error The error that occurred
     * @param location Where the error occurred
     */
    public static void endActionWithError(String actionId, Throwable error, String location)
    {
        // Early return for performance if debugging is disabled or actionId is null
        if (!ForgeConfig.enableSharpToolDebugLogging.get() || actionId == null)
        {
            return;
        }
        
        ActionContext context = ACTIVE_ACTIONS.remove(actionId);
        if (context == null)
        {
            error(actionId, "ACTION ERROR: No matching start found! Error: %s at %s", error.getMessage(), location);
            return;
        }
        
        long duration = System.currentTimeMillis() - context.startTime;
        
        error(actionId, "ACTION ERROR %s: %s at %s | Duration=%dms | Tool=%s | Block=%s", 
              context.actionType, error.getMessage(), location, duration, context.toolInfo, context.blockInfo);
    }
    
    /**
     * Check if an action is currently being tracked
     * @param actionId The action ID to check
     * @return true if the action is active
     */
    public static boolean isActionActive(String actionId)
    {
        return ACTIVE_ACTIONS.containsKey(actionId);
    }
    
    /**
     * Get count of currently active actions (for debugging the debug system)
     * @return Number of active actions
     */
    public static int getActiveActionCount()
    {
        return ACTIVE_ACTIONS.size();
    }
    
    /**
     * Clear all active actions - useful for cleanup or testing
     * @return Number of actions that were cleared
     */
    public static int clearActiveActions()
    {
        int count = ACTIVE_ACTIONS.size();
        ACTIVE_ACTIONS.clear();
        if (count > 0)
        {
            warn(null, "Cleared %d orphaned debug actions", count);
        }
        return count;
    }
    
    /**
     * Log configuration state for debugging
     */
    public static void logConfigState()
    {
        boolean debugEnabled = ForgeConfig.enableSharpToolDebugLogging.get();
        info(null, "DebugUtil Configuration: enableSharpToolDebugLogging=%s, activeActions=%d", 
             debugEnabled, getActiveActionCount());
    }
    
    /**
     * Internal class to track action context with immutable data snapshot
     */
    private static class ActionContext
    {
        final String actionId;
        final String actionType;
        final String playerInfo;      // Immutable snapshot at action start
        final String blockInfo;       // Immutable snapshot at action start
        final String toolInfo;        // Immutable snapshot at action start
        final long startTime;         // For duration tracking
        
        ActionContext(String actionId, String actionType, String playerInfo, String blockInfo, String toolInfo)
        {
            this.actionId = actionId;
            this.actionType = actionType;
            this.playerInfo = playerInfo;
            this.blockInfo = blockInfo;
            this.toolInfo = toolInfo;
            this.startTime = System.currentTimeMillis();
        }
        
        @Override
        public String toString()
        {
            return String.format("ActionContext[id=%s, type=%s, duration=%dms]", 
                               actionId, actionType, System.currentTimeMillis() - startTime);
        }
    }
    
    private DebugUtil()
    {
        // Utility class - no instantiation
    }
}