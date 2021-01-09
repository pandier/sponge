/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.item.recipe.smithing;

import com.google.gson.JsonObject;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.item.crafting.SingleItemRecipe;
import net.minecraft.item.crafting.SmithingRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import org.spongepowered.common.item.recipe.SpongeRecipeRegistration;
import org.spongepowered.common.item.recipe.ingredient.IngredientUtil;
import org.spongepowered.common.item.recipe.ingredient.ResultUtil;
import org.spongepowered.common.item.recipe.stonecutting.SpongeStonecuttingRecipe;
import org.spongepowered.common.util.Constants;

import java.util.function.Function;

public class SpongeSmithingRecipeSerializer<R extends SmithingRecipe> implements IRecipeSerializer<R> {

    public static IRecipeSerializer<?> SPONGE_SMITHING = SpongeRecipeRegistration.register("smithing", new SpongeSmithingRecipeSerializer<>());

    @SuppressWarnings("unchecked")
    @Override
    public R fromJson(ResourceLocation recipeId, JsonObject json) {
        final Ingredient base = IngredientUtil.spongeDeserialize(json.get(Constants.Recipe.SMITHING_BASE_INGREDIENT));
        final Ingredient addition = IngredientUtil.spongeDeserialize(json.get(Constants.Recipe.SMITHING_ADDITION_INGREDIENT));

        final Function<IInventory, ItemStack> resultFunction = ResultUtil.deserializeResultFunction(json);

        ItemStack itemstack = ShapedRecipe.itemFromJson(JSONUtils.getAsJsonObject(json, Constants.Recipe.RESULT));
        final ItemStack spongeStack = ResultUtil.deserializeItemStack(json.getAsJsonObject(Constants.Recipe.SPONGE_RESULT));

        return (R) new SpongeSmithingRecipe(recipeId, base, addition, spongeStack == null ? itemstack : spongeStack, resultFunction);
    }

    @SuppressWarnings("unchecked")
    @Override
    public R fromNetwork(ResourceLocation recipeId, PacketBuffer buffer) {
        throw new UnsupportedOperationException("custom serializer needs client side support");
    }

    public void toNetwork(PacketBuffer buffer, R recipe) {
        throw new UnsupportedOperationException("custom serializer needs client side support");
    }
}
