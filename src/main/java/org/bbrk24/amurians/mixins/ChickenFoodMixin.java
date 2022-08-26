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
