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

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import com.alcatrazescapee.notreepunching.Config;
import com.alcatrazescapee.notreepunching.ForgeConfig;
import com.alcatrazescapee.notreepunching.common.ModTags;
import com.alcatrazescapee.notreepunching.platform.Platform;
import com.alcatrazescapee.notreepunching.platform.PlatformOverride;
import com.alcatrazescapee.notreepunching.util.SharpToolUtil;
import com.alcatrazescapee.notreepunching.util.ToolDamageUtil;

public class KnifeItem extends SwordItem
{
    private static final Logger LOGGER = LogUtils.getLogger();
    
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
        if (ForgeConfig.enableSharpToolDebugLogging.get())
        {
            LOGGER.debug("KnifeItem.isCorrectToolForDrops() called with stack: {} (item: {}), state: {} (block: {})", 
                        stack, stack.isEmpty() ? "EMPTY" : stack.getItem().getDescriptionId(),
                        state, state.getBlock().getDescriptionId());
        }
        
        // ALWAYS allow knives on plants - this is the core NTP behavior and must work regardless of config
        // This ensures backward compatibility and fixes the issue where knives can't harvest plants
        boolean isPlant = isPlantBlock(state);
        if (ForgeConfig.enableSharpToolDebugLogging.get())
        {
            LOGGER.debug("KnifeItem.isCorrectToolForDrops() isPlantBlock check: {}", isPlant);
        }
        
        if (isPlant)
        {
            if (ForgeConfig.enableSharpToolDebugLogging.get())
            {
                LOGGER.debug("KnifeItem.isCorrectToolForDrops() returning true - knife on plant block");
            }
            return true;
        }
        
        // Fall back to parent behavior for other blocks (swords work on cobwebs, etc.)
        boolean parentResult = super.isCorrectToolForDrops(stack, state);
        if (ForgeConfig.enableSharpToolDebugLogging.get())
        {
            LOGGER.debug("KnifeItem.isCorrectToolForDrops() falling back to parent behavior: {}", parentResult);
        }
        return parentResult;
    }
    
    /**
     * Check if a block state represents a plant that should be harvestable with knives.
     * This provides the core knife functionality independent of Sharp Tool System configuration.
     * Knives should ALWAYS work on plants - this is fundamental NTP behavior.
     */
    private boolean isPlantBlock(BlockState state)
    {
        if (ForgeConfig.enableSharpToolDebugLogging.get())
        {
            LOGGER.debug("KnifeItem.isPlantBlock() called with state: {} (block: {})", 
                        state, state.getBlock().getDescriptionId());
        }
        
        // Check core plant tags that knives should always work on
        // These checks work regardless of Sharp Tool System configuration
        boolean requiresSharpTool = state.is(ModTags.Blocks.REQUIRES_SHARP_TOOL);
        boolean isPlantFiberSource = state.is(ModTags.Blocks.PLANT_FIBER_SOURCES);
        boolean isSwordEfficient = state.is(BlockTags.SWORD_EFFICIENT);
        boolean isFlower = state.is(BlockTags.FLOWERS);
        boolean isSmallFlower = state.is(BlockTags.SMALL_FLOWERS);
        boolean isTallFlower = state.is(BlockTags.TALL_FLOWERS);
        boolean isCrop = state.is(BlockTags.CROPS);
        boolean isSapling = state.is(BlockTags.SAPLINGS);
        
        if (ForgeConfig.enableSharpToolDebugLogging.get())
        {
            LOGGER.debug("KnifeItem.isPlantBlock() tag checks for {}: REQUIRES_SHARP_TOOL={}, PLANT_FIBER_SOURCES={}, SWORD_EFFICIENT={}, FLOWERS={}, SMALL_FLOWERS={}, TALL_FLOWERS={}, CROPS={}, SAPLINGS={}", 
                        state.getBlock().getDescriptionId(), requiresSharpTool, isPlantFiberSource, isSwordEfficient, 
                        isFlower, isSmallFlower, isTallFlower, isCrop, isSapling);
        }
        
        boolean result = requiresSharpTool || isPlantFiberSource || isSwordEfficient || isFlower || isSmallFlower || isTallFlower || isCrop || isSapling;
        
        if (ForgeConfig.enableSharpToolDebugLogging.get())
        {
            LOGGER.debug("KnifeItem.isPlantBlock() returning: {} for block: {}", result, state.getBlock().getDescriptionId());
        }
        
        return result;
    }

    /**
     * Override to provide faster destroy speed for plant blocks.
     * This ensures knives harvest plants efficiently.
     * Knives ALWAYS get fast speed on plants, regardless of Sharp Tool System configuration.
     */
    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state)
    {
        if (ForgeConfig.enableSharpToolDebugLogging.get())
        {
            LOGGER.debug("KnifeItem.getDestroySpeed() called with stack: {} (item: {}), state: {} (block: {})", 
                        stack, stack.isEmpty() ? "EMPTY" : stack.getItem().getDescriptionId(),
                        state, state.getBlock().getDescriptionId());
        }
        
        // ALWAYS give knives fast speed on plants - this is core NTP behavior
        // This ensures knives break plants quickly regardless of configuration
        boolean isPlant = isPlantBlock(state);
        if (ForgeConfig.enableSharpToolDebugLogging.get())
        {
            LOGGER.debug("KnifeItem.getDestroySpeed() isPlantBlock check: {}", isPlant);
        }
        
        if (isPlant)
        {
            if (ForgeConfig.enableSharpToolDebugLogging.get())
            {
                LOGGER.debug("KnifeItem.getDestroySpeed() returning 15.0f - fast speed for knife on plant");
            }
            return 15.0f; // Fast speed for knives on plants (original behavior)
        }
        
        // Fall back to parent behavior for other blocks (swords have 1.5f speed on cobwebs)
        float parentSpeed = super.getDestroySpeed(stack, state);
        if (ForgeConfig.enableSharpToolDebugLogging.get())
        {
            LOGGER.debug("KnifeItem.getDestroySpeed() falling back to parent speed: {}", parentSpeed);
        }
        return parentSpeed;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity)
    {
        if (ForgeConfig.enableSharpToolDebugLogging.get())
        {
            LOGGER.debug("KnifeItem.mineBlock() called with stack: {} (item: {}), state: {} (block: {}), pos: {}, entity: {}, isClientSide: {}", 
                        stack, stack.isEmpty() ? "EMPTY" : stack.getItem().getDescriptionId(),
                        state, state.getBlock().getDescriptionId(), pos, 
                        entity != null ? entity.getClass().getSimpleName() : "null", level.isClientSide);
        }
        
        if (!level.isClientSide)
        {
            // Use existing ToolDamageUtil for damage calculation - preserves all existing logic
            final boolean shouldDamage = ToolDamageUtil.shouldDamageToolOnBlock(stack, level, state, pos);
            if (ForgeConfig.enableSharpToolDebugLogging.get())
            {
                LOGGER.debug("KnifeItem.mineBlock() shouldDamageToolOnBlock: {}", shouldDamage);
            }
            
            if (shouldDamage)
            {
                final int damageAmount = ToolDamageUtil.calculateToolDamage(stack, ToolDamageUtil.ToolUsage.BLOCK_MINING);
                if (ForgeConfig.enableSharpToolDebugLogging.get())
                {
                    LOGGER.debug("KnifeItem.mineBlock() calculated damage amount: {}", damageAmount);
                }
                ToolDamageUtil.damageToolSafely(stack, entity, damageAmount, InteractionHand.MAIN_HAND);
                if (ForgeConfig.enableSharpToolDebugLogging.get())
                {
                    LOGGER.debug("KnifeItem.mineBlock() tool damage applied");
                }
            }
        }
        
        if (ForgeConfig.enableSharpToolDebugLogging.get())
        {
            LOGGER.debug("KnifeItem.mineBlock() returning true");
        }
        return true;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand)
    {
        return Items.SHEARS.interactLivingEntity(stack, player, entity, hand);
    }
}