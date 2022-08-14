package org.bbrk24.amurians.mixins;

import java.util.Arrays;

import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.item.ItemConvertible;

import org.bbrk24.amurians.Initializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ChickenEntity.class)
public abstract class ChickenFoodMixin {
    @ModifyArg(
        method = "<clinit>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/recipe/Ingredient;ofItems([Lnet/minecraft/item/ItemConvertible;)Lnet/minecraft/recipe/Ingredient;"
        ),
        index = 0
    )
    private static ItemConvertible[] addHishaiSeeds(ItemConvertible[] seeds) {
        ItemConvertible[] newArr = Arrays.copyOf(seeds, seeds.length + 1);
        newArr[seeds.length] = Initializer.getHishaiSeeds();
        return newArr;
    }
}
