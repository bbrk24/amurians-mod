package org.bbrk24.amurians

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.DyeItem
import net.minecraft.item.ItemStack
import net.minecraft.util.ActionResult
import net.minecraft.util.DyeColor
import net.minecraft.util.Hand

class PowderDyeItem(color: DyeColor, settings: Settings) : DyeItem(color, settings) {
    override fun useOnEntity(
        stack: ItemStack,
        user: PlayerEntity,
        entity: LivingEntity,
        hand: Hand
    ) = ActionResult.PASS
}
