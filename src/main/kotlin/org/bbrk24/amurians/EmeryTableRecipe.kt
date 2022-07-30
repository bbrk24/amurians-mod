package org.bbrk24.amurians

import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.recipe.CuttingRecipe
import net.minecraft.recipe.Ingredient
import net.minecraft.util.Identifier
import net.minecraft.world.World

class EmeryTableRecipe(
    id: Identifier,
    group: String,
    input: Ingredient,
    output: ItemStack
) : CuttingRecipe(
    Initializer.EMERY_TABLE_RECIPE_TYPE,
    Initializer.EMERY_TABLE_RECIPE_SERIALIZER,
    id,
    group,
    input,
    output
) {
    override fun matches(inventory: Inventory, world: World): Boolean {
        return input.test(inventory.getStack(0))
    }

    override fun createIcon(): ItemStack {
        return ItemStack(Initializer.EMERY_TABLE)
    }

    fun getInput(): Ingredient {
        return input
    }
}
