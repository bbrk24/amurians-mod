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

package org.bbrk24.amurians

import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.ArmorMaterial
import net.minecraft.recipe.Ingredient
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents

object RubyMaterial : ArmorMaterial {
    // boots, leggings, chestplate, helmet respectively
    private val PROTECTION_AMOUNTS = arrayOf(2, 5, 7, 3)

    // For builtin armor it does a multiplier times a base durability array. That array is private,
    // so I can't directly use it here, but these numbers are the same as if the multiplier were 35.
    private val DURABILITIES = arrayOf(455, 525, 560, 385)

    private val REPAIR_INGREDIENT = Ingredient.ofItems(Initializer.RUBY)

    override fun getDurability(slot: EquipmentSlot) = DURABILITIES[slot.getEntitySlotId()]

    override fun getProtectionAmount(slot: EquipmentSlot): Int {
        return PROTECTION_AMOUNTS[slot.getEntitySlotId()]
    }

    override fun getRepairIngredient() = REPAIR_INGREDIENT
    override fun getEnchantability() = 10
    override fun getName() = "ruby"
    override fun getToughness() = 1.0f
    override fun getKnockbackResistance() = 0.0f

    // FIXME: Placeholder until specific sounds are recorded
    override fun getEquipSound() = SoundEvents.ITEM_ARMOR_EQUIP_GENERIC
}
