/*
 * Copyright 2022 William Baker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bbrk24.amurians.emerytable

import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.recipe.CuttingRecipe
import net.minecraft.recipe.Ingredient
import net.minecraft.util.Identifier
import net.minecraft.world.World

import org.bbrk24.amurians.Initializer

class EmeryTableRecipe(
    id: Identifier,
    group: String,
    input: Ingredient,
    output: ItemStack
) : CuttingRecipe(
    Initializer.EMERY_TABLE_RECIPE_TYPE,
    EmeryTableRecipeSerializer,
    id,
    group,
    input,
    output
) {
    override fun matches(inventory: Inventory, world: World) = input.test(inventory.getStack(0))
    override fun createIcon() = ItemStack(Initializer.EMERY_TABLE)

    fun getInput() = input
}
