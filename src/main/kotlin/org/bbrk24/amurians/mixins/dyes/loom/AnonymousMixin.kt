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
