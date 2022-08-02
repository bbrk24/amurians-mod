package org.bbrk24.amurians.amurian.trade

import kotlin.math.min

import net.minecraft.enchantment.EnchantmentHelper
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

private const val PRICE_MULTIPLIER = 1.0f

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

    fun makeTippedArrowTrade(
        count: Int,
        price: Int,
        calcXP: (Int) -> Int
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
        TradeEntry(
            "buy_oak_boat",
            { it == AmurianEntity.BiomeGroup.MODERATE },
            { _, level ->
                TradeOffer(
                    ItemStack(Items.OAK_BOAT),
                    ItemStack(Initializer.RUBY),
                    12,
                    lowXPForLevel(level),
                    PRICE_MULTIPLIER
                )
            }
        ),
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
}
