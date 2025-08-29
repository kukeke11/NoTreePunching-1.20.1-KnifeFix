package com.alcatrazescapee.notreepunching.common.recipes;

import java.util.Optional;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Function3;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Nullable;

import com.alcatrazescapee.notreepunching.platform.XPlatform;
import com.alcatrazescapee.notreepunching.util.Helpers;
import com.alcatrazescapee.notreepunching.util.ToolDamageUtil;

public abstract class ToolDamagingRecipe implements DelegateRecipe<CraftingContainer>, CraftingRecipe
{
    private final ResourceLocation id;
    private final Recipe<?> recipe;
    private @Nullable final Ingredient tool;

    protected ToolDamagingRecipe(ResourceLocation id, Recipe<?> recipe, @Nullable Ingredient tool)
    {
        this.id = id;
        this.recipe = recipe;
        this.tool = tool;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer container)
    {
        final NonNullList<ItemStack> items = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
        
        for (int i = 0; i < items.size(); i++)
        {
            try
            {
                final ItemStack stack = container.getItem(i);
                if (stack.isEmpty()) continue;
                
                // Check for crafting remainder first (like buckets -> empty buckets)
                final ItemStack remainder = XPlatform.INSTANCE.getCraftingRemainder(stack);
                if (!remainder.isEmpty())
                {
                    items.set(i, remainder);
                }
                // Handle tool damage with proper validation
                else if (stack.isDamageableItem() && (tool == null || tool.test(stack)))
                {
                    // Use the new centralized tool damage utility for safety
                    ItemStack damagedStack = stack.copy();
                    if (ToolDamageUtil.damageToolWithoutEntity(damagedStack, 1))
                    {
                        items.set(i, damagedStack);
                    }
                    // If tool broke, don't set any remainder (empty slot)
                }
            }
            catch (Exception e)
            {
                // Log error but continue processing other items
                // This prevents one bad item from breaking the entire crafting result
                continue;
            }
        }
        
        return items;
    }

    @Override
    public ResourceLocation getId()
    {
        return id;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Recipe<CraftingContainer> delegate()
    {
        return (Recipe<CraftingContainer>) recipe;
    }

    @Override
    public CraftingBookCategory category()
    {
        return CraftingBookCategory.MISC;
    }

    public static class Shaped extends ToolDamagingRecipe
    {
        public Shaped(ResourceLocation id, Recipe<?> recipe, @Nullable Ingredient tool)
        {
            super(id, recipe, tool);
        }

        @Override
        public RecipeSerializer<?> getSerializer()
        {
            return ModRecipes.SHAPED_TOOL_DAMAGING.get();
        }
    }

    public static class Shapeless extends ToolDamagingRecipe
    {
        public Shapeless(ResourceLocation id, Recipe<?> recipe, @Nullable Ingredient tool)
        {
            super(id, recipe, tool);
        }

        @Override
        public RecipeSerializer<?> getSerializer()
        {
            return ModRecipes.SHAPELESS_TOOL_DAMAGING.get();
        }
    }

    public record Serializer<T extends ToolDamagingRecipe>(Function3<ResourceLocation, Recipe<?>, Ingredient, T> factory) implements RecipeSerializerImpl<T>
    {
        @Override
        public T fromJson(ResourceLocation recipeId, JsonObject json, RecipeSerializerImpl.Context context)
        {
            final Recipe<?> recipe = context.fromJson(recipeId, GsonHelper.getAsJsonObject(json, "recipe"));
            final Ingredient tool = json.has("tool") ? Ingredient.fromJson(json.get("tool")) : null;
            return factory.apply(recipeId, recipe, tool);
        }

        @Override
        public T fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer)
        {
            final Recipe<?> recipe = ClientboundUpdateRecipesPacket.fromNetwork(buffer);
            final Ingredient tool = buffer.readOptional(Ingredient::fromNetwork).orElse(null);
            return factory.apply(recipeId, recipe, tool);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ToolDamagingRecipe recipe)
        {
            ClientboundUpdateRecipesPacket.toNetwork(buffer, recipe.delegate());
            buffer.writeOptional(Optional.ofNullable(recipe.tool), (b, i) -> i.toNetwork(b));
        }
    }
}
