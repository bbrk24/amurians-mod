package org.bbrk24.amurians.mixins.dyes.fireworks;

import net.minecraft.item.DyeItem;
import net.minecraft.recipe.FireworkStarFadeRecipe;

import org.bbrk24.amurians.PowderDyeItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(FireworkStarFadeRecipe.class)
public abstract class FireworkStarFadeRecipeMixin {
    @ModifyConstant(
        method = "matches(Lnet/minecraft/inventory/CraftingInventory;Lnet/minecraft/world/World;)Z",
        constant = @Constant(classValue = DyeItem.class)
    )
    public Class<? extends DyeItem> getDyeItemClass(Object obj, Class<DyeItem> c) {
        return PowderDyeItem.class;
    }
}
