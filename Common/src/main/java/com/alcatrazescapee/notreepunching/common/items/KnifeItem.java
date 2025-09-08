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
import net.minecraft.world.level.block.state.BlockState;

import com.alcatrazescapee.notreepunching.Config;
import com.alcatrazescapee.notreepunching.common.ModTags;
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
     * Knives ALWAYS work on plants, regardless of Sharp Tool System configuration (backward compatibility).
     */
    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state)
    {
        // ALWAYS allow knives on plants - this is the core NTP behavior and must work regardless of config
        // This ensures backward compatibility and fixes the issue where knives can't harvest plants
        if (isPlantBlock(state))
        {
            return true;
        }
        
        // Fall back to parent behavior for other blocks (swords work on cobwebs, etc.)
        return super.isCorrectToolForDrops(stack, state);
    }
    
    /**
     * Check if a block state represents a plant that should be harvestable with knives.
     * This provides the core knife functionality independent of Sharp Tool System configuration.
     * Knives should ALWAYS work on plants - this is fundamental NTP behavior.
     */
    private boolean isPlantBlock(BlockState state)
    {
        // Check core plant tags that knives should always work on
        // These checks work regardless of Sharp Tool System configuration
        return state.is(ModTags.Blocks.REQUIRES_SHARP_TOOL) || 
               state.is(ModTags.Blocks.PLANT_FIBER_SOURCES) ||
               state.is(BlockTags.SWORD_EFFICIENT) ||
               // Also check common plant tags to ensure comprehensive coverage
               state.is(BlockTags.FLOWERS) ||
               state.is(BlockTags.SMALL_FLOWERS) ||
               state.is(BlockTags.TALL_FLOWERS) ||
               state.is(BlockTags.CROPS) ||
               state.is(BlockTags.SAPLINGS);
    }

    /**
     * Override to provide faster destroy speed for plant blocks.
     * This ensures knives harvest plants efficiently.
     * Knives ALWAYS get fast speed on plants, regardless of Sharp Tool System configuration.
     */
    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state)
    {
        // ALWAYS give knives fast speed on plants - this is core NTP behavior
        // This ensures knives break plants quickly regardless of configuration
        if (isPlantBlock(state))
        {
            return 15.0f; // Fast speed for knives on plants (original behavior)
        }
        
        // Fall back to parent behavior for other blocks (swords have 1.5f speed on cobwebs)
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