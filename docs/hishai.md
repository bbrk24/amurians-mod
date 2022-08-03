# Hishai

Hishai is a fruit-bearing plant found in sparse jungles and some amurian villages. Like berries,
hishai fruit can be picked without destroying the entire plant. When young, the plants are less than
one block tall; fully-grown hishai plants are about a block and a half in height. Unlike similar
plants, hishai bushes are destroyed fastest with an axe, and have a collision box no more than a
quarter of a block wide.

When picked, hishai plants drop 1-3 fruit and usually take around five minutes to regrow, though
growth time varies significantly as it's based on random ticks. Like crops and berries, hishai
growth can be sped up with the help of bees. Hishai fruit can be eaten directly, smelted to make
cyan powder dye, or crafted into hishai seeds.

## Datapack Details

There are two block tags relating to hishai plants: `#amurians:hishai` and
`#amurians:hishai_plantable`. The former is not referenced by the code directly, but is referenced
by two other block tags: `#minecraft:bee_growables` and `#minecraft:mineable/axe`. The latter
controls which blocks hishai seeds can be planted on. Mycelium is intentionally excluded; if
mycelium spreads onto dirt where a hishai plant is growing, the plant will break.
