package org.bbrk24.amurians;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public object RubyMaterial : ArmorMaterial {
    // boots, leggings, chestplate, helmet respectively
    private val PROTECTION_AMOUNTS = arrayOf(2, 5, 7, 3);

    // For builtin armor it does a multiplier times a base durability array. That array is private,
    // so I can't directly use it here, but these numbers are the same as if the multiplier were 35.
    private val DURABILITIES = arrayOf(455, 525, 560, 385);

    private val REPAIR_INGREDIENT = Ingredient.ofItems(Initializer.RUBY);

    public override fun getDurability(slot: EquipmentSlot): Int {
        return DURABILITIES[slot.getEntitySlotId()];
    }

    public override fun getProtectionAmount(slot: EquipmentSlot): Int {
        return PROTECTION_AMOUNTS[slot.getEntitySlotId()];
    }

    public override fun getRepairIngredient(): Ingredient {
        return REPAIR_INGREDIENT;
    }

    public override fun getEnchantability(): Int {
        return 10;
    }

    public override fun getEquipSound(): SoundEvent {
        // FIXME: Placeholder until specific sounds are recorded
        return SoundEvents.ITEM_ARMOR_EQUIP_GENERIC;
    }

    public override fun getName(): String {
        return "ruby";
    }

    public override fun getToughness(): Float {
        return 1.0f;
    }

    public override fun getKnockbackResistance(): Float {
        return 0.0f;
    }
}
