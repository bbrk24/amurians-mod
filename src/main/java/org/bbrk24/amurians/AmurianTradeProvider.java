package org.bbrk24.amurians;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.TradeOffer;

import org.bbrk24.amurians.AmurianEntity.Profession;
import org.bbrk24.amurians.AmurianEntity.BiomeGroup;
import org.bbrk24.util.FunctionalList;

public abstract class AmurianTradeProvider {
    // I have no idea what the price multiplier is so I'm just making it 1 for now
    private static final ImmutableList<TradeEntry> BUYING_FUEL = ImmutableList.of(
        new TradeEntry(
            "buy_coal",
            (random, level) -> new TradeOffer(
                new ItemStack(Items.COAL, random.nextBetween(10, 25)),
                new ItemStack(Initializer.RUBY),
                16,
                level == 1 ? 1 : 5,
                1.0f
            )
        ),
        new TradeEntry(
            "buy_dried_kelp_block",
            (random, level) -> new TradeOffer(
                new ItemStack(Items.DRIED_KELP_BLOCK, random.nextBetween(4, 10)),
                new ItemStack(Initializer.RUBY),
                16,
                level == 1 ? 1 : 5,
                1.0f
            )
        ),
        new TradeEntry(
            "buy_coal_block",
            (random, level) -> new TradeOffer(
                new ItemStack(Items.COAL_BLOCK, random.nextBetween(2, 5)),
                new ItemStack(Initializer.RUBY, 2),
                12,
                level == 1 ? 2 : 10,
                1.0f
            )
        )
    );
    private static final ImmutableList<TradeEntry> BUYING_STICKS_AND_STRING = ImmutableList.of(
        new TradeEntry(
            "buy_stick",
            (random, level) -> new TradeOffer(
                new ItemStack(Items.STICK, 32),
                new ItemStack(Initializer.RUBY),
                16,
                1,
                1.0f
            )
        ),
        new TradeEntry(
            "buy_string",
            (random, level) -> new TradeOffer(
                new ItemStack(Items.STRING, random.nextBetween(14, 20)),
                new ItemStack(Initializer.RUBY),
                16,
                1,
                1.0f
            )
        )
    );
    private static final ImmutableList<TradeEntry> BUYING_WEAPON_MATERIALS = ImmutableList.of(
        new TradeEntry(
            "buy_flint",
            (random, level) -> new TradeOffer(
                new ItemStack(Items.FLINT, random.nextBetween(24, 30)),
                new ItemStack(Initializer.RUBY),
                16,
                2,
                1.0f
            )
        ),
        new TradeEntry(
            "buy_iron",
            (random, level) -> new TradeOffer(
                new ItemStack(Items.IRON_INGOT, 4),
                new ItemStack(Initializer.RUBY),
                16,
                2,
                1.0f
            )
        )
    );
    private static final ImmutableList<TradeEntry> SELLING_COMMON_FISH = ImmutableList.of(
        new TradeEntry(
            "sell_raw_cod",
            (random, level) -> new TradeOffer(
                new ItemStack(Initializer.RUBY),
                new ItemStack(Items.COD, 15),
                16,
                5,
                1.0f
            )
        ),
        new TradeEntry(
            "sell_cooked_cod",
            (random, level) -> new TradeOffer(
                new ItemStack(Initializer.RUBY),
                new ItemStack(Items.COOKED_COD, random.nextBetween(12, 14)),
                16,
                10,
                1.0f
            )
        ),
        new TradeEntry(
            "sell_live_cod",
            (random, level) -> new TradeOffer(
                new ItemStack(Initializer.RUBY, 3),
                new ItemStack(Items.COD_BUCKET),
                16,
                10,
                1.0f
            )
        ),
        new TradeEntry(
            "sell_raw_salmon",
            (random, level) -> new TradeOffer(
                new ItemStack(Initializer.RUBY),
                new ItemStack(Items.SALMON, 13),
                16,
                5,
                1.0f
            )
        ),
        new TradeEntry(
            "sell_cooked_salmon",
            (random, level) -> new TradeOffer(
                new ItemStack(Initializer.RUBY),
                new ItemStack(Items.COOKED_SALMON, random.nextBetween(10, 12)),
                16,
                10,
                1.0f
            )
        ),
        new TradeEntry(
            "sell_live_salmon",
            (random, level) -> new TradeOffer(
                new ItemStack(Initializer.RUBY, random.nextBetween(3, 5)),
                new ItemStack(Items.SALMON_BUCKET),
                16,
                10,
                1.0f
            )
        )
    );
    private static final ImmutableList<TradeEntry> SELLING_LOW_RANGED_WEAPONS = ImmutableList.of(
        new TradeEntry(
            "sell_arrow",
            (random, level) -> new TradeOffer(
                new ItemStack(Initializer.RUBY),
                new ItemStack(Items.ARROW, 16),
                12,
                5,
                1.0f
            )
        ),
        new TradeEntry(
            "sell_bow",
            (random, level) -> new TradeOffer(
                new ItemStack(Initializer.RUBY, 2),
                new ItemStack(Items.BOW),
                12,
                10,
                1.0f
            )
        ),
        new TradeEntry(
            "sell_crossbow",
            (random, level) -> new TradeOffer(
                new ItemStack(Initializer.RUBY, 3),
                new ItemStack(Items.CROSSBOW),
                12,
                10,
                1.0f
            )
        )
    );
    private static final ImmutableList<TradeEntry> SELLING_LOW_MELEE_WEAPONS = ImmutableList.of(
        new TradeEntry(
            "sell_iron_axe",
            (random, level) -> new TradeOffer(
                new ItemStack(Initializer.RUBY, 3),
                new ItemStack(Items.IRON_AXE),
                12,
                5,
                1.0f
            )
        ),
        new TradeEntry(
            "sell_iron_sword",
            (random, level) -> new TradeOffer(
                new ItemStack(Initializer.RUBY, 4),
                new ItemStack(Items.IRON_SWORD),
                12,
                10,
                1.0f
            )
        ),
        new TradeEntry(
            "sell_diamond_axe",
            (random, level) -> new TradeOffer(
                new ItemStack(Initializer.RUBY, 9),
                new ItemStack(Items.DIAMOND_AXE),
                12,
                5,
                1.0f
            )
        )
    );
    private static final ImmutableList<TradeEntry> BUYING_BOAT = ImmutableList.of(
        new TradeEntry(
            "buy_spruce_boat",
            biome -> biome == BiomeGroup.COLD,
            (random, level) -> new TradeOffer(
                new ItemStack(Items.SPRUCE_BOAT),
                new ItemStack(Initializer.RUBY),
                12,
                10,
                1.0f
            )
        ),
        new TradeEntry(
            "buy_oak_boat",
            biome -> biome == BiomeGroup.MODERATE,
            (random, level) -> new TradeOffer(
                new ItemStack(Items.OAK_BOAT),
                new ItemStack(Initializer.RUBY),
                12,
                10,
                1.0f
            )
        ),
        new TradeEntry(
            "buy_birch_boat",
            biome -> biome == BiomeGroup.MODERATE,
            (random, level) -> new TradeOffer(
                new ItemStack(Items.BIRCH_BOAT),
                new ItemStack(Initializer.RUBY),
                12,
                10,
                1.0f
            )
        ),
        new TradeEntry(
            "buy_jungle_boat",
            biome -> biome == BiomeGroup.JUNGLE,
            (random, level) -> new TradeOffer(
                new ItemStack(Items.JUNGLE_BOAT),
                new ItemStack(Initializer.RUBY),
                12,
                10,
                1.0f
            )
        ),
        new TradeEntry(
            "buy_acacia_boat",
            biome -> biome == BiomeGroup.SAVANNAH,
            (random, level) -> new TradeOffer(
                new ItemStack(Items.ACACIA_BOAT),
                new ItemStack(Initializer.RUBY),
                12,
                10,
                1.0f
            )
        )
    );
    private static final ImmutableList<TradeEntry> SELLING_RARE_FISH = ImmutableList.of(
        new TradeEntry(
            "sell_tropical_fish",
            (random, level) -> new TradeOffer(
                new ItemStack(Initializer.RUBY),
                new ItemStack(Items.TROPICAL_FISH, 6),
                16,
                20,
                1.0f
            )
        ),
        new TradeEntry(
            "sell_pufferfish",
            (random, level) -> new TradeOffer(
                new ItemStack(Initializer.RUBY),
                new ItemStack(Items.PUFFERFISH, 4),
                16,
                20,
                1.0f
            )
        ),
        new TradeEntry(
            "sell_live_tropical_fish",
            (random, level) -> new TradeOffer(
                new ItemStack(Initializer.RUBY, random.nextBetween(5, 7)),
                new ItemStack(Items.TROPICAL_FISH_BUCKET),
                12,
                20,
                1.0f
            )
        )
    );
    private static final ImmutableList<TradeEntry> SELLING_MEAT = ImmutableList.of(
        new TradeEntry(
            "sell_cooked_chicken",
            (random, level) -> new TradeOffer(
                new ItemStack(Initializer.RUBY),
                new ItemStack(Items.COOKED_CHICKEN, 8),
                16,
                20,
                1.0f
            )
        ),
        new TradeEntry(
            "sell_cooked_porkchop",
            (random, level) -> new TradeOffer(
                new ItemStack(Initializer.RUBY),
                new ItemStack(Items.COOKED_PORKCHOP, 5),
                16,
                20,
                1.0f
            )
        ),
        new TradeEntry(
            "sell_cooked_rabbit",
            biome -> biome == BiomeGroup.COLD,
            (random, level) -> new TradeOffer(
                new ItemStack(Initializer.RUBY),
                new ItemStack(Items.COOKED_RABBIT, 3),
                16,
                20,
                1.0f
            )
        ),
        new TradeEntry(
            "sell_rabbit_stew",
            biome -> biome == BiomeGroup.COLD,
            (random, level) -> new TradeOffer(
                new ItemStack(Initializer.RUBY),
                new ItemStack(Items.RABBIT_STEW),
                16,
                level == 3 ? 20 : 30,
                1.0f
            )
        ),
        new TradeEntry(
            "sell_raw_chicken",
            (random, level) -> new TradeOffer(
                new ItemStack(Initializer.RUBY),
                new ItemStack(Items.CHICKEN, 14),
                16,
                level == 3 ? 10 : 15,
                1.0f
            )
        ),
        new TradeEntry(
            "sell_raw_porkchop",
            (random, level) -> new TradeOffer(
                new ItemStack(Initializer.RUBY),
                new ItemStack(Items.PORKCHOP, 7),
                16,
                level == 3 ? 10 : 15,
                1.0f
            )
        ),
        new TradeEntry(
            "sell_raw_rabbit",
            biome -> biome == BiomeGroup.COLD,
            (random, level) -> new TradeOffer(
                new ItemStack(Initializer.RUBY),
                new ItemStack(Items.RABBIT, 4),
                16,
                level == 3 ? 10 : 15,
                1.0f
            )
        )
    );
    private static final ImmutableList<TradeEntry> SELLING_MID_MELEE_WEAPONS = ImmutableList.of(
        new TradeEntry("sell_enchanted_iron_axe", getEnchantedItemTrade(Items.IRON_AXE, 1, 20)),
        new TradeEntry("sell_enchanted_iron_sword", getEnchantedItemTrade(Items.IRON_SWORD, 2, 20)),
        new TradeEntry(
            "sell_diamond_sword",
            (random, level) -> new TradeOffer(
                new ItemStack(Initializer.RUBY, 7),
                new ItemStack(Items.DIAMOND_SWORD),
                12,
                10,
                1.0f
            )
        )
    );
    private static final ImmutableList<TradeEntry> SELLING_FISHING_LEATHER = ImmutableList.of(
        new TradeEntry(
            "sell_leather_boots",
            (random, level) -> new TradeOffer(
                new ItemStack(Initializer.RUBY, 4),
                new ItemStack(Items.LEATHER_BOOTS),
                12,
                15,
                1.0f
            )
        ),
        new TradeEntry(
            "sell_saddle",
            (random, level) -> new TradeOffer(
                new ItemStack(Initializer.RUBY, 6),
                new ItemStack(Items.SADDLE),
                12,
                30,
                1.0f
            )
        )
    );
    private static final ImmutableList<TradeEntry> SELLING_HIGH_RANGED_WEAPONS = ImmutableList.of(
        new TradeEntry("sell_tipped_arrow", getTippedArrowTrade(5, 10, 15)),
        new TradeEntry("sell_enchanted_bow", getEnchantedItemTrade(Items.BOW, 2, 30)),
        new TradeEntry("sell_enchanted_crossbow", getEnchantedItemTrade(Items.CROSSBOW, 3, 30))
    );
    private static final ImmutableList<TradeEntry> SELLING_HIGH_MELEE_WEAPONS = ImmutableList.of(
        new TradeEntry(
            "sell_enchanted_diamond_sword",
            getEnchantedItemTrade(Items.DIAMOND_SWORD, 8, 30)
        ),
        new TradeEntry(
            "sell_enchanted_diamond_axe",
            getEnchantedItemTrade(Items.DIAMOND_AXE, 12, 30)
        )
    );

    private static BiFunction<Random, Integer, TradeOffer> getEnchantedItemTrade(
        ItemConvertible item,
        int basePrice,
        int experience
    ) {
        // based on TradeOffers$SellEnchantedToolFactory#create()
        return (random, i) -> {
            int level = random.nextBetween(5, 19);
            ItemStack sellStack = EnchantmentHelper.enchant(
                random,
                new ItemStack(item),
                level,
                false
            );
            int cost = Math.min(64, level + basePrice);
            return new TradeOffer(
                new ItemStack(Initializer.RUBY, cost),
                sellStack,
                3,
                experience,
                1.0f
            );
        };
    }
    private static BiFunction<Random, Integer, TradeOffer> getTippedArrowTrade(
        int price,
        int count,
        int experience
    ) {
        ArrayList<ItemStack> arrows = Registry.POTION.stream()
            .filter(
                potion -> !potion.getEffects().isEmpty() && BrewingRecipeRegistry.isBrewable(potion)
            )
            .map(potion -> PotionUtil.setPotion(new ItemStack(Items.TIPPED_ARROW, count), potion))
            .collect(Collectors.toCollection(ArrayList::new));
        arrows.add(new ItemStack(Items.SPECTRAL_ARROW, count));

        return (random, level) -> {
            int index = random.nextInt(arrows.size());
            ItemStack sellItem = arrows.get(index);
            return new TradeOffer(
                new ItemStack(Initializer.RUBY, price),
                sellItem,
                12,
                experience,
                1.0f
            );
        };
    }

    /**
     * Get possible trade offers for the given profession, biome, and level.
     * @param profession The profession of the merchant
     * @param biome The biome the merchant belongs to
     * @param currentOffers The string ID's of the trades the merchant already offers
     * @param random The random number generator to use
     * @param level The merchant level to fetch offers for
     * @throws IllegalArgumentException if {@code level} is outside the range 1-5 (inclusive)
     */
    public static List<Pair<String, TradeOffer>> getOffersForLevel(
        Profession profession,
        BiomeGroup biome,
        Collection<String> currentOffers,
        Random random,
        int level
    ) {
        if (profession == Profession.UNEMPLOYED) {
            return ImmutableList.of();
        }
        switch (level) {
        case 1:
            if (profession == Profession.FISHERMAN) {
                return chooseOffers(
                    biome,
                    currentOffers,
                    random,
                    1,
                    2,
                    FunctionalList.concat(BUYING_FUEL, BUYING_STICKS_AND_STRING)
                );
            }
            if (profession == Profession.WEAPONSMITH) {
                return FunctionalList.concat(
                    chooseOffers(biome, currentOffers, random, 1, 1, BUYING_STICKS_AND_STRING),
                    chooseOffers(biome, currentOffers, random, 1, 1, BUYING_WEAPON_MATERIALS)
                );
            }
            break;
        case 2:
            if (profession == Profession.FARMER || profession == Profession.BUTCHER) {
                return chooseOffers(biome, currentOffers, random, 2, 1, BUYING_FUEL);
            }
            if (profession == Profession.FISHERMAN) {
                return chooseOffers(biome, currentOffers, random, 2, 2, SELLING_COMMON_FISH);
            }
            if (profession == Profession.WEAPONSMITH) {
                return chooseOffers(
                    biome,
                    currentOffers,
                    random,
                    2,
                    2,
                    FunctionalList.concat(SELLING_LOW_MELEE_WEAPONS, SELLING_LOW_RANGED_WEAPONS)
                );
            }
            // This should not be reached, no break necessary
        case 3:
            if (profession == Profession.FISHERMAN) {
                return FunctionalList.concat(
                    chooseOffers(biome, currentOffers, random, 3, 1, BUYING_BOAT),
                    chooseOffers(biome, currentOffers, random, 3, 1, SELLING_RARE_FISH)
                );
            }
            if (profession == Profession.BUTCHER) {
                return chooseOffers(biome, currentOffers, random, 3, 2, SELLING_MEAT);
            }
            if (profession == Profession.WEAPONSMITH) {
                return chooseOffers(
                    biome,
                    currentOffers,
                    random,
                    3,
                    2,
                    FunctionalList.concat(SELLING_LOW_RANGED_WEAPONS, SELLING_MID_MELEE_WEAPONS)
                );
            }
            break;
        case 4:
            if (profession == Profession.FISHERMAN) {
                return chooseOffers(biome, currentOffers, random, 4, 1, SELLING_FISHING_LEATHER);
            }
            if (profession == Profession.WEAPONSMITH) {
                return FunctionalList.concat(
                    chooseOffers(biome, currentOffers, random, 4, 1, SELLING_HIGH_MELEE_WEAPONS),
                    chooseOffers(biome, currentOffers, random, 4, 1, SELLING_HIGH_RANGED_WEAPONS)
                );
            }
            break;
        case 5:
            break;
        default:
            throw new IllegalArgumentException("Merchant level must be between 1 and 5 (inclusive)");
        }
        // TODO: defaults to empty list
        return ImmutableList.of();
    }

    // I recognize this might not be the most efficient possible way to implement this, but it works
    private static List<Pair<String, TradeOffer>> chooseOffers(
        BiomeGroup biome,
        Collection<String> currentOffers,
        Random random,
        int level,
        int amount,
        List<TradeEntry> entries
    ) {
        final FunctionalList<TradeEntry> validEntries = FunctionalList.filtering(
            entries,
            el -> el.requirements.test(biome)
        );

        if (amount > validEntries.size()) {
            // what do
        }

        FunctionalList<Pair<String, TradeOffer>> offers = new FunctionalList<>(amount);
        Predicate<String> alreadyPresent = id ->
            currentOffers.contains(id) || offers.some(p -> p.getFirst() == id);

        for (int i = 0; i < amount; ++i) {
            TradeEntry chosenEntry;

            do {
                int index = random.nextInt(validEntries.size());
                chosenEntry = validEntries.get(index);
            } while (alreadyPresent.test(chosenEntry.id));

            offers.add(Pair.of(chosenEntry.id, chosenEntry.tradeGenerator.apply(random, level)));
        }

        return offers;
    }

    static class TradeEntry {
        public final String id;
        public final Predicate<BiomeGroup> requirements;
        public final BiFunction<Random, Integer, TradeOffer> tradeGenerator;

        public TradeEntry(
            String id,
            Predicate<BiomeGroup> requirements,
            BiFunction<Random, Integer, TradeOffer> tradeGenerator
        ) {
            this.id = id;
            this.requirements = requirements;
            this.tradeGenerator = tradeGenerator;
        }

        public TradeEntry(String id, BiFunction<Random, Integer, TradeOffer> tradeGenerator) {
            this(id, biome -> true, tradeGenerator);
        }
    }
}
