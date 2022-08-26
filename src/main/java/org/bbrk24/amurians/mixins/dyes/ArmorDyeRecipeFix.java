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
