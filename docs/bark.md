# Bark Shavings

Bark shavings are items that have a chance to drop when stripping overworld wood with an axe.
Stripping wood at an emery table will not drop bark shavings.

## Drop Chances

Enchantments affect how likely bark shavings are to drop, and how much can drop:

| Enchantment | Maximum | Average (log) | Average (wood) |
|:------------|:--------|:--------------|:---------------|
| none        | 1       | 0.167         | 0.25           |
| Silk Touch  | 1       | 1             | 1              |
| Fortune I   | 2       | 0.5           | 0.615          |
| Fortune II  | 3       | 0.766         | 0.998          |
| Fortune III | 4       | 1.055         | 1.54           |

*All values in the above table are rounded to three decimal places.*

## Uses

Bark shavings can be put in a water bottle at a brewing stand to produce dye medium. Only plain
water bottles work for this purpose; potions and splash water bottles will not work.

Bark shavings can be used as furnace fuel for 100 ticks (the same amount of time as sticks,
saplings, and azaleas).

Bark shavings can be composted for a 50% chance to increase the compost level.

## Datapack Details

The item tag `#amurians:bark` controls two behaviors: the ability to make dye medium, and the
ability to be burned in a furnace for 100 ticks. Composters do not support item tags, so the tag
does not affect this behavior.

The `loot_tables/blocks/stripped` folder within each namespace contains the loot tables for
stripping each block.
