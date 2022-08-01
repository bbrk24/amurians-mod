package org.bbrk24.amurians.amurian.trade

import com.mojang.datafixers.util.Pair

import net.minecraft.util.math.random.Random
import net.minecraft.village.TradeOffer

import org.bbrk24.amurians.Initializer
// I reference Profession very often in this file, and I don't reference AmurianEntity directly at
// all, which in my mind justifies importing the inner classes instead.
import org.bbrk24.amurians.amurian.AmurianEntity.BiomeGroup
import org.bbrk24.amurians.amurian.AmurianEntity.Profession

object AmurianTradeProvider {
    private fun chooseOffers(
        random: Random,
        biome: BiomeGroup,
        currentOffers: Collection<String>,
        level: Int,
        amount: Int,
        entries: List<TradeEntry>
    ): List<Pair<String, TradeOffer>> {
        // filter out the ones that require a different biome or were already chosen
        val validEntries = entries.filter {
            it.requirements(biome) && !currentOffers.contains(it.id)
        }.toMutableList()
        // if there's still too many, pick some randomly
        var chosenEntries: MutableList<TradeEntry>
        if (validEntries.size <= amount) {
            chosenEntries = validEntries
        } else if (validEntries.size >= amount * 2) {
            chosenEntries = ArrayList(amount)
            while (chosenEntries.size < amount) {
                val index = random.nextInt(validEntries.size)
                chosenEntries.add(validEntries.removeAt(index))
            }
        } else {
            chosenEntries = validEntries
            while (chosenEntries.size > amount) {
                val index = random.nextInt(chosenEntries.size)
                chosenEntries.removeAt(index)
            }
        }
        // map it to the expected type
        return chosenEntries.map {
            Pair.of(it.id, it.tradeGenerator(random, level))
        }
    }

    fun getOffersForMerchant(
        profession: Profession,
        level: Int,
        biome: BiomeGroup,
        offers: Collection<String>,
        random: Random
    ): List<Pair<String, TradeOffer>> {
        if (profession == Profession.UNEMPLOYED) {
            return emptyList()
        }
        when (level) {
            1 -> return when (profession) {
                Profession.FISHERMAN -> chooseOffers(
                        random,
                        biome,
                        offers,
                        1,
                        2,
                        TradeEntries.BUYING_FUEL + TradeEntries.BUYING_STICKS_AND_STRING
                    )
                Profession.WEAPONSMITH -> chooseOffers(
                        random,
                        biome,
                        offers,
                        1,
                        1,
                        TradeEntries.BUYING_STICKS_AND_STRING
                    ) + chooseOffers(
                        random,
                        biome,
                        offers,
                        1,
                        1,
                        TradeEntries.BUYING_WEAPON_MATERIALS
                    )
                // butcher or farmer
                else -> chooseOffers(
                        random,
                        biome,
                        offers,
                        1,
                        2,
                        TradeEntries.BUYING_SEEDS
                    )
            }
            2 -> return when (profession) {
                Profession.FISHERMAN -> chooseOffers(
                        random,
                        biome,
                        offers,
                        2,
                        2,
                        TradeEntries.SELLING_COMMON_FISH
                    )
                Profession.WEAPONSMITH -> chooseOffers(
                        random,
                        biome,
                        offers,
                        2,
                        2,
                        TradeEntries.SELLING_LOW_MELEE_WEAPONS +
                            TradeEntries.SELLING_LOW_RANGED_WEAPONS
                    )
                // butcher or farmer
                else -> chooseOffers(
                        random,
                        biome,
                        offers,
                        2,
                        1,
                        TradeEntries.BUYING_FUEL
                    )
            }
            3 -> when (profession) {
                Profession.FISHERMAN -> return chooseOffers(
                        random,
                        biome,
                        offers,
                        3,
                        1,
                        TradeEntries.SELLING_RARE_FISH
                    ) + chooseOffers(
                        random,
                        biome,
                        offers,
                        3,
                        1,
                        TradeEntries.BUYING_BOAT
                    )
                Profession.BUTCHER -> return chooseOffers(
                        random,
                        biome,
                        offers,
                        3,
                        2,
                        TradeEntries.SELLING_MEAT
                    )
                Profession.WEAPONSMITH -> return chooseOffers(
                        random,
                        biome,
                        offers,
                        3,
                        2,
                        TradeEntries.SELLING_MID_MELEE_WEAPONS +
                            TradeEntries.SELLING_LOW_RANGED_WEAPONS
                    )
                // farmer
                else -> Unit
            }
            4 -> when (profession) {
                Profession.FISHERMAN -> return chooseOffers(
                        random,
                        biome,
                        offers,
                        4,
                        1,
                        TradeEntries.SELLING_FISHING_LEATHER
                    )
                Profession.WEAPONSMITH -> return chooseOffers(
                        random,
                        biome,
                        offers,
                        4,
                        1,
                        TradeEntries.SELLING_HIGH_MELEE_WEAPONS
                    ) + chooseOffers(
                        random,
                        biome,
                        offers,
                        4,
                        1,
                        TradeEntries.SELLING_HIGH_RANGED_WEAPONS
                    )
                // farmer or butcher
                else -> Unit
            }
            5 -> Unit
            else -> throw IllegalArgumentException("Merchant level must be between 1 and 5 (inclusive)")
        }
        Initializer.LOGGER.warn("getOffersForMerchant() fell through, returning empty list")
        return emptyList()
    }
}
