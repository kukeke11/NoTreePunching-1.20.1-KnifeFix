package com.alcatrazescapee.notreepunching;

import java.nio.file.Path;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import com.mojang.logging.LogUtils;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.common.MinecraftForge;
import org.slf4j.Logger;

import com.alcatrazescapee.notreepunching.platform.XPlatform;

/**
 * Compatibility layer that maintains the same interface as the original Epsilon config
 * while using Forge Config API under the hood.
 */
public final class Config
{
    private static final Logger LOGGER = LogUtils.getLogger();

    // Static config value constants for cleaner access patterns
    public static final ConfigValue<Boolean> ENABLE_DYNAMIC_RECIPE_REPLACEMENT = new ConfigValue<>(() -> ForgeConfig.enableDynamicRecipeReplacement.get());
    public static final ConfigValue<Boolean> ENABLE_LOOSE_ROCKS_WORLD_GEN = new ConfigValue<>(() -> ForgeConfig.enableLooseRocksWorldGen.get());
    public static final ConfigValue<Boolean> DO_BLOCKS_MINE_WITHOUT_CORRECT_TOOL = new ConfigValue<>(() -> ForgeConfig.doBlocksMineWithoutCorrectTool.get());
    public static final ConfigValue<Boolean> DO_INSTANT_BREAK_BLOCKS_MINE_WITHOUT_CORRECT_TOOL = new ConfigValue<>(() -> ForgeConfig.doInstantBreakBlocksMineWithoutCorrectTool.get());
    public static final ConfigValue<Boolean> DO_BLOCKS_DROP_WITHOUT_CORRECT_TOOL = new ConfigValue<>(() -> ForgeConfig.doBlocksDropWithoutCorrectTool.get());
    public static final ConfigValue<Boolean> DO_INSTANT_BREAK_BLOCKS_DROP_WITHOUT_CORRECT_TOOL = new ConfigValue<>(() -> ForgeConfig.doInstantBreakBlocksDropWithoutCorrectTool.get());
    public static final ConfigValue<Boolean> DO_INSTANT_BREAK_BLOCKS_DAMAGE_KNIVES = new ConfigValue<>(() -> ForgeConfig.doInstantBreakBlocksDamageKnives.get());

    public static final ConfigValue<Float> FLINT_KNAPPING_CONSUME_CHANCE = new ConfigValue<>(() -> ForgeConfig.flintKnappingConsumeChance.get().floatValue());
    public static final ConfigValue<Float> FLINT_KNAPPING_SUCCESS_CHANCE = new ConfigValue<>(() -> ForgeConfig.flintKnappingSuccessChance.get().floatValue());
    public static final ConfigValue<Float> FIRE_STARTER_FIRE_START_CHANCE = new ConfigValue<>(() -> ForgeConfig.fireStarterFireStartChance.get().floatValue());
    public static final ConfigValue<Boolean> FIRE_STARTER_CAN_MAKE_CAMPFIRE = new ConfigValue<>(() -> ForgeConfig.fireStarterCanMakeCampfire.get());
    public static final ConfigValue<Boolean> FIRE_STARTER_CAN_MAKE_SOUL_CAMPFIRE = new ConfigValue<>(() -> ForgeConfig.fireStarterCanMakeSoulCampfire.get());
    public static final ConfigValue<Boolean> LARGE_VESSEL_KEEPS_CONTENTS_WHEN_BROKEN = new ConfigValue<>(() -> ForgeConfig.largeVesselKeepsContentsWhenBroken.get());
    public static final ConfigValue<List<Block>> POTTERY_BLOCK_SEQUENCES = new ConfigValue<>(ForgeConfig::getPotteryBlockSequence);

    // Keep INSTANCE for backward compatibility during transition
    @Deprecated
    public static final Config INSTANCE = new Config();
    
    // Instance fields for backward compatibility - will be removed in future version
    @Deprecated
    public final ConfigValue<Boolean> enableDynamicRecipeReplacement = ENABLE_DYNAMIC_RECIPE_REPLACEMENT;
    @Deprecated
    public final ConfigValue<Boolean> enableLooseRocksWorldGen = ENABLE_LOOSE_ROCKS_WORLD_GEN;
    @Deprecated
    public final ConfigValue<Boolean> doBlocksMineWithoutCorrectTool = DO_BLOCKS_MINE_WITHOUT_CORRECT_TOOL;
    @Deprecated
    public final ConfigValue<Boolean> doInstantBreakBlocksMineWithoutCorrectTool = DO_INSTANT_BREAK_BLOCKS_MINE_WITHOUT_CORRECT_TOOL;
    @Deprecated
    public final ConfigValue<Boolean> doBlocksDropWithoutCorrectTool = DO_BLOCKS_DROP_WITHOUT_CORRECT_TOOL;
    @Deprecated
    public final ConfigValue<Boolean> doInstantBreakBlocksDropWithoutCorrectTool = DO_INSTANT_BREAK_BLOCKS_DROP_WITHOUT_CORRECT_TOOL;
    @Deprecated
    public final ConfigValue<Boolean> doInstantBreakBlocksDamageKnives = DO_INSTANT_BREAK_BLOCKS_DAMAGE_KNIVES;
    @Deprecated
    public final ConfigValue<Float> flintKnappingConsumeChance = FLINT_KNAPPING_CONSUME_CHANCE;
    @Deprecated
    public final ConfigValue<Float> flintKnappingSuccessChance = FLINT_KNAPPING_SUCCESS_CHANCE;
    @Deprecated
    public final ConfigValue<Float> fireStarterFireStartChance = FIRE_STARTER_FIRE_START_CHANCE;
    @Deprecated
    public final ConfigValue<Boolean> fireStarterCanMakeCampfire = FIRE_STARTER_CAN_MAKE_CAMPFIRE;
    @Deprecated
    public final ConfigValue<Boolean> fireStarterCanMakeSoulCampfire = FIRE_STARTER_CAN_MAKE_SOUL_CAMPFIRE;
    @Deprecated
    public final ConfigValue<Boolean> largeVesselKeepsContentsWhenBroken = LARGE_VESSEL_KEEPS_CONTENTS_WHEN_BROKEN;
    @Deprecated
    public final ConfigValue<List<Block>> potteryBlockSequences = POTTERY_BLOCK_SEQUENCES;

    static 
    {
        // Register config reload listener
        MinecraftForge.EVENT_BUS.addListener((ModConfigEvent.Reloading event) -> {
            if (NoTreePunching.MOD_ID.equals(event.getConfig().getModId()))
            {
                ForgeConfig.clearCache();
                LOGGER.info("NoTreePunching config reloaded");
            }
        });
    }

    private Config() {
        // Private constructor for singleton pattern
    }

    public static void load()
    {
        // With Forge Config API, loading is handled automatically
        // This method is kept for compatibility with existing code
        LOGGER.info("NoTreePunching Config is managed by Forge Config API");
        ForgeConfig.register();
    }

    /**
     * Functional interface for float suppliers to maintain original interface
     */
    @FunctionalInterface
    public interface FloatSupplier
    {
        float getAsFloat();
        
        default boolean getAsBoolean()
        {
            return getAsFloat() != 0.0f;
        }
    }

    /**
     * Config value wrapper that maintains the original Epsilon interface
     */
    public static class ConfigValue<T>
    {
        private final Supplier<T> supplier;

        public ConfigValue(Supplier<T> supplier)
        {
            this.supplier = supplier;
        }

        public T get()
        {
            return supplier.get();
        }

        public boolean getAsBoolean()
        {
            T value = get();
            if (value instanceof Boolean bool)
            {
                return bool;
            }
            else if (value instanceof Number num)
            {
                return num.doubleValue() != 0.0;
            }
            return value != null;
        }

        public float getAsFloat()
        {
            T value = get();
            if (value instanceof Number num)
            {
                return num.floatValue();
            }
            return 0.0f;
        }

        public double getAsDouble()
        {
            T value = get();
            if (value instanceof Number num)
            {
                return num.doubleValue();
            }
            return 0.0;
        }
    }
}