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

import kotlin.math.min

import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.EnchantmentLevelEntry
import net.minecraft.item.EnchantedBookItem
import net.minecraft.item.ItemConvertible
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.potion.PotionUtil
import net.minecraft.recipe.BrewingRecipeRegistry
import net.minecraft.util.math.random.Random
import net.minecraft.util.registry.Registry
import net.minecraft.village.TradeOffer

import org.bbrk24.amurians.Initializer
import org.bbrk24.amurians.amurian.AmurianEntity

private const val PRICE_MULTIPLIER = 0.2f

private fun lowXPForLevel(level: Int) = when (level) {
    1 -> 1
    2 -> 5
    3 -> 10
    4 -> 15
    else -> 0
}
private fun highXPForLevel(level: Int) = when (level) {
    1 -> 2
    2 -> 10
    3 -> 20
    4 -> 30
    else -> 0
}

private object TradeItemHelper {
    fun makeEnchantedItemTrade(
        item: ItemConvertible,
        basePrice: Int,
        calcXP: (Int) -> Int
    ): (Random, Int) -> TradeOffer = { random, merchantLevel ->
        val enchLevel = random.nextBetween(5, 19)
        val sellStack = EnchantmentHelper.enchant(random, ItemStack(item), enchLevel, false)
        val cost = min(64, enchLevel + basePrice)

        TradeOffer(
            ItemStack(Initializer.RUBY, cost),
            sellStack,
            3,
            calcXP(merchantLevel),
            PRICE_MULTIPLIER
        )
    }

    inline fun makeTippedArrowTrade(
        count: Int,
        price: Int,
        crossinline calcXP: (Int) -> Int
    ): (Random, Int) -> TradeOffer {
        val arrows = Registry.POTION.stream()
            .filter { !it.getEffects().isEmpty() && BrewingRecipeRegistry.isBrewable(it) }
            .map { PotionUtil.setPotion(ItemStack(Items.TIPPED_ARROW, count), it) }
            .toList() + ItemStack(Items.SPECTRAL_ARROW, count)

        return { random, level ->
            val index: Int = random.nextInt(arrows.size)

            TradeOffer(
                ItemStack(Initializer.RUBY, price),
                arrows[index],
                12,
                calcXP(level),
                PRICE_MULTIPLIER
            )
        }
    }

    private val allEnchants = Registry.ENCHANTMENT.stream()
        .filter { it.isAvailableForEnchantedBookOffer() }
        .toList()

    fun makeEnchantedBookTrade(random: Random, merchantLevel: Int): TradeOffer {
        val enchantment = allEnchants[random.nextInt(allEnchants.size)]
        val maxLevel = enchantment.getMaxLevel()
        val level = if (maxLevel == 1) 1 else random.nextBetween((maxLevel + 1) / 2, maxLevel)
        val bookStack = EnchantedBookItem.forEnchantment(
            EnchantmentLevelEntry(enchantment, level)
        )

        var cost = 2 + (3 * level) + random.nextInt(5 + (level * 10))
        if (enchantment.isTreasure()) {
            cost *= 2
        }

        return TradeOffer(
            ItemStack(Initializer.RUBY, min(cost, 64)),
            bookStack,
            12,
            highXPForLevel(merchantLevel),
            PRICE_MULTIPLIER
        )
    }
}

internal object TradeEntries {
    val BUYING_FUEL = listOf(
        TradeEntry("buy_coal") { random, level ->
            TradeOffer(
                ItemStack(Items.COAL, random.nextBetween(10, 25)),
                ItemStack(Initializer.RUBY),
                16,
                lowXPForLevel(level),
                PRICE_MULTIPLIER
            )
        },
        TradeEntry("buy_dried_kelp_block") { random, level ->
            TradeOffer(
                ItemStack(Items.DRIED_KELP_BLOCK, random.nextBetween(4, 10)),
                ItemStack(Initializer.RUBY),
                16,
                lowXPForLevel(level),
                PRICE_MULTIPLIER
            )
        },
        TradeEntry("buy_coal_block") { random, level ->
            TradeOffer(
                ItemStack(Items.COAL_BLOCK, random.nextBetween(2, 5)),
                ItemStack(Initializer.RUBY, 2),
                12,
                highXPForLevel(level),
                PRICE_MULTIPLIER
            )
        }
    )
    val BUYING_STICKS_AND_STRING = listOf(
        TradeEntry("buy_stick") { _, level ->
            TradeOffer(
                ItemStack(Items.STICK, 32),
                ItemStack(Initializer.RUBY),
                16,
                lowXPForLevel(level),
                PRICE_MULTIPLIER
            )
        },
        TradeEntry("buy_string") { random, level ->
            TradeOffer(
                ItemStack(Items.STRING, random.nextBetween(14, 20)),
                ItemStack(Initializer.RUBY),
                16,
                lowXPForLevel(level),
                PRICE_MULTIPLIER
            )
        }
    )
    val BUYING_WEAPON_MATERIALS = listOf(
        TradeEntry("buy_flint") { random, level ->
            TradeOffer(
                ItemStack(Items.FLINT, random.nextBetween(24, 30)),
                ItemStack(Initializer.RUBY),
                16,
                highXPForLevel(level),
                PRICE_MULTIPLIER
            )
        },
        TradeEntry("buy_iron") { _, level ->
            TradeOffer(
                ItemStack(Items.IRON_INGOT, 4),
                ItemStack(Initializer.RUBY),
                16,
                highXPForLevel(level),
                PRICE_MULTIPLIER
            )
        }
    )
    val SELLING_COMMON_FISH = listOf(
        TradeEntry("sell_raw_cod") { _, level ->
            TradeOffer(
                ItemStack(Initializer.RUBY),
                ItemStack(Items.COD, 15),
                16,
                lowXPForLevel(level),
                PRICE_MULTIPLIER
            )
        },
        TradeEntry("sell_cooked_cod") { random, level ->
            TradeOffer(
                ItemStack(Initializer.RUBY),
                ItemStack(Items.COOKED_COD, random.nextBetween(12, 14)),
                16,
                highXPForLevel(level),
                PRICE_MULTIPLIER
            )
        },
        TradeEntry("sell_live_cod") { _, level ->
            TradeOffer(
                ItemStack(Initializer.RUBY, 3),
                ItemStack(Items.COD_BUCKET),
                16,
                highXPForLevel(level),
                PRICE_MULTIPLIER
            )
        },
        TradeEntry("sell_raw_salmon") { _, level ->
            TradeOffer(
                ItemStack(Initializer.RUBY),
                ItemStack(Items.SALMON, 13),
                16,
                lowXPForLevel(level),
                PRICE_MULTIPLIER
            )
        },
        TradeEntry("sell_cooked_salmon") { random, level ->
            TradeOffer(
                ItemStack(Initializer.RUBY),
                ItemStack(Items.COOKED_SALMON, random.nextBetween(10, 12)),
                16,
                highXPForLevel(level),
                PRICE_MULTIPLIER
            )
        },
        TradeEntry("sell_live_salmon") { random, level ->
            TradeOffer(
                ItemStack(Initializer.RUBY, random.nextBetween(3, 5)),
                ItemStack(Items.SALMON_BUCKET),
                16,
                highXPForLevel(level),
                PRICE_MULTIPLIER
            )
        }
    )
    val SELLING_LOW_RANGED_WEAPONS = listOf(
        TradeEntry("sell_arrow") { _, level ->
            TradeOffer(
                ItemStack(Initializer.RUBY),
                ItemStack(Items.ARROW, 16),
                12,
                lowXPForLevel(level),
                PRICE_MULTIPLIER
            )
        },
        TradeEntry("sell_bow") { _, level ->
            TradeOffer(
                ItemStack(Initializer.RUBY, 2),
                ItemStack(Items.BOW),
                12,
                highXPForLevel(level),
                PRICE_MULTIPLIER
            )
        },
        TradeEntry("sell_crossbow") { _, level ->
            TradeOffer(
                ItemStack(Initializer.RUBY, 3),
                ItemStack(Items.CROSSBOW),
                12,
                highXPForLevel(level),
                PRICE_MULTIPLIER
            )
        }
    )
    val BUYING_SEEDS = listOf(
        TradeEntry("buy_wheat_seeds") { _, level ->
            TradeOffer(
                ItemStack(Items.WHEAT_SEEDS, 25),
                ItemStack(Initializer.RUBY),
                16,
                lowXPForLevel(level),
                PRICE_MULTIPLIER
            )
        },
        TradeEntry("buy_beetroot_seeds") { _, level ->
            TradeOffer(
                ItemStack(Items.BEETROOT_SEEDS, 20),
                ItemStack(Initializer.RUBY),
                16,
                lowXPForLevel(level),
                PRICE_MULTIPLIER
            )
        },
        TradeEntry("buy_carrots") { _, level ->
            TradeOffer(
                ItemStack(Items.CARROT, 22),
                ItemStack(Initializer.RUBY),
                16,
                highXPForLevel(level),
                PRICE_MULTIPLIER
            )
        },
    )
    val SELLING_LOW_MELEE_WEAPONS = listOf(
        TradeEntry("sell_iron_axe") { _, level ->
            TradeOffer(
                ItemStack(Initializer.RUBY, 3),
                ItemStack(Items.IRON_AXE),
                12,
                lowXPForLevel(level),
                PRICE_MULTIPLIER
            )
        },
        TradeEntry("sell_iron_sword") { _, level ->
            TradeOffer(
                ItemStack(Initializer.RUBY, 4),
                ItemStack(Items.IRON_SWORD),
                12,
                highXPForLevel(level),
                PRICE_MULTIPLIER
            )
        },
        TradeEntry("sell_diamond_axe") { _, level ->
            TradeOffer(
                ItemStack(Initializer.RUBY, 9),
                ItemStack(Items.DIAMOND_AXE),
                12,
                lowXPForLevel(level),
                PRICE_MULTIPLIER
            )
        }
    )
    val BUYING_BOAT = listOf(
        TradeEntry(
            "buy_spruce_boat",
            { it == AmurianEntity.BiomeGroup.COLD },
            { _, level ->
                TradeOffer(
                    ItemStack(Items.SPRUCE_BOAT),
                    ItemStack(Initializer.RUBY),
                    12,
                    lowXPForLevel(level),
                    PRICE_MULTIPLIER
                )
            }
        ),
        TradeEntry("buy_oak_boat") { _, level ->
            TradeOffer(
                ItemStack(Items.OAK_BOAT),
                ItemStack(Initializer.RUBY),
                12,
                lowXPForLevel(level),
                PRICE_MULTIPLIER
            )
        },
        TradeEntry(
            "buy_birch_boat",
            { it == AmurianEntity.BiomeGroup.MODERATE },
            { _, level ->
                TradeOffer(
                    ItemStack(Items.BIRCH_BOAT),
                    ItemStack(Initializer.RUBY),
                    12,
                    lowXPForLevel(level),
                    PRICE_MULTIPLIER
                )
            }
        ),
        TradeEntry(
            "buy_jungle_boat",
            { it == AmurianEntity.BiomeGroup.JUNGLE },
            { _, level ->
                TradeOffer(
                    ItemStack(Items.JUNGLE_BOAT),
                    ItemStack(Initializer.RUBY),
                    12,
                    lowXPForLevel(level),
                    PRICE_MULTIPLIER
                )
            }
        ),
        TradeEntry(
            "buy_acacia_boat",
            { it == AmurianEntity.BiomeGroup.SAVANNA },
            { _, level ->
                TradeOffer(
                    ItemStack(Items.ACACIA_BOAT),
                    ItemStack(Initializer.RUBY),
                    12,
                    lowXPForLevel(level),
                    PRICE_MULTIPLIER
                )
            }
        )
    )
    val SELLING_RARE_FISH = listOf(
        TradeEntry("sell_tropical_fish") { _, level ->
            TradeOffer(
                ItemStack(Initializer.RUBY),
                ItemStack(Items.TROPICAL_FISH, 6),
                16,
                highXPForLevel(level),
                PRICE_MULTIPLIER
            )
        },
        TradeEntry("sell_pufferfish") { _, level ->
            TradeOffer(
                ItemStack(Initializer.RUBY),
                ItemStack(Items.PUFFERFISH, 4),
                16,
                highXPForLevel(level),
                PRICE_MULTIPLIER
            )
        },
        TradeEntry("sell_live_tropical_fish") { random, level ->
            TradeOffer(
                ItemStack(Initializer.RUBY, random.nextBetween(5, 7)),
                ItemStack(Items.TROPICAL_FISH_BUCKET),
                12,
                highXPForLevel(level),
                PRICE_MULTIPLIER
            )
        }
    )
    val SELLING_MEAT = listOf(
        TradeEntry("sell_cooked_chicken") { _, _ ->
            TradeOffer(
                ItemStack(Initializer.RUBY),
                ItemStack(Items.COOKED_CHICKEN, 8),
                16,
                20,
                PRICE_MULTIPLIER
            )
        },
        TradeEntry("sell_cooked_porkchop") { _, _ ->
            TradeOffer(
                ItemStack(Initializer.RUBY),
                ItemStack(Items.COOKED_PORKCHOP, 5),
                16,
                20,
                PRICE_MULTIPLIER
            )
        },
        TradeEntry("sell_raw_chicken") { _, level ->
            TradeOffer(
                ItemStack(Initializer.RUBY),
                ItemStack(Items.CHICKEN, 14),
                16,
                lowXPForLevel(level),
                PRICE_MULTIPLIER
            )
        },
        TradeEntry("sell_raw_porkchop") { _, level ->
            TradeOffer(
                ItemStack(Initializer.RUBY),
                ItemStack(Items.PORKCHOP, 7),
                16,
                lowXPForLevel(level),
                PRICE_MULTIPLIER
            )
        },
        TradeEntry(
            "sell_cooked_rabbit",
            { it == AmurianEntity.BiomeGroup.COLD },
            { _, _ ->
                TradeOffer(
                    ItemStack(Initializer.RUBY),
                    ItemStack(Items.COOKED_RABBIT, 3),
                    16,
                    20,
                    PRICE_MULTIPLIER
                )
            }
        ),
        TradeEntry(
            "sell_raw_rabbit",
            { it == AmurianEntity.BiomeGroup.COLD },
            { _, level ->
                TradeOffer(
                    ItemStack(Initializer.RUBY),
                    ItemStack(Items.RABBIT, 4),
                    16,
                    lowXPForLevel(level),
                    PRICE_MULTIPLIER
                )
            }
        ),
        TradeEntry(
            "sell_rabbit_stew",
            { it == AmurianEntity.BiomeGroup.COLD },
            { _, level ->
                TradeOffer(
                    ItemStack(Initializer.RUBY),
                    ItemStack(Items.RABBIT_STEW),
                    12,
                    highXPForLevel(level),
                    PRICE_MULTIPLIER
                )
            }
        )
    )
    val SELLING_MID_MELEE_WEAPONS = listOf(
        TradeEntry(
            "sell_enchanted_iron_axe",
            TradeItemHelper.makeEnchantedItemTrade(Items.IRON_AXE, 1, ::highXPForLevel)
        ),
        TradeEntry(
            "sell_enchanted_iron_sword",
            TradeItemHelper.makeEnchantedItemTrade(Items.IRON_SWORD, 2, ::highXPForLevel)
        ),
        TradeEntry("sell_diamond_sword") { _, level ->
            TradeOffer(
                ItemStack(Initializer.RUBY, 7),
                ItemStack(Items.DIAMOND_SWORD),
                12,
                lowXPForLevel(level),
                PRICE_MULTIPLIER
            )
        }
    )
    val SELLING_FISHING_LEATHER = listOf(
        TradeEntry("sell_leather_boots") { _, level ->
            TradeOffer(
                ItemStack(Initializer.RUBY, 4),
                ItemStack(Items.LEATHER_BOOTS),
                12,
                lowXPForLevel(level),
                PRICE_MULTIPLIER
            )
        },
        TradeEntry("sell_saddle") { _, level ->
            TradeOffer(
                ItemStack(Initializer.RUBY, 6),
                ItemStack(Items.SADDLE),
                12,
                highXPForLevel(level),
                PRICE_MULTIPLIER
            )
        }
    )
    val SELLING_HIGH_RANGED_WEAPONS = listOf(
        TradeEntry(
            "sell_tipped_arrow",
            TradeItemHelper.makeTippedArrowTrade(10, 5, ::lowXPForLevel)
        ),
        TradeEntry(
            "sell_enchanted_bow",
            TradeItemHelper.makeEnchantedItemTrade(Items.BOW, 2, ::highXPForLevel)
        ),
        TradeEntry(
            "sell_enchanted_crossbow",
            TradeItemHelper.makeEnchantedItemTrade(Items.CROSSBOW, 3, ::highXPForLevel)
        )
    )
    val SELLING_HIGH_MELEE_WEAPONS = listOf(
        TradeEntry(
            "sell_enchanted_diamond_axe",
            TradeItemHelper.makeEnchantedItemTrade(Items.DIAMOND_AXE, 12, ::highXPForLevel)
        ),
        TradeEntry(
            "sell_enchanted_diamond_sword",
            TradeItemHelper.makeEnchantedItemTrade(Items.DIAMOND_SWORD, 8, ::highXPForLevel)
        )
    )
    val SELLING_CROPS = listOf(
        TradeEntry("sell_beetroot") { _, level ->
            TradeOffer(
                ItemStack(Initializer.RUBY),
                ItemStack(Items.BEETROOT, 15),
                16,
                lowXPForLevel(level),
                PRICE_MULTIPLIER
            )
        },
        TradeEntry("sell_baked_potato") { random, level ->
            TradeOffer(
                ItemStack(Initializer.RUBY),
                ItemStack(Items.BAKED_POTATO, random.nextBetween(21, 25)),
                16,
                highXPForLevel(level),
                PRICE_MULTIPLIER
            )
        },
        TradeEntry("sell_wheat") { _, level ->
            TradeOffer(
                ItemStack(Initializer.RUBY),
                ItemStack(Items.WHEAT, 20),
                16,
                lowXPForLevel(level),
                PRICE_MULTIPLIER
            )
        },
        TradeEntry(
            "sell_sweet_berries",
            { it == AmurianEntity.BiomeGroup.COLD },
            { _, level ->
                TradeOffer(
                    ItemStack(Initializer.RUBY),
                    ItemStack(Items.SWEET_BERRIES, 10),
                    16,
                    lowXPForLevel(level),
                    PRICE_MULTIPLIER
                )
            }
        ),
        TradeEntry(
            "sell_glow_berries",
            { it == AmurianEntity.BiomeGroup.JUNGLE },
            { _, level ->
                TradeOffer(
                    ItemStack(Initializer.RUBY),
                    ItemStack(Items.GLOW_BERRIES, 10),
                    12,
                    lowXPForLevel(level),
                    PRICE_MULTIPLIER
                )
            }
        )
    )
    val BUYING_GOLD = listOf(
        TradeEntry("buy_gold") { _, level ->
            TradeOffer(
                ItemStack(Items.GOLD_INGOT, 3),
                ItemStack(Initializer.RUBY),
                16,
                lowXPForLevel(level),
                PRICE_MULTIPLIER
            )
        }
    )
    val SELLING_ANIMAL_DROPS = listOf(
        TradeEntry("sell_feather") { _, level ->
            TradeOffer(
                ItemStack(Initializer.RUBY),
                ItemStack(Items.FEATHER, 24),
                16,
                lowXPForLevel(level),
                PRICE_MULTIPLIER
            )
        },
        TradeEntry(
            "sell_rabbit_hide",
            { it == AmurianEntity.BiomeGroup.COLD },
            { _, level ->
                TradeOffer(
                    ItemStack(Initializer.RUBY),
                    ItemStack(Items.RABBIT_HIDE, 9),
                    16,
                    highXPForLevel(level),
                    PRICE_MULTIPLIER
                )
            }
        ),
        TradeEntry(
            "sell_rabbit_foot",
            { it == AmurianEntity.BiomeGroup.COLD },
            { _, level ->
                TradeOffer(
                    ItemStack(Initializer.RUBY),
                    ItemStack(Items.RABBIT_FOOT, 2),
                    12,
                    highXPForLevel(level),
                    PRICE_MULTIPLIER
                )
            }
        )
    )
    val SELLING_FISHING_TREASURE = listOf(
        TradeEntry("sell_enchanted_book", TradeItemHelper::makeEnchantedBookTrade),
        TradeEntry(
            "sell_enchanted_fishing_rod",
            TradeItemHelper.makeEnchantedItemTrade(Items.FISHING_ROD, 3, ::highXPForLevel)
        ),
        TradeEntry("sell_nautilus_shell") { _, _ ->
            TradeOffer(
                ItemStack(Initializer.RUBY, 6),
                ItemStack(Items.NAUTILUS_SHELL),
                12,
                0,
                PRICE_MULTIPLIER
            )
        }
    )
    val SELLING_MAX_WEAPONS = listOf(
        TradeEntry("sell_tnt") { random, _ ->
            TradeOffer(
                ItemStack(Initializer.RUBY, random.nextBetween(6, 8)),
                ItemStack(Items.TNT),
                12,
                0,
                PRICE_MULTIPLIER
            )
        },
        TradeEntry("sell_trident") { _, _ ->
            TradeOffer(
                ItemStack(Initializer.RUBY, 9),
                ItemStack(Items.TRIDENT),
                4,
                0,
                PRICE_MULTIPLIER
            )
        }
    )
    val SELLING_GILDED_FOOD = listOf(
        TradeEntry("sell_golden_carrot") { _, _ ->
            TradeOffer(
                ItemStack(Initializer.RUBY),
                ItemStack(Items.GOLDEN_CARROT),
                16,
                0,
                PRICE_MULTIPLIER
            )
        },
        TradeEntry(
            "sell_glistering_melon_slice",
            { it == AmurianEntity.BiomeGroup.JUNGLE || it == AmurianEntity.BiomeGroup.SAVANNA },
            { _, _ ->
                TradeOffer(
                    ItemStack(Initializer.RUBY),
                    ItemStack(Items.GLISTERING_MELON_SLICE, 2),
                    16,
                    0,
                    PRICE_MULTIPLIER
                )
            }
        ),
        TradeEntry("sell_golden_apple") { _, _ ->
            TradeOffer(
                ItemStack(Initializer.RUBY),
                ItemStack(Items.GOLDEN_APPLE),
                12,
                0,
                PRICE_MULTIPLIER
            )
        }
    )
}
