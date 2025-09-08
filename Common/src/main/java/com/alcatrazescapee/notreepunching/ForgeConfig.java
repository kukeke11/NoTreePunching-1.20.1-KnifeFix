package com.alcatrazescapee.notreepunching;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

import com.alcatrazescapee.notreepunching.common.blocks.ModBlocks;
import com.alcatrazescapee.notreepunching.common.blocks.PotteryBlock;
import com.alcatrazescapee.notreepunching.platform.XPlatform;

/**
 * Forge Config API replacement for Epsilon config system.
 * Maintains same config structure and variable names for user compatibility.
 */
public final class ForgeConfig
{
    private static final Logger LOGGER = LogUtils.getLogger();

    // Config values - using the same names as the original Epsilon config
    public static final ForgeConfigSpec.BooleanValue enableDynamicRecipeReplacement;
    public static final ForgeConfigSpec.BooleanValue enableLooseRocksWorldGen;
    public static final ForgeConfigSpec.BooleanValue doBlocksMineWithoutCorrectTool;
    public static final ForgeConfigSpec.BooleanValue doInstantBreakBlocksMineWithoutCorrectTool;
    public static final ForgeConfigSpec.BooleanValue doBlocksDropWithoutCorrectTool;
    public static final ForgeConfigSpec.BooleanValue doInstantBreakBlocksDropWithoutCorrectTool;
    public static final ForgeConfigSpec.BooleanValue doInstantBreakBlocksDamageKnives;

    public static final ForgeConfigSpec.DoubleValue flintKnappingConsumeChance;
    public static final ForgeConfigSpec.DoubleValue flintKnappingSuccessChance;
    public static final ForgeConfigSpec.DoubleValue fireStarterFireStartChance;
    public static final ForgeConfigSpec.BooleanValue fireStarterCanMakeCampfire;
    public static final ForgeConfigSpec.BooleanValue fireStarterCanMakeSoulCampfire;
    public static final ForgeConfigSpec.BooleanValue largeVesselKeepsContentsWhenBroken;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> potteryBlockSequences;
    
    // Sharp tool system config values
    public static final ForgeConfigSpec.BooleanValue enableSharpToolSystem;
    public static final ForgeConfigSpec.BooleanValue requireSharpToolForPlants;

    private static final ForgeConfigSpec spec;

    // Cached config values for performance optimization
    private static volatile Boolean cachedEnableDynamicRecipes = null;
    private static volatile Boolean cachedEnableLooseRocks = null;
    private static volatile List<Block> cachedPotteryBlocks = null;

    // Cache invalidation timestamp
    private static volatile long lastCacheUpdate = 0;

    static
    {
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        // Recipes section
        builder.push("recipes");
        enableDynamicRecipeReplacement = builder
            .comment(
                "Enables dynamic replacement of log -> plank recipes with variants that use a axe or saw.",
                "These recipes are added dynamically and are not editable via datapacks. If this is disabled, no log -> plank recipes will be replaced!")
            .define("enableDynamicRecipeReplacement", true);

        // World generation section
        builder.pop().push("worldgen");
        enableLooseRocksWorldGen = builder
            .comment("Enables loose rock world gen added automatically to biomes.")
            .define("enableLooseRocksWorldGen", true);

        // Block harvesting section
        builder.pop().push("blockHarvesting");
        doBlocksMineWithoutCorrectTool = builder
            .comment("If blocks are mineable without the correct tool.")
            .define("doBlocksMineWithoutCorrectTool", false);
        doBlocksDropWithoutCorrectTool = builder
            .comment("If blocks drop their items without the correct tool.")
            .define("doBlocksDropWithoutCorrectTool", false);

        doInstantBreakBlocksDropWithoutCorrectTool = builder
            .comment("If blocks that break instantly are mineable without the correct tool.")
            .define("doInstantBreakBlocksDropWithoutCorrectTool", false);
        doInstantBreakBlocksMineWithoutCorrectTool = builder
            .comment("If blocks that break instantly drop their items without the correct tool.")
            .define("doInstantBreakBlocksMineWithoutCorrectTool", true);

        doInstantBreakBlocksDamageKnives = builder
            .comment("If blocks such as tall grass which break instantly consume durability when broken with a knife (only affects No Tree Punching knives)")
            .define("doInstantBreakBlocksDamageKnives", true);

        // Balance section
        builder.pop().push("balance");
        flintKnappingConsumeChance = builder
            .comment("The chance to consume a piece of flint when knapping")
            .defineInRange("flintKnappingConsumeChance", 0.4, 0.0, 1.0);
        flintKnappingSuccessChance = builder
            .comment("The chance to produce flint shards if a piece of flint has been consumed while knapping")
            .defineInRange("flintKnappingSuccessChance", 0.7, 0.0, 1.0);

        fireStarterFireStartChance = builder
            .comment("The chance for a fire starter to start fires")
            .defineInRange("fireStarterFireStartChance", 0.3, 0.0, 1.0);
        fireStarterCanMakeCampfire = builder
            .comment("If the fire starter can be used to make a campfire (with one '#notreepunching:fire_starter_logs' and three '#notreepunching:fire_starter_kindling'")
            .define("fireStarterCanMakeCampfire", true);
        fireStarterCanMakeSoulCampfire = builder
            .comment("If the fire starter can be used to make a soul campfire (with one '#notreepunching:fire_starter_logs', three '#notreepunching:fire_starter_kindling', and one '#notreepunching:fire_starter_soul_fire_catalyst'")
            .define("fireStarterCanMakeSoulCampfire", true);

        largeVesselKeepsContentsWhenBroken = builder
            .comment("If the large ceramic vessel block keeps it's contents when broken (as opposed to dropping them on the ground)")
            .define("largeVesselKeepsContentsWhenBroken", true);

        potteryBlockSequences = builder
            .comment(
                "The sequence of blocks that can be created with the clay tool.",
                "When the clay tool is used, if the block is present in this list, it may be converted to the next block in the list.",
                "If the next block is minecraft:air, the block will be destroyed (the clay tool will never try and convert air into something)")
            .define("potteryBlockSequences", List.of(
                "minecraft:clay",
                "notreepunching:pottery_worked",
                "notreepunching:pottery_large_vessel",
                "notreepunching:pottery_small_vessel",
                "notreepunching:pottery_bucket",
                "notreepunching:pottery_flower_pot",
                "minecraft:air"
            ));

        // Sharp tool system section
        builder.pop().push("sharpTools");
        enableSharpToolSystem = builder
            .comment(
                "Enables the tag-based sharp tool system for plant harvesting.",
                "When enabled, any item tagged with '#notreepunching:sharp_tools' can harvest plants that require sharp tools.",
                "When disabled, falls back to vanilla behavior for all tools except NTP knives (which use their original logic)."
            )
            .define("enableSharpToolSystem", true);
        
        requireSharpToolForPlants = builder
            .comment(
                "If plants require sharp tools for item drops.",
                "When enabled, plants tagged with '#notreepunching:requires_sharp_tool' will only drop items when harvested with sharp tools.",
                "When disabled, plants drop items when harvested with any tool or by hand."
            )
            .define("requireSharpToolForPlants", true);

        builder.pop();
        spec = builder.build();
    }

    /**
     * Register the config with Forge
     */
    public static void register()
    {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, spec, NoTreePunching.MOD_ID + ".toml");
        LOGGER.info("Registered NoTreePunching Forge Config");
    }

    /**
     * Get the pottery block sequence as parsed Block objects.
     * Results are cached for performance.
     */
    public static List<Block> getPotteryBlockSequence()
    {
        List<Block> cached = cachedPotteryBlocks;
        if (cached == null)
        {
            synchronized (ForgeConfig.class)
            {
                cached = cachedPotteryBlocks;
                if (cached == null)
                {
                    cached = parsePotteryBlocks();
                    cachedPotteryBlocks = cached;
                }
            }
        }
        return cached;
    }

    /**
     * Clear all caches - call when config reloads for performance optimization
     */
    public static void clearCache()
    {
        synchronized (ForgeConfig.class)
        {
            cachedEnableDynamicRecipes = null;
            cachedEnableLooseRocks = null;
            cachedPotteryBlocks = null;
            lastCacheUpdate = System.currentTimeMillis();
        }
    }

    /**
     * Get cached boolean value with automatic cache management
     */
    public static boolean getCachedBoolean(ForgeConfigSpec.BooleanValue configValue, String cacheName)
    {
        // For frequently accessed config values, consider caching
        // For now, just return the value directly as Forge already optimizes this
        return configValue.get();
    }

    private static List<Block> parsePotteryBlocks()
    {
        try
        {
            return potteryBlockSequences.get().stream()
                .map(name -> {
                    Block block = BuiltInRegistries.BLOCK.getOptional(new ResourceLocation(name)).orElse(null);
                    if (block == null)
                    {
                        LOGGER.warn("Invalid block in pottery sequence: '{}'", name);
                        return Blocks.AIR; // Fallback to air for invalid blocks
                    }
                    return block;
                })
                .toList();
        }
        catch (Exception e)
        {
            LOGGER.error("Failed to parse pottery block sequences, using defaults", e);
            // Return default sequence if parsing fails
            return List.of(
                Blocks.CLAY,
                ModBlocks.POTTERY.get(PotteryBlock.Variant.WORKED).get(),
                ModBlocks.POTTERY.get(PotteryBlock.Variant.LARGE_VESSEL).get(),
                ModBlocks.POTTERY.get(PotteryBlock.Variant.SMALL_VESSEL).get(),
                ModBlocks.POTTERY.get(PotteryBlock.Variant.BUCKET).get(),
                ModBlocks.POTTERY.get(PotteryBlock.Variant.FLOWER_POT).get(),
                Blocks.AIR
            );
        }
    }

    private ForgeConfig() {} // Static utility class
}