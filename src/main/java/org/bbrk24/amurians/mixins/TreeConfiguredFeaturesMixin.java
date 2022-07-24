package org.bbrk24.amurians.mixins;

import net.minecraft.block.Block;
import net.minecraft.world.gen.feature.TreeConfiguredFeatures;

import org.bbrk24.amurians.Initializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(TreeConfiguredFeatures.class)
public class TreeConfiguredFeaturesMixin {
    @Redirect(
        method = "<clinit>",
        at = @At(
            target = "Lnet/minecraft/block/Blocks;OAK_LOG:Lnet/minecraft/block/Block;",
            value = "FIELD",
            ordinal = 0
        ),
        slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=azalea_tree"))
    )
    private static Block getAzaleaTreeOakLog() {
        return Initializer.AZALEA_LOG;
    }
}
