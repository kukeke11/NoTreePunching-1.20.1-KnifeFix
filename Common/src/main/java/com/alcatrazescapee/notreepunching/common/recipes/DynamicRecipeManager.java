package com.alcatrazescapee.notreepunching.common.recipes;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.mojang.logging.LogUtils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import org.slf4j.Logger;

/**
 * Reload listener that injects dynamic recipes at the proper time in the reload cycle.
 * This replaces the unsafe mixin approach with proper Forge integration.
 */
public class DynamicRecipeManager implements PreparableReloadListener
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private final RegistryAccess registryAccess;

    public DynamicRecipeManager(RegistryAccess registryAccess)
    {
        this.registryAccess = registryAccess;
    }

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, 
                                         ProfilerFiller profilerFiller, ProfilerFiller profilerFiller2, 
                                         Executor executor, Executor executor2)
    {
        return preparationBarrier.wait(Unit.INSTANCE).thenRunAsync(() -> {
            try
            {
                LOGGER.debug("Dynamic recipe reload initiated");
                // The actual injection will happen when we have access to the RecipeManager
                // This is handled in the server events
            }
            catch (Exception e)
            {
                LOGGER.error("Error during dynamic recipe reload", e);
            }
        }, executor2);
    }

    /**
     * Dummy preparation result since we don't need to prepare anything
     */
    private static final class Unit
    {
        static final Unit INSTANCE = new Unit();
        private Unit() {}
    }
}