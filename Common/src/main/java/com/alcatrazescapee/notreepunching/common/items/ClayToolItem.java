package com.alcatrazescapee.notreepunching.common.items;


import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import com.alcatrazescapee.notreepunching.Config;
import com.alcatrazescapee.notreepunching.util.Helpers;
import com.alcatrazescapee.notreepunching.platform.Platform;
import com.alcatrazescapee.notreepunching.platform.PlatformOverride;

public class ClayToolItem extends TieredItem
{
    public static ItemStack interactWithBlock(LevelAccessor level, BlockPos pos, BlockState state, @Nullable Player player, @Nullable InteractionHand hand, ItemStack stack)
    {
        final List<Block> sequence = Config.INSTANCE.potteryBlockSequences.get();
        for (int i = 0; i < sequence.size() - 1; i++)
        {
            if (state.getBlock() == sequence.get(i) && state.getBlock() != Blocks.AIR)
            {
                final Block replacement = sequence.get(i + 1);
                level.setBlock(pos, replacement.defaultBlockState(), 3);
                level.playSound(null, pos, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 0.5F, 1.0F);
                stack = Helpers.hurtAndBreak(player, hand, stack, 1);
                return stack;
            }
        }
        return stack;
    }

    public ClayToolItem()
    {
        super(Tiers.WOOD, new Properties());
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        final LevelAccessor world = context.getLevel();
        if (!world.isClientSide())
        {
            final BlockPos pos = context.getClickedPos();
            interactWithBlock(world, pos, world.getBlockState(pos), context.getPlayer(), context.getHand(), context.getItemInHand());
        }
        return InteractionResult.SUCCESS;
    }

    @PlatformOverride(Platform.FORGE)
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment)
    {
        return enchantment.category == EnchantmentCategory.BREAKABLE;
    }
}