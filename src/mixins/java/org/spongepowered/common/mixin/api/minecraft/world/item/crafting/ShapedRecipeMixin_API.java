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
package org.spongepowered.common.mixin.api.minecraft.world.item.crafting;

import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import org.spongepowered.api.item.recipe.crafting.ShapedCraftingRecipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ShapedRecipe.class)
public abstract class ShapedRecipeMixin_API implements ShapedCraftingRecipe {

    // @formatter:off
    @Shadow @Final ShapedRecipePattern pattern;

    // @formatter:on


    @SuppressWarnings("ConstantConditions")
    @Override
    public org.spongepowered.api.item.recipe.crafting.Ingredient ingredient(final int x, final int y) {
        if (x < 0 || x >= this.width() || y < 0 || y >= this.height()) {
            throw new IndexOutOfBoundsException("Invalid ingredient predicate location");
        }

        final int recipeItemIndex = x + y * this.width();

        return ((org.spongepowered.api.item.recipe.crafting.Ingredient)(Object) this.pattern.ingredients().get(recipeItemIndex));
    }

    @Override
    public int width() {
        return this.pattern.width();
    }

    @Override
    public int height() {
        return this.pattern.height();
    }

}
