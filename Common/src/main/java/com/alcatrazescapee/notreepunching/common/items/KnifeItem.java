package com.alcatrazescapee.notreepunching.common.items;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import com.alcatrazescapee.notreepunching.Config;
import com.alcatrazescapee.notreepunching.platform.Platform;
import com.alcatrazescapee.notreepunching.platform.PlatformOverride;
import com.alcatrazescapee.notreepunching.util.ToolDamageUtil;

public class KnifeItem extends SwordItem
{
    public KnifeItem(Tier tier, int attackDamage, float attackSpeed, Properties properties)
    {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @PlatformOverride(Platform.FORGE)
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment)
    {
        return enchantment.category == EnchantmentCategory.BREAKABLE || enchantment.category == EnchantmentCategory.WEAPON;
    }

    /**
     * Override to make knives effective against plant-type blocks.
     * This allows knives to be recognized as the correct tool for harvesting plants.
     */
    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state)
    {
        // Check if it's a plant-type block that knives should be able to harvest
        if (isPlantBlock(state))
        {
            return true;
        }
        
        // Fall back to parent behavior for other blocks
        return super.isCorrectToolForDrops(stack, state);
    }

    /**
     * Override to provide faster destroy speed for plant blocks.
     * This ensures knives harvest plants efficiently.
     */
    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state)
    {
        // For plant blocks, provide fast destroy speed (similar to shears on leaves)
        if (isPlantBlock(state))
        {
            return 15.0f; // Fast but not instant
        }
        
        // Fall back to parent behavior for other blocks
        return super.getDestroySpeed(stack, state);
    }

    /**
     * Helper method to identify plant-type blocks that knives should be effective against.
     */
    private boolean isPlantBlock(BlockState state)
    {
        Block block = state.getBlock();
        
        // Check for common plant blocks
        return state.is(BlockTags.FLOWERS) ||
               state.is(BlockTags.SMALL_FLOWERS) ||
               block == Blocks.GRASS ||
               block == Blocks.TALL_GRASS ||
               block == Blocks.FERN ||
               block == Blocks.LARGE_FERN ||
               block == Blocks.DEAD_BUSH ||
               block == Blocks.SEAGRASS ||
               block == Blocks.TALL_SEAGRASS ||
               state.is(BlockTags.CROPS) ||
               state.is(BlockTags.SAPLINGS);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity)
    {
        if (!level.isClientSide)
        {
            final boolean shouldDamage = ToolDamageUtil.shouldDamageToolOnBlock(stack, level, state, pos);
            if (shouldDamage)
            {
                final int damageAmount = ToolDamageUtil.calculateToolDamage(stack, ToolDamageUtil.ToolUsage.BLOCK_MINING);
                ToolDamageUtil.damageToolSafely(stack, entity, damageAmount, InteractionHand.MAIN_HAND);
            }
        }
        return true;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand)
    {
        return Items.SHEARS.interactLivingEntity(stack, player, entity, hand);
    }
}