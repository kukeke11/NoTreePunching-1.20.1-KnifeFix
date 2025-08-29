package com.alcatrazescapee.notreepunching.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import org.jetbrains.annotations.Nullable;

import com.alcatrazescapee.notreepunching.Config;

/**
 * Centralized tool damage logic with proper validation and error handling.
 * Replaces scattered tool damage code throughout the mod.
 */
public final class ToolDamageUtil
{
    /**
     * Damage a tool held by a living entity with proper error handling and validation.
     * 
     * @param stack The item stack to damage
     * @param entity The entity using the tool
     * @param amount The amount of damage to apply
     * @param hand The hand holding the tool (for break event)
     * @return true if the tool was damaged, false if it broke or couldn't be damaged
     */
    public static boolean damageToolSafely(ItemStack stack, LivingEntity entity, int amount, @Nullable InteractionHand hand)
    {
        if (stack.isEmpty() || !stack.isDamageableItem())
        {
            return false;
        }

        try
        {
            if (hand != null && entity instanceof Player)
            {
                stack.hurtAndBreak(amount, entity, livingEntity -> livingEntity.broadcastBreakEvent(hand));
            }
            else
            {
                return damageToolWithoutEntity(stack, amount);
            }
            return !stack.isEmpty();
        }
        catch (Exception e)
        {
            // Log error but don't crash
            return false;
        }
    }

    /**
     * Damage a tool without an entity using safe random source.
     * 
     * @param stack The item stack to damage
     * @param amount The amount of damage to apply
     * @return true if the tool was damaged, false if it broke
     */
    public static boolean damageToolWithoutEntity(ItemStack stack, int amount)
    {
        if (stack.isEmpty() || !stack.isDamageableItem())
        {
            return false;
        }

        try
        {
            if (stack.hurt(amount, new XoroshiroRandomSource(System.currentTimeMillis()), null))
            {
                stack.shrink(1);
                stack.setDamageValue(0);
                return false; // Tool broke
            }
            return true; // Tool was damaged but didn't break
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * Determines if a tool should take damage when used on a specific block.
     * Centralizes the logic for different tool types and block interactions.
     */
    public static boolean shouldDamageToolOnBlock(ItemStack stack, Level level, BlockState state, BlockPos pos)
    {
        if (stack.isEmpty() || !stack.isDamageableItem())
        {
            return false;
        }

        final float destroySpeed = state.getDestroySpeed(level, pos);
        
        // For instant-break blocks (destroySpeed == 0), check specific tool configs
        if (destroySpeed == 0.0F)
        {
            // Special handling for knives - use config setting
            if (isKnife(stack))
            {
                return Config.INSTANCE.doInstantBreakBlocksDamageKnives.getAsBoolean();
            }
            
            // For other tools, generally don't damage on instant-break blocks
            return false;
        }
        
        // For normal blocks that require time to break, tools should take damage
        return true;
    }

    /**
     * Check if an ItemStack is a knife from this mod.
     */
    private static boolean isKnife(ItemStack stack)
    {
        // This could be expanded to check for mod's knife items specifically
        return stack.getItem().getClass().getSimpleName().contains("Knife");
    }

    /**
     * Calculate the appropriate damage amount based on tool type and usage.
     */
    public static int calculateToolDamage(ItemStack stack, ToolUsage usage)
    {
        // Base damage amount - could be made configurable
        int baseDamage = 1;
        
        // Adjust based on usage type
        return switch (usage)
        {
            case BLOCK_MINING -> baseDamage;
            case CRAFTING -> baseDamage;
            case ENTITY_INTERACTION -> baseDamage;
            case SPECIAL_ABILITY -> baseDamage * 2; // Special abilities might cost more
        };
    }

    /**
     * Enum representing different types of tool usage for damage calculation.
     */
    public enum ToolUsage
    {
        BLOCK_MINING,
        CRAFTING, 
        ENTITY_INTERACTION,
        SPECIAL_ABILITY
    }

    private ToolDamageUtil() {} // Static utility class
}