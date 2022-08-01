package org.bbrk24.amurians

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
