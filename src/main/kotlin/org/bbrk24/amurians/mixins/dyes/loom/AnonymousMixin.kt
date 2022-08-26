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

package org.bbrk24.amurians.mixins.dyes.loom

import net.minecraft.item.DyeItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

import org.bbrk24.amurians.PowderDyeItem
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Overwrite

@Mixin(targets = ["net.minecraft.screen.LoomScreenHandler$4"])
abstract class AnonymousMixin {
    @Overwrite
    fun canInsert(stack: ItemStack): Boolean {
        val item = stack.getItem()
        return item is DyeItem && item !is PowderDyeItem
    }
}
