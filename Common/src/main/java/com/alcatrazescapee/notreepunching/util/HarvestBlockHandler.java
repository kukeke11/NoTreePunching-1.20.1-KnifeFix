package com.alcatrazescapee.notreepunching.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.alcatrazescapee.notreepunching.Config;
import com.alcatrazescapee.notreepunching.ForgeConfig;
import com.alcatrazescapee.notreepunching.NoTreePunching;
import com.alcatrazescapee.notreepunching.common.ModTags;
import com.alcatrazescapee.notreepunching.mixin.AbstractBlockAccessor;
import com.alcatrazescapee.notreepunching.mixin.AbstractBlockStateAccessor;
import com.alcatrazescapee.notreepunching.util.SharpToolUtil;
import com.alcatrazescapee.notreepunching.util.DebugUtil;


public final class HarvestBlockHandler
{
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Selective block modification that only targets vanilla blocks and explicitly compatible blocks.
     * Adds config option for modded block compatibility and uses registry namespaces for filtering.
     * Optimized with early filtering to reduce processing overhead.
     */
    public static void setup()
    {
        int vanillaBlocksModified = 0;
        int moddedBlocksSkipped = 0;
        int totalBlocks = 0;
        
        // Pre-filter blocks for better performance
        final List<Block> blocksToProcess = new ArrayList<>();
        for (Block block : BuiltInRegistries.BLOCK)
        {
            totalBlocks++;
            final ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
            
            // Early filtering - skip blocks that don't need processing
            if (shouldModifyBlock(blockId))
            {
                blocksToProcess.add(block);
            }
            else
            {
                moddedBlocksSkipped++;
            }
        }
        
        LOGGER.info("Block processing: {} total blocks, {} selected for processing, {} skipped for compatibility", 
                   totalBlocks, blocksToProcess.size(), moddedBlocksSkipped);
        
        // Process only the filtered blocks
        for (Block block : blocksToProcess)
        {
            try
            {
                if (processBlock(block))
                {
                    vanillaBlocksModified++;
                }
            }
            catch (Exception e)
            {
                final ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
                LOGGER.error("Failed to modify block {}, skipping", blockId, e);
            }
        }
        
        LOGGER.info("Block harvest setup complete: {} blocks modified successfully", vanillaBlocksModified);
    }

    /**
     * Process a single block with proper error handling
     * @return true if the block was modified, false otherwise
     */
    private static boolean processBlock(Block block)
    {
        final AbstractBlockAccessor blockAccess = (AbstractBlockAccessor) block;
        final BlockBehaviour.Properties settings = blockAccess.getProperties();

        // Check if all possible states have destroySpeed == 0 (instant break blocks like grass/flowers)
        boolean allStatesInstantBreak = true;
        boolean anyStateRequiresSharpTool = false;
        
        for (BlockState state : block.getStateDefinition().getPossibleStates())
        {
            if (((AbstractBlockStateAccessor) state).getDestroySpeed() != 0F)
            {
                allStatesInstantBreak = false;
            }
            
            // Check if any state requires sharp tools (plants that need knives)
            if (state.is(ModTags.Blocks.REQUIRES_SHARP_TOOL) || 
                state.is(ModTags.Blocks.PLANT_FIBER_SOURCES))
            {
                anyStateRequiresSharpTool = true;
            }
        }

        // Force requiresCorrectToolForDrops for:
        // 1. All non-instant-break blocks (original behavior)
        // 2. Instant-break blocks that require sharp tools (plants)
        if (!allStatesInstantBreak || anyStateRequiresSharpTool)
        {
            // Forcefully set to require a tool for drops
            // Need to do both the block settings and the block state since the value is copied there for every state
            settings.requiresCorrectToolForDrops();
            for (BlockState state : block.getStateDefinition().getPossibleStates())
            {
                ((AbstractBlockStateAccessor) state).setRequiresCorrectToolForDrops(true);
            }
            return true;
        }
        return false;
    }

    /**
     * Determines if a block should be modified based on its namespace and configuration.
     * Only modifies vanilla blocks and explicitly whitelisted blocks for compatibility.
     */
    private static boolean shouldModifyBlock(ResourceLocation blockId)
    {
        final String namespace = blockId.getNamespace();
        
        // Always modify vanilla blocks
        if ("minecraft".equals(namespace))
        {
            return true;
        }
        
        // Always modify our own blocks
        if (NoTreePunching.MOD_ID.equals(namespace))
        {
            return true;
        }
        
        // For now, skip all other modded blocks for safety
        // This can be made configurable in the future if needed
        return false;
    }

    public static boolean isUsingCorrectToolToMine(BlockState state, @Nullable BlockPos pos, Player player)
    {
        // Start action tracking for this algorithm chain
        String actionId = DebugUtil.startAction(player, state, pos, "TOOL_TO_MINE_CHECK");
        
        try
        {
            DebugUtil.debug(actionId, "HarvestBlockHandler.isUsingCorrectToolToMine() - Entry point");
            DebugUtil.debug(actionId, "HarvestBlockHandler.isUsingCorrectToolToMine() - Player: %s", DebugUtil.getPlayerInfo(player));
            DebugUtil.debug(actionId, "HarvestBlockHandler.isUsingCorrectToolToMine() - Block: %s", DebugUtil.getBlockInfo(state, pos));
            DebugUtil.debug(actionId, "HarvestBlockHandler.isUsingCorrectToolToMine() - Tool: %s", DebugUtil.getDetailedToolInfo(player.getMainHandItem()));
            
            boolean result = isUsingCorrectTool(state, pos, player, ModTags.Blocks.ALWAYS_BREAKABLE, 
                                              () -> Config.INSTANCE.doBlocksMineWithoutCorrectTool.getAsBoolean(), 
                                              () -> Config.INSTANCE.doInstantBreakBlocksMineWithoutCorrectTool.getAsBoolean(), 
                                              true);
            
            DebugUtil.debug(actionId, "HarvestBlockHandler.isUsingCorrectToolToMine() - isUsingCorrectTool returned: %s", result);
            DebugUtil.endAction(actionId, result, "HarvestBlockHandler.isUsingCorrectToolToMine");
            return result;
        }
        catch (Exception e)
        {
            DebugUtil.endActionWithError(actionId, e, "HarvestBlockHandler.isUsingCorrectToolToMine");
            throw e;
        }
    }

    public static boolean isUsingCorrectToolForDrops(BlockState state, @Nullable BlockPos pos, Player player)
    {
        // Start action tracking for this algorithm chain
        String actionId = DebugUtil.startAction(player, state, pos, "TOOL_FOR_DROPS_CHECK");
        
        try
        {
            DebugUtil.debug(actionId, "HarvestBlockHandler.isUsingCorrectToolForDrops() - Entry point");
            DebugUtil.debug(actionId, "HarvestBlockHandler.isUsingCorrectToolForDrops() - Player: %s", DebugUtil.getPlayerInfo(player));
            DebugUtil.debug(actionId, "HarvestBlockHandler.isUsingCorrectToolForDrops() - Block: %s", DebugUtil.getBlockInfo(state, pos));
            DebugUtil.debug(actionId, "HarvestBlockHandler.isUsingCorrectToolForDrops() - Tool: %s", DebugUtil.getDetailedToolInfo(player.getMainHandItem()));
            
            boolean result = isUsingCorrectTool(state, pos, player, ModTags.Blocks.ALWAYS_DROPS, 
                                              () -> Config.INSTANCE.doBlocksDropWithoutCorrectTool.getAsBoolean(), 
                                              () -> Config.INSTANCE.doInstantBreakBlocksDropWithoutCorrectTool.getAsBoolean(), 
                                              false);
            
            DebugUtil.debug(actionId, "HarvestBlockHandler.isUsingCorrectToolForDrops() - isUsingCorrectTool returned: %s", result);
            
            // This is often the FINAL point in the algorithm chain - mark as END
            DebugUtil.endAction(actionId, result, "HarvestBlockHandler.isUsingCorrectToolForDrops");
            return result;
        }
        catch (Exception e)
        {
            DebugUtil.endActionWithError(actionId, e, "HarvestBlockHandler.isUsingCorrectToolForDrops");
            throw e;
        }
    }

    private static boolean isUsingCorrectTool(BlockState state, @Nullable BlockPos pos, Player player, TagKey<Block> alwaysAllowTag, Supplier<Boolean> withoutCorrectTool, BooleanSupplier instantBreakBlocksWithoutCorrectTool, boolean checkingCanMine)
    {
        // Create action ID for detailed decision tracking
        String actionId = DebugUtil.startAction(player, state, pos, "CORRECT_TOOL_LOGIC");
        
        try
        {
            DebugUtil.debug(actionId, "HarvestBlockHandler.isUsingCorrectTool() - Starting decision tree | checkingCanMine=%s", checkingCanMine);
            
            // Check 1: Feature disabled globally
            boolean featureDisabled = withoutCorrectTool.get();
            DebugUtil.debug(actionId, "HarvestBlockHandler.isUsingCorrectTool() - Check 1: Feature disabled = %s", featureDisabled);
            if (featureDisabled)
            {
                DebugUtil.endAction(actionId, true, "HarvestBlockHandler.isUsingCorrectTool - Feature disabled");
                return true; // Feature is disabled, always allow
            }

            // Check 2: Instant break blocks and conditional disable
            final float destroySpeed = getDestroySpeed(state, pos, player);
            boolean isInstantBreak = destroySpeed == 0;
            boolean instantBreakDisabled = instantBreakBlocksWithoutCorrectTool.getAsBoolean();
            DebugUtil.debug(actionId, "HarvestBlockHandler.isUsingCorrectTool() - Check 2: destroySpeed=%.2f, isInstantBreak=%s, instantBreakDisabled=%s", destroySpeed, isInstantBreak, instantBreakDisabled);
            if (isInstantBreak && instantBreakDisabled)
            {
                DebugUtil.endAction(actionId, true, "HarvestBlockHandler.isUsingCorrectTool - Instant break disabled");
                return true; // Feature is conditionally disabled for instant break blocks, always allow
            }

            // Check 3: Always allow tag
            boolean isAlwaysAllowed = state.is(alwaysAllowTag);
            DebugUtil.debug(actionId, "HarvestBlockHandler.isUsingCorrectTool() - Check 3: Block in alwaysAllowTag = %s", isAlwaysAllowed);
            if (isAlwaysAllowed)
            {
                DebugUtil.endAction(actionId, true, "HarvestBlockHandler.isUsingCorrectTool - Always allow tag");
                return true; // Block is set to always allow
            }

            // Check 4: Vanilla tool correctness
            final ItemStack stack = player.getMainHandItem();
            boolean vanillaCorrect = stack.isCorrectToolForDrops(state);
            DebugUtil.debug(actionId, "HarvestBlockHandler.isUsingCorrectTool() - Check 4: Vanilla isCorrectToolForDrops = %s | Tool: %s", vanillaCorrect, DebugUtil.getDetailedToolInfo(stack));
            if (vanillaCorrect)
            {
                DebugUtil.endAction(actionId, true, "HarvestBlockHandler.isUsingCorrectTool - Vanilla correct tool");
                return true; // Tool has already reported itself as the correct tool. This includes a tier check in vanilla.
            }

            // Check 5: Sharp tool system
            boolean isSharpTool = SharpToolUtil.isSharpTool(stack);
            boolean requiresSharpTool = SharpToolUtil.requiresSharpTool(state);
            boolean sharpToolMatch = isSharpTool && requiresSharpTool;
            DebugUtil.debug(actionId, "HarvestBlockHandler.isUsingCorrectTool() - Check 5: Sharp tool system | isSharpTool=%s, requiresSharpTool=%s, match=%s", isSharpTool, requiresSharpTool, sharpToolMatch);
            if (sharpToolMatch)
            {
                DebugUtil.endAction(actionId, true, "HarvestBlockHandler.isUsingCorrectTool - Sharp tool match");
                return true; // Sharp tool can harvest plants that require sharp tools
            }

            // Check 6: Mining speed check (only for canMine checks)
            if (checkingCanMine)
            {
                float toolDestroySpeed = stack.getDestroySpeed(state);
                boolean fasterThanNormal = toolDestroySpeed > 1.0f;
                DebugUtil.debug(actionId, "HarvestBlockHandler.isUsingCorrectTool() - Check 6: Mining speed | toolDestroySpeed=%.2f, fasterThanNormal=%s", toolDestroySpeed, fasterThanNormal);
                if (fasterThanNormal)
                {
                    DebugUtil.endAction(actionId, true, "HarvestBlockHandler.isUsingCorrectTool - Faster mining speed");
                    return true; // Tool reported itself as harvesting faster than normal, in which case when checking if we can *mine* the block, we return true.
                }
            }

            // Check 7: Unknown tool requirements
            boolean isMineable = state.is(ModTags.Blocks.MINEABLE);
            DebugUtil.debug(actionId, "HarvestBlockHandler.isUsingCorrectTool() - Check 7: Block is mineable = %s", isMineable);
            if (!isMineable)
            {
                DebugUtil.endAction(actionId, true, "HarvestBlockHandler.isUsingCorrectTool - Unknown tool requirements");
                return true; // If we have no idea what tool can mine this block, we have to return true, as otherwise it's impossible to mine
            }

            // Final result: No conditions met
            DebugUtil.debug(actionId, "HarvestBlockHandler.isUsingCorrectTool() - All checks failed, denying access");
            DebugUtil.endAction(actionId, false, "HarvestBlockHandler.isUsingCorrectTool - All checks failed");
            return false; // None of our checks have confirmed we can mine this block, so we can't
        }
        catch (Exception e)
        {
            DebugUtil.endActionWithError(actionId, e, "HarvestBlockHandler.isUsingCorrectTool");
            throw e;
        }
    }

    private static float getDestroySpeed(BlockState state, @Nullable BlockPos pos, Player player)
    {
        return pos != null ? state.getDestroySpeed(player.level(), pos) : ((AbstractBlockStateAccessor) state).getDestroySpeed();
    }
}