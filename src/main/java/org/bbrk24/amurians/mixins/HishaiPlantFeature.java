package org.bbrk24.amurians.mixins;

import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.OverworldBiomeCreator;
import net.minecraft.world.gen.GenerationStep;

import org.bbrk24.amurians.hishai.HishaiPlacedFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(OverworldBiomeCreator.class)
public abstract class HishaiPlantFeature {
    @ModifyVariable(
        method = "createJungleFeatures(FZZZLnet/minecraft/world/biome/SpawnSettings$Builder;)Lnet/minecraft/world/biome/Biome;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/gen/feature/DefaultBiomeFeatures;addSparseJungleTrees(Lnet/minecraft/world/biome/GenerationSettings$Builder;)V",
            ordinal = 0
        )
    )
    private static GenerationSettings.Builder createHishaiFeature(
        GenerationSettings.Builder builder
    ) {
        builder.feature(
            GenerationStep.Feature.VEGETAL_DECORATION,
            HishaiPlacedFeature.getHishaiPlacedFeature()
        );
        return builder;
    }
}
