package com.alcatrazescapee.notreepunching;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.alcatrazescapee.notreepunching.client.ModSounds;
import com.alcatrazescapee.notreepunching.common.ModTags;
import com.alcatrazescapee.notreepunching.common.items.ModItems;
import com.alcatrazescapee.notreepunching.util.HarvestBlockHandler;
import com.alcatrazescapee.notreepunching.util.Helpers;
import com.alcatrazescapee.notreepunching.util.DebugUtil;

public final class EventHandler
{
    private static final Logger LOGGER = LogUtils.getLogger();
    
    public static boolean modifyHarvestCheck(Player player, BlockState state, @Nullable BlockPos pos, boolean canHarvest)
    {
        // Only start debug tracking if debugging is enabled for performance
        String actionId = null;
        if (ForgeConfig.enableSharpToolDebugLogging.get()) {
            actionId = DebugUtil.startAction(player, state, pos, "HARVEST_CHECK");
        }
        
        try
        {
            if (ForgeConfig.enableSharpToolDebugLogging.get()) {
                DebugUtil.debug(actionId, "EventHandler.modifyHarvestCheck() - Entry point | canHarvest=%s", canHarvest);
                DebugUtil.debug(actionId, "EventHandler.modifyHarvestCheck() - Player: %s", DebugUtil.getPlayerInfo(player));
                DebugUtil.debug(actionId, "EventHandler.modifyHarvestCheck() - Block: %s", DebugUtil.getBlockInfo(state, pos));
                DebugUtil.debug(actionId, "EventHandler.modifyHarvestCheck() - Tool: %s", DebugUtil.getDetailedToolInfo(player.getMainHandItem()));
            }
            
            boolean correctTool = HarvestBlockHandler.isUsingCorrectToolForDrops(state, pos, player);
            
            if (ForgeConfig.enableSharpToolDebugLogging.get()) {
                DebugUtil.debug(actionId, "EventHandler.modifyHarvestCheck() - HarvestBlockHandler.isUsingCorrectToolForDrops returned: %s", correctTool);
            }
            
            boolean result = canHarvest && correctTool;
            
            if (ForgeConfig.enableSharpToolDebugLogging.get()) {
                DebugUtil.debug(actionId, "EventHandler.modifyHarvestCheck() - Final calculation: %s && %s = %s", canHarvest, correctTool, result);
                DebugUtil.endAction(actionId, result, "EventHandler.modifyHarvestCheck");
            }
            
            return result;
        }
        catch (Exception e)
        {
            if (ForgeConfig.enableSharpToolDebugLogging.get()) {
                DebugUtil.endActionWithError(actionId, e, "EventHandler.modifyHarvestCheck");
            }
            throw e;
        }
    }

    public static float modifyBreakSpeed(Player player, BlockState state, @Nullable BlockPos pos, float speed)
    {
        // Only start debug tracking if debugging is enabled for performance
        String actionId = null;
        if (ForgeConfig.enableSharpToolDebugLogging.get()) {
            actionId = DebugUtil.startAction(player, state, pos, "BREAK_SPEED");
        }
        
        try
        {
            if (ForgeConfig.enableSharpToolDebugLogging.get()) {
                DebugUtil.debug(actionId, "EventHandler.modifyBreakSpeed() - Entry point | originalSpeed=%.2f", speed);
                DebugUtil.debug(actionId, "EventHandler.modifyBreakSpeed() - Player: %s", DebugUtil.getPlayerInfo(player));
                DebugUtil.debug(actionId, "EventHandler.modifyBreakSpeed() - Block: %s", DebugUtil.getBlockInfo(state, pos));
                DebugUtil.debug(actionId, "EventHandler.modifyBreakSpeed() - Tool: %s", DebugUtil.getDetailedToolInfo(player.getMainHandItem()));
            }
            
            boolean correctTool = HarvestBlockHandler.isUsingCorrectToolToMine(state, pos, player);
            
            if (ForgeConfig.enableSharpToolDebugLogging.get()) {
                DebugUtil.debug(actionId, "EventHandler.modifyBreakSpeed() - HarvestBlockHandler.isUsingCorrectToolToMine returned: %s", correctTool);
            }
            
            float result = correctTool ? speed : 0;
            
            if (ForgeConfig.enableSharpToolDebugLogging.get()) {
                DebugUtil.debug(actionId, "EventHandler.modifyBreakSpeed() - Final calculation: %s ? %.2f : 0 = %.2f", correctTool, speed, result);
                DebugUtil.endAction(actionId, result, "EventHandler.modifyBreakSpeed");
            }
            
            return result;
        }
        catch (Exception e)
        {
            if (ForgeConfig.enableSharpToolDebugLogging.get()) {
                DebugUtil.endActionWithError(actionId, e, "EventHandler.modifyBreakSpeed");
            }
            throw e;
        }
    }

    /**
     * @return If non-null, an interaction was done and the regular code flow should be prevented.
     */
    @Nullable
    public static InteractionResult onRightClickBlock(Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack stack, @Nullable Direction targetedFace)
    {
        final BlockState state = level.getBlockState(pos);
        // Use block tag instead of hardcoded SoundType.STONE for better mod compatibility
        if (Helpers.isItem(stack.getItem(), ModTags.Items.FLINT_KNAPPABLE) && 
            (state.is(ModTags.Blocks.KNAPPABLE_STONE) || state.getSoundType() == SoundType.STONE))
        {
            if (!level.isClientSide)
            {
                if (level.random.nextFloat() < Config.INSTANCE.flintKnappingConsumeChance.getAsFloat())
                {
                    if (level.random.nextFloat() < Config.INSTANCE.flintKnappingSuccessChance.getAsFloat())
                    {
                        Direction face = targetedFace == null ? Direction.UP : targetedFace;
                        Containers.dropItemStack(level, pos.getX() + 0.5 + face.getStepX() * 0.5, pos.getY() + 0.5 + face.getStepY() * 0.5, pos.getZ() + 0.5 + face.getStepZ() * 0.5, new ItemStack(ModItems.FLINT_SHARD.get(), 2));
                    }
                    stack.shrink(1);
                    player.setItemInHand(hand, stack);
                }
                level.playSound(null, pos, ModSounds.KNAPPING.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }
        return null;
    }

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        dispatcher.register(Commands.literal("notreepunchingReloadConfig").requires(c -> c.hasPermission(2)).executes(source -> {
            Config.INSTANCE.load();
            return Command.SINGLE_SUCCESS;
        }));
    }
}