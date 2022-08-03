package org.bbrk24.amurians.mixins.dyes;

import net.minecraft.item.DyeItem;
import net.minecraft.recipe.ArmorDyeRecipe;

import org.bbrk24.amurians.PowderDyeItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ArmorDyeRecipe.class)
public abstract class ArmorDyeRecipeFix {
    @ModifyConstant(
        method = "matches(Lnet/minecraft/inventory/CraftingInventory;Lnet/minecraft/world/World;)Z",
        constant = @Constant(classValue = DyeItem.class)
    )
    public boolean isDyeItem(Object obj, Class<DyeItem> c) {
        return obj instanceof DyeItem && !(obj instanceof PowderDyeItem);
    }
}
