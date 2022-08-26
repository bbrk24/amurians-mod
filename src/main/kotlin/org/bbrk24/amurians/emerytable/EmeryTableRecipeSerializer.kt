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

import com.google.gson.JsonObject

import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.util.Identifier
import net.minecraft.util.JsonHelper
import net.minecraft.util.registry.Registry

object EmeryTableRecipeSerializer : RecipeSerializer<EmeryTableRecipe> {
    override fun read(id: Identifier, json: JsonObject): EmeryTableRecipe {
        // Kotlin can't parse the @Contract annotation, but this will never return null if the third
        // argument is non-null.
        val group = JsonHelper.getString(json, "group", "")!!

        val ingredient = Ingredient.fromJson(
            if (JsonHelper.hasArray(json, "ingredient"))
                JsonHelper.getArray(json, "ingredient")
            else
                JsonHelper.getObject(json, "ingredient")
        )
        val resultId = Identifier(JsonHelper.getString(json, "result"))
        val resultCount = JsonHelper.getInt(json, "count", 1)
        return EmeryTableRecipe(
            id,
            group,
            ingredient,
            ItemStack(Registry.ITEM.get(resultId), resultCount)
        )
    }

    override fun read(id: Identifier, buf: PacketByteBuf): EmeryTableRecipe {
        val group = buf.readString()
        val ingredient = Ingredient.fromPacket(buf)
        val output = buf.readItemStack()
        return EmeryTableRecipe(id, group, ingredient, output)
    }

    override fun write(buf: PacketByteBuf, recipe: EmeryTableRecipe) {
        buf.writeString(recipe.getGroup())
        recipe.getInput().write(buf)
        buf.writeItemStack(recipe.getOutput())
    }
}
