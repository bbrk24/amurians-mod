package org.bbrk24.amurians;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public class RubyMaterial implements ArmorMaterial {
    private static final int[] PROTECTION_AMOUNTS = {
        2, // boots
        5, // leggings
        7, // chestplate
        3  // helmet
    };

    // For builtin armor it does a multiplier times a base durability array. That array is private,
    // so I can't directly use it here, but these numbers are the same as if the multiplier were 35.
    private static final int[] DURABILITIES = {
        455, // boots
        525, // leggings
        560, // chestplate
        385  // helmet
    };

    protected static RubyMaterial instance = new RubyMaterial();

    private RubyMaterial() {
    }

    public static RubyMaterial getInstance() {
        return instance;
    }

    @Override
    public int getDurability(EquipmentSlot slot) {
        return DURABILITIES[slot.getEntitySlotId()];
    }

    @Override
    public int getProtectionAmount(EquipmentSlot slot) {
        return PROTECTION_AMOUNTS[slot.getEntitySlotId()];
    }

    @Override
    public int getEnchantability() {
        return 10;
    }

    @Override
    public SoundEvent getEquipSound() {
        // FIXME: Placeholder until specific sounds are recorded
        return SoundEvents.ITEM_ARMOR_EQUIP_GENERIC;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.ofItems(Initializer.RUBY);
    }

    @Override
    public String getName() {
        return "ruby";
    }

    @Override
    public float getToughness() {
        return 1.0f;
    }

    @Override
    public float getKnockbackResistance() {
        return 0.0f;
    }
}
