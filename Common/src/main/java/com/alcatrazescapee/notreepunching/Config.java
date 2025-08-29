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
public enum Config
{
    INSTANCE;

    private static final Logger LOGGER = LogUtils.getLogger();

    // Config value wrappers that maintain the original interface
    public final ConfigValue<Boolean> enableDynamicRecipeReplacement = new ConfigValue<>(() -> ForgeConfig.enableDynamicRecipeReplacement.get());
    public final ConfigValue<Boolean> enableLooseRocksWorldGen = new ConfigValue<>(() -> ForgeConfig.enableLooseRocksWorldGen.get());
    public final ConfigValue<Boolean> doBlocksMineWithoutCorrectTool = new ConfigValue<>(() -> ForgeConfig.doBlocksMineWithoutCorrectTool.get());
    public final ConfigValue<Boolean> doInstantBreakBlocksMineWithoutCorrectTool = new ConfigValue<>(() -> ForgeConfig.doInstantBreakBlocksMineWithoutCorrectTool.get());
    public final ConfigValue<Boolean> doBlocksDropWithoutCorrectTool = new ConfigValue<>(() -> ForgeConfig.doBlocksDropWithoutCorrectTool.get());
    public final ConfigValue<Boolean> doInstantBreakBlocksDropWithoutCorrectTool = new ConfigValue<>(() -> ForgeConfig.doInstantBreakBlocksDropWithoutCorrectTool.get());
    public final ConfigValue<Boolean> doInstantBreakBlocksDamageKnives = new ConfigValue<>(() -> ForgeConfig.doInstantBreakBlocksDamageKnives.get());

    public final ConfigValue<Float> flintKnappingConsumeChance = new ConfigValue<>(() -> ForgeConfig.flintKnappingConsumeChance.get().floatValue());
    public final ConfigValue<Float> flintKnappingSuccessChance = new ConfigValue<>(() -> ForgeConfig.flintKnappingSuccessChance.get().floatValue());
    public final ConfigValue<Float> fireStarterFireStartChance = new ConfigValue<>(() -> ForgeConfig.fireStarterFireStartChance.get().floatValue());
    public final ConfigValue<Boolean> fireStarterCanMakeCampfire = new ConfigValue<>(() -> ForgeConfig.fireStarterCanMakeCampfire.get());
    public final ConfigValue<Boolean> fireStarterCanMakeSoulCampfire = new ConfigValue<>(() -> ForgeConfig.fireStarterCanMakeSoulCampfire.get());
    public final ConfigValue<Boolean> largeVesselKeepsContentsWhenBroken = new ConfigValue<>(() -> ForgeConfig.largeVesselKeepsContentsWhenBroken.get());
    public final ConfigValue<List<Block>> potteryBlockSequences = new ConfigValue<>(ForgeConfig::getPotteryBlockSequence);

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

    public void load()
    {
        // With Forge Config API, loading is handled automatically
        // This method is kept for compatibility with existing code
        // Config is already registered during mod initialization, no need to register again
        LOGGER.info("NoTreePunching Config is managed by Forge Config API");
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