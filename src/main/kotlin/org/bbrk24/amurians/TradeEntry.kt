package org.bbrk24.amurians

import net.minecraft.util.math.random.Random
import net.minecraft.village.TradeOffer

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
