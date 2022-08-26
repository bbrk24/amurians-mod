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

package org.bbrk24.amurians.amurian.trade

import net.minecraft.util.math.random.Random
import net.minecraft.village.TradeOffer

import org.bbrk24.amurians.amurian.AmurianEntity

internal data class TradeEntry(
    val id: String,
    val requirements: (AmurianEntity.BiomeGroup) -> Boolean,
    val tradeGenerator: (Random, Int) -> TradeOffer
) {
    constructor(id: String, tradeGenerator: (Random, Int) -> TradeOffer) : this(
        id,
        { _ -> true },
        tradeGenerator
    ) { }
}
