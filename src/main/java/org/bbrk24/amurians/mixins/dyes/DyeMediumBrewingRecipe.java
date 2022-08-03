package org.bbrk24.amurians.mixins.dyes;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.bbrk24.amurians.Initializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrewingRecipeRegistry.class)
public abstract class DyeMediumBrewingRecipe {
    private static boolean isWaterBottle(ItemStack bottle) {
        // don't count splash and lingering water bottles
        if (!bottle.isOf(Items.POTION)) {
            return false;
        }
        Potion effects = PotionUtil.getPotion(bottle);
        return effects == Potions.WATER;
    }

    @Inject(
        method = "isValidIngredient(Lnet/minecraft/item/ItemStack;)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void checkIngredient(ItemStack ingredient, CallbackInfoReturnable<Boolean> cir) {
        if (ingredient.isIn(Initializer.getBarkTag())) {
            cir.setReturnValue(true);
        }
    }

    @Inject(
        method = "hasRecipe(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void checkPotion(
        ItemStack potion,
        ItemStack ingredient,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (ingredient.isIn(Initializer.getBarkTag())) {
            cir.setReturnValue(isWaterBottle(potion));
        }
    }

    @Inject(
        method = "craft(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void craft(
        ItemStack ingredient,
        ItemStack potion,
        CallbackInfoReturnable<ItemStack> cir
    ) {
        if (ingredient.isIn(Initializer.getBarkTag()) && isWaterBottle(potion)) {
            cir.setReturnValue(new ItemStack(Initializer.getDyeMedium()));
        }
    }
}
