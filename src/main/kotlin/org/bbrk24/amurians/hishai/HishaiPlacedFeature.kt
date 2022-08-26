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
