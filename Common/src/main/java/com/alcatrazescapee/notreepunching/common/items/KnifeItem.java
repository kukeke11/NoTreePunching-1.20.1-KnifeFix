package com.alcatrazescapee.notreepunching.common.items;

import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.state.BlockState;

import com.alcatrazescapee.notreepunching.Config;
import com.alcatrazescapee.notreepunching.platform.Platform;
import com.alcatrazescapee.notreepunching.platform.PlatformOverride;
import com.alcatrazescapee.notreepunching.util.SharpToolUtil;
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
     * Now delegates to SharpToolUtil for tag-based detection while preserving exact behavior.
     */
    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state)
    {
        // Use the new sharp tool system - knives are included in the sharp_tools tag
        if (SharpToolUtil.isSharpTool(stack) && SharpToolUtil.requiresSharpTool(state))
        {
            return true;
        }
        
        // Fall back to parent behavior for other blocks
        return super.isCorrectToolForDrops(stack, state);
    }

    /**
     * Override to provide faster destroy speed for plant blocks.
     * This ensures knives harvest plants efficiently.
     * Now delegates to SharpToolUtil for tag-based speed calculation.
     */
    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state)
    {
        // Use the new sharp tool system for destroy speed calculation
        float sharpToolSpeed = SharpToolUtil.getDestroySpeed(stack, state);
        if (sharpToolSpeed > 1.0f)
        {
            return sharpToolSpeed; // Returns 15.0f for sharp tools on applicable blocks
        }
        
        // Fall back to parent behavior for other blocks
        return super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity)
    {
        if (!level.isClientSide)
        {
            // Use existing ToolDamageUtil for damage calculation - preserves all existing logic
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