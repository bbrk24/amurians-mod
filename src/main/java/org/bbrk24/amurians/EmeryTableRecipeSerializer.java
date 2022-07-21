package org.bbrk24.amurians;

import com.google.gson.JsonObject;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

// pretty much just copied from CuttingRecipe$Serializer, since its constructor is inaccessible here
public class EmeryTableRecipeSerializer implements RecipeSerializer<EmeryTableRecipe> {
    @Override
    public EmeryTableRecipe read(Identifier identifier, JsonObject jsonObject) {
        String group = JsonHelper.getString(jsonObject, "group", "");
        Ingredient ingredient =
            JsonHelper.hasArray(jsonObject, "ingredient")
                ? Ingredient.fromJson(JsonHelper.getArray(jsonObject, "ingredient"))
                : Ingredient.fromJson(JsonHelper.getObject(jsonObject, "ingredient"));
        String resultID = JsonHelper.getString(jsonObject, "result");
        int resultCount = JsonHelper.getInt(jsonObject, "count", 1);
        ItemStack resultStack = new ItemStack(
            Registry.ITEM.get(new Identifier(resultID)),
            resultCount
        );
        return new EmeryTableRecipe(identifier, group, ingredient, resultStack);
    }

    @Override
    public EmeryTableRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
        String string = packetByteBuf.readString();
        Ingredient ingredient = Ingredient.fromPacket(packetByteBuf);
        ItemStack itemStack = packetByteBuf.readItemStack();
        return new EmeryTableRecipe(identifier, string, ingredient, itemStack);
    }

    @Override
    public void write(PacketByteBuf packetByteBuf, EmeryTableRecipe cuttingRecipe) {
        packetByteBuf.writeString(cuttingRecipe.getGroup());
        cuttingRecipe.getInput().write(packetByteBuf);
        packetByteBuf.writeItemStack(cuttingRecipe.getOutput());
    }
}
