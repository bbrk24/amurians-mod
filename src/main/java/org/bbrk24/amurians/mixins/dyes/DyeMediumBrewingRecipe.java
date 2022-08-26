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
