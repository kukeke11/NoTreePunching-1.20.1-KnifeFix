package com.alcatrazescapee.notreepunching.common.recipes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.logging.LogUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import com.alcatrazescapee.notreepunching.Config;
import com.alcatrazescapee.notreepunching.NoTreePunching;
import com.alcatrazescapee.notreepunching.common.ModTags;
import com.alcatrazescapee.notreepunching.platform.XPlatform;
import com.alcatrazescapee.notreepunching.util.Helpers;

/**
 * Safe recipe injection system using proper Forge events instead of unsafe mixin approach.
 * Replaces the dangerous direct RecipeManager mutation with event-based injection.
 */
@Mod.EventBusSubscriber(modid = NoTreePunching.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RecipeInjectionHandler
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<ResourceLocation, Recipe<?>> injectedRecipes = new HashMap<>();

    /**
     * Listen for resource reload events to inject our recipes safely.
     * This replaces the unsafe mixin approach with proper event handling.
     */
    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event)
    {
        event.addListener(new DynamicRecipeManager(event.getRegistryAccess()));
    }

    /**
     * Generates and injects dynamic recipes safely.
     * This method is now called through proper event system instead of unsafe mixins.
     */
    public static void injectRecipes(RecipeManager recipeManager, RegistryAccess registryAccess)
    {
        if (!Config.INSTANCE.enableDynamicRecipeReplacement.getAsBoolean())
        {
            LOGGER.info("Dynamic recipe replacement is disabled in config");
            return;
        }

        try
        {
            LOGGER.info("Starting safe recipe injection...");
            
            final Set<Item> logItems = new HashSet<>();
            final Set<Item> plankItems = new HashSet<>();

            // Safely gather items from tags with error handling
            try
            {
                BuiltInRegistries.ITEM.getTagOrEmpty(ItemTags.LOGS).forEach(holder -> logItems.add(holder.value()));
                BuiltInRegistries.ITEM.getTagOrEmpty(ItemTags.PLANKS).forEach(holder -> plankItems.add(holder.value()));
            }
            catch (Exception e)
            {
                LOGGER.error("Failed to gather items from tags, aborting recipe injection", e);
                return;
            }

            if (logItems.isEmpty() || plankItems.isEmpty())
            {
                LOGGER.warn("No log or plank items found in tags, skipping recipe injection");
                return;
            }

            final List<Recipe<?>> newRecipes = generateSawingRecipes(recipeManager, registryAccess, logItems, plankItems);
            
            if (newRecipes.isEmpty())
            {
                LOGGER.info("No eligible recipes found for conversion");
                return;
            }

            // Store injected recipes for tracking
            newRecipes.forEach(recipe -> injectedRecipes.put(recipe.getId(), recipe));
            
            LOGGER.info("Successfully generated {} sawing recipes", newRecipes.size());
        }
        catch (Exception e)
        {
            LOGGER.error("Failed to inject recipes safely", e);
        }
    }

    /**
     * Generate sawing recipes from existing log->plank recipes with proper error handling.
     */
    private static List<Recipe<?>> generateSawingRecipes(RecipeManager recipeManager, RegistryAccess registryAccess, 
                                                        Set<Item> logItems, Set<Item> plankItems)
    {
        final List<Recipe<?>> injectedRecipes = new ArrayList<>();

        // Safely iterate through existing recipes with error handling
        try
        {
            for (Recipe<?> recipe : recipeManager.getRecipes().stream().toList())
            {
                if (recipe.getType() != RecipeType.CRAFTING) continue;
                if (recipe.getSerializer() != RecipeSerializer.SHAPED_RECIPE && 
                    recipe.getSerializer() != RecipeSerializer.SHAPELESS_RECIPE) continue;
                if (recipe.getIngredients().size() != 1) continue;

                final Ingredient log = recipe.getIngredients().get(0);
                final ItemStack[] values = log.getItems();

                if (values.length == 0) continue;
                if (Arrays.stream(values).anyMatch(item -> !logItems.contains(item.getItem()))) continue;

                final ItemStack result = recipe.getResultItem(registryAccess);
                if (result.isEmpty() || !plankItems.contains(result.getItem())) continue;

                final Item plank = result.getItem();
                final ResourceLocation plankName = BuiltInRegistries.ITEM.getKey(plank);

                // Generate safer recipe IDs
                final ResourceLocation sawRecipeId = recipe.getId();
                final ResourceLocation weakSawRecipeId = Helpers.identifier("generated/weak_saw_%s_%s"
                    .formatted(plankName.getNamespace(), plankName.getPath()));

                try
                {
                    // Create saw recipe (4 planks)
                    injectedRecipes.add(createSawLogToPlankRecipe(sawRecipeId, ModTags.Items.SAWS, log, plank, 4));
                    
                    // Create weak saw recipe (2 planks)
                    injectedRecipes.add(createSawLogToPlankRecipe(weakSawRecipeId, ModTags.Items.WEAK_SAWS, log, plank, 2));
                }
                catch (Exception e)
                {
                    LOGGER.error("Failed to create sawing recipe for {}", plankName, e);
                }
            }
        }
        catch (Exception e)
        {
            LOGGER.error("Error occurred while generating sawing recipes", e);
        }

        return injectedRecipes;
    }

    /**
     * Create a saw recipe with proper error handling and validation.
     */
    private static Recipe<?> createSawLogToPlankRecipe(ResourceLocation id, TagKey<Item> saw, Ingredient log, Item plank, int count)
    {
        try
        {
            return XPlatform.INSTANCE.shapedToolDamagingRecipe(
                id,
                new ShapedRecipe(id, "", CraftingBookCategory.BUILDING, 1, 2, NonNullList.of(
                    Ingredient.EMPTY,
                    Ingredient.of(saw),
                    log
                ), new ItemStack(plank, count)), 
                Ingredient.of(saw));
        }
        catch (Exception e)
        {
            LOGGER.error("Failed to create saw recipe with ID {}", id, e);
            throw e;
        }
    }

    /**
     * Get all injected recipes for tracking and debugging.
     */
    public static Map<ResourceLocation, Recipe<?>> getInjectedRecipes()
    {
        return Map.copyOf(injectedRecipes);
    }

    /**
     * Clear injected recipes on reload.
     */
    public static void clearInjectedRecipes()
    {
        injectedRecipes.clear();
    }
}