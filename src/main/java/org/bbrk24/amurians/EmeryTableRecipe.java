package org.bbrk24.amurians;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CuttingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class EmeryTableRecipe extends CuttingRecipe {
    public EmeryTableRecipe(Identifier id, String group, Ingredient input, ItemStack output) {
        super(
            Initializer.EMERY_TABLE_RECIPE_TYPE,
            Initializer.EMERY_TABLE_RECIPE_SERIALIZER,
            id,
            group,
            input,
            output
        );
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        return this.input.test(inventory.getStack(0));
    }

    @Override
    public ItemStack createIcon() {
        return new ItemStack(Initializer.EMERY_TABLE);
    }

    public Ingredient getInput() {
        return this.input;
    }
}
