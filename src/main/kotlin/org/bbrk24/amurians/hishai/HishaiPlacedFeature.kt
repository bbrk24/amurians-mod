package org.bbrk24.amurians.hishai

import net.minecraft.world.gen.feature.ConfiguredFeatures
import net.minecraft.world.gen.feature.Feature
import net.minecraft.world.gen.feature.PlacedFeatures
import net.minecraft.world.gen.feature.RandomPatchFeatureConfig
import net.minecraft.world.gen.feature.SimpleBlockFeatureConfig
import net.minecraft.world.gen.feature.VegetationPlacedFeatures
import net.minecraft.world.gen.stateprovider.BlockStateProvider

import org.bbrk24.amurians.Initializer

const val HISHAI_FEATURE_ID = "hishai"

private val HISHAI_CONFIGURED_FEATURE = ConfiguredFeatures.register(
    "hishai",
    Feature.RANDOM_PATCH,
    RandomPatchFeatureConfig(
        16,
        7,
        3,
        PlacedFeatures.createEntry(
            Feature.SIMPLE_BLOCK,
            SimpleBlockFeatureConfig(
                BlockStateProvider.of(
                    Initializer.HISHAI_PLANT.getDefaultState()
                        .with(AGE, LARGE_CUTOFF_AGE - 1)
                )
            )
        )
    )
)

private val HISHAI_PLACED_FEATURE = PlacedFeatures.register(
    HISHAI_FEATURE_ID,
    HISHAI_CONFIGURED_FEATURE,
    VegetationPlacedFeatures.modifiers(2)
)

object HishaiPlacedFeature {
    @JvmStatic
    fun getHishaiPlacedFeature() = HISHAI_PLACED_FEATURE
}
