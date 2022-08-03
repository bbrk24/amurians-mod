package org.bbrk24.amurians.mixins.dyes.loom;

import net.minecraft.item.DyeItem;
import net.minecraft.screen.LoomScreenHandler;

import org.bbrk24.amurians.PowderDyeItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(LoomScreenHandler.class)
public abstract class LoomScreenHandlerMixin {
    @ModifyConstant(
        method = "transferSlot(Lnet/minecraft/entity/player/PlayerEntity;I)Lnet/minecraft/item/ItemStack;",
        constant = @Constant(classValue = DyeItem.class)
    )
    public boolean isDyeItem(Object obj, Class<DyeItem> c) {
        return obj instanceof DyeItem && !(obj instanceof PowderDyeItem);
    }
}
