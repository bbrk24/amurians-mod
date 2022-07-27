package org.bbrk24.amurians;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.block.PillarBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.registry.Registry;

public class Initializer implements ModInitializer {
    private static final MapColor AZALEA_BARK_COLOR = MapColor.SPRUCE_BROWN;
    private static final MapColor AZALEA_WOOD_COLOR = MapColor.PALE_YELLOW;

    private static final AbstractBlock.Settings AZALEA_PLANKS_SETTINGS =
        FabricBlockSettings.of(Material.WOOD, AZALEA_WOOD_COLOR)
            .strength(2.0f, 3.0f)
            .sounds(BlockSoundGroup.WOOD);
    private static final AbstractBlock.Settings STRIPPED_AZALEA_SETTINGS =
        FabricBlockSettings.of(Material.WOOD, AZALEA_WOOD_COLOR)
            .strength(2.0f)
            .sounds(BlockSoundGroup.WOOD);

    public static final Logger LOGGER = LoggerFactory.getLogger("amurians");

    public static final Item RUBY = new Item(new FabricItemSettings().group(ItemGroup.MISC));
    public static final Item RUBY_SHARD = new Item(new FabricItemSettings().group(ItemGroup.MISC));
    public static final Item RUBY_HELMET = new ArmorItem(
        RubyMaterial.getInstance(),
        EquipmentSlot.HEAD,
        new FabricItemSettings().group(ItemGroup.COMBAT)
    );
    public static final Item RUBY_CHESTPLATE = new ArmorItem(
        RubyMaterial.getInstance(),
        EquipmentSlot.CHEST,
        new FabricItemSettings().group(ItemGroup.COMBAT)
    );
    public static final Item RUBY_LEGGINGS = new ArmorItem(
        RubyMaterial.getInstance(),
        EquipmentSlot.LEGS,
        new FabricItemSettings().group(ItemGroup.COMBAT)
    );
    public static final Item RUBY_BOOTS = new ArmorItem(
        RubyMaterial.getInstance(),
        EquipmentSlot.FEET,
        new FabricItemSettings().group(ItemGroup.COMBAT)
    );

    public static final Block RUBY_BLOCK = new Block(
        FabricBlockSettings.of(Material.METAL, MapColor.PINK)
            .strength(5.0f, 6.0f)
            .requiresTool()
            .sounds(BlockSoundGroup.METAL)
    );
    public static final Block EMERY_TABLE = new EmeryTableBlock(
        FabricBlockSettings.of(Material.STONE)
            .strength(3.5f)
            .requiresTool()
    );
    public static final Block AZALEA_LOG = new PillarBlock(
        FabricBlockSettings.of(
            Material.WOOD,
            state -> state.get(PillarBlock.AXIS) == Axis.Y
                ? AZALEA_WOOD_COLOR
                : AZALEA_BARK_COLOR
        )
            .strength(2.0f)
            .sounds(BlockSoundGroup.WOOD)
    );
    public static final Block AZALEA_PLANKS = new Block(AZALEA_PLANKS_SETTINGS);
    public static final Block STRIPPED_AZALEA_LOG = new PillarBlock(STRIPPED_AZALEA_SETTINGS);
    public static final Block AZALEA_WOOD = new PillarBlock(
        FabricBlockSettings.of(Material.WOOD, AZALEA_BARK_COLOR)
            .strength(2.0f)
            .sounds(BlockSoundGroup.WOOD)
    );
    public static final Block STRIPPED_AZALEA_WOOD = new PillarBlock(STRIPPED_AZALEA_SETTINGS);
    public static final Block AZALEA_SLAB = new SlabBlock(AZALEA_PLANKS_SETTINGS);
    public static final Block AZALEA_STAIRS = new StairsBlock(
        AZALEA_PLANKS.getDefaultState(),
        AZALEA_PLANKS_SETTINGS
    );
    public static final Block AZALEA_FENCE = new FenceBlock(AZALEA_PLANKS_SETTINGS);
    public static final Block AZALEA_FENCE_GATE = new FenceGateBlock(AZALEA_PLANKS_SETTINGS);

    public static final EntityType<AmurianEntity> AMURIAN = Registry.register(
        Registry.ENTITY_TYPE,
        new Identifier("amurians", "amurian"),
        FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, AmurianEntity::new)
            .dimensions(EntityDimensions.fixed(0.6f, 1.8f))
            .build()
    );

    public static final ScreenHandlerType<EmeryTableScreenHandler> EMERY_TABLE_SCREEN_HANDLER_TYPE =
        new ScreenHandlerType<>(EmeryTableScreenHandler::new);
    public static final RecipeType<EmeryTableRecipe> EMERY_TABLE_RECIPE_TYPE =
        RecipeType.register("amurians:emery_table");
    public static final RecipeSerializer<EmeryTableRecipe> EMERY_TABLE_RECIPE_SERIALIZER =
        RecipeSerializer.register("amurians:emery_table", new EmeryTableRecipeSerializer());

    @Override
    public void onInitialize() {
        registerBlock(RUBY_BLOCK, "ruby_block", ItemGroup.BUILDING_BLOCKS);
        registerBlock(EMERY_TABLE, "emery_table", ItemGroup.DECORATIONS);
        registerBlock(AZALEA_LOG, "azalea_log", ItemGroup.BUILDING_BLOCKS);
        registerBlock(AZALEA_PLANKS, "azalea_planks", ItemGroup.BUILDING_BLOCKS);
        registerBlock(STRIPPED_AZALEA_LOG, "stripped_azalea_log", ItemGroup.BUILDING_BLOCKS);
        registerBlock(AZALEA_WOOD, "azalea_wood", ItemGroup.BUILDING_BLOCKS);
        registerBlock(STRIPPED_AZALEA_WOOD, "stripped_azalea_wood", ItemGroup.BUILDING_BLOCKS);
        registerBlock(AZALEA_SLAB, "azalea_slab", ItemGroup.BUILDING_BLOCKS);
        registerBlock(AZALEA_STAIRS, "azalea_stairs", ItemGroup.BUILDING_BLOCKS);
        registerBlock(AZALEA_FENCE, "azalea_fence", ItemGroup.DECORATIONS);
        registerBlock(AZALEA_FENCE_GATE, "azalea_fence_gate", ItemGroup.REDSTONE);

        StrippableBlockRegistry.register(AZALEA_LOG, STRIPPED_AZALEA_LOG);
        StrippableBlockRegistry.register(AZALEA_WOOD, STRIPPED_AZALEA_WOOD);

        registerItem(RUBY, "ruby");
        registerItem(RUBY_SHARD, "ruby_shard");
        registerItem(RUBY_HELMET, "ruby_helmet");
        registerItem(RUBY_CHESTPLATE, "ruby_chestplate");
        registerItem(RUBY_LEGGINGS, "ruby_leggings");
        registerItem(RUBY_BOOTS, "ruby_boots");

        FuelRegistry.INSTANCE.add(AZALEA_FENCE, 300);
        FuelRegistry.INSTANCE.add(AZALEA_FENCE_GATE, 300);

        FabricDefaultAttributeRegistry.register(AMURIAN, AmurianEntity.createAmurianAttributes());

        Registry.register(
            Registry.SCREEN_HANDLER,
            new Identifier("amurians", "emery_table"),
            EMERY_TABLE_SCREEN_HANDLER_TYPE
        );
        // We need to use this inside this method, to ensure the register call is made before the
        // method returns. However, I don't have anything to do with it, so just assert that it
        // exists.
        assert EMERY_TABLE_RECIPE_SERIALIZER != null;
    }

    private static void registerBlock(Block block, String name, ItemGroup group) {
        Identifier id = new Identifier("amurians", name);
        Registry.register(Registry.BLOCK, id, block);
        Registry.register(
            Registry.ITEM,
            id,
            new BlockItem(block, new FabricItemSettings().group(group))
        );
    }

    private static void registerItem(Item item, String name) {
        Registry.register(Registry.ITEM, new Identifier("amurians", name), item);
    }
}
