package org.bbrk24.amurians;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.block.PillarBlock;
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
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;

public class Initializer implements ModInitializer {
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
        FabricBlockSettings.of(Material.METAL, MapColor.BRIGHT_RED)
            .strength(5.0f, 6.0f)
            .requiresTool()
    );
    public static final Block EMERY_TABLE = new EmeryTableBlock();
    public static final Block AZALEA_LOG = new PillarBlock(
        FabricBlockSettings.of(
            Material.WOOD,
            state -> state.get(PillarBlock.AXIS) == Direction.Axis.Y
                ? MapColor.PALE_YELLOW
                : MapColor.SPRUCE_BROWN
        )
            .strength(2.0f)
    );
    public static final Block AZALEA_PLANKS = new Block(
        FabricBlockSettings.of(Material.WOOD, MapColor.PALE_YELLOW)
            .strength(2.0f)
    );
    public static final Block STRIPPED_AZALEA_LOG = new PillarBlock(
        FabricBlockSettings.of(Material.WOOD, MapColor.PALE_YELLOW)
            .strength(2.0f)
    );
    public static final Block AZALEA_WOOD = new PillarBlock(
        FabricBlockSettings.of(Material.WOOD, MapColor.SPRUCE_BROWN)
            .strength(2.0f)
    );
    public static final Block STRIPPED_AZALEA_WOOD = new PillarBlock(
        FabricBlockSettings.of(Material.WOOD, MapColor.PALE_YELLOW)
            .strength(2.0f)
    );

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
        Registry.register(
            Registry.BLOCK,
            new Identifier("amurians", "ruby_block"),
            RUBY_BLOCK
        );
        Registry.register(
            Registry.BLOCK,
            new Identifier("amurians", "emery_table"),
            EMERY_TABLE
        );
        Registry.register(
            Registry.BLOCK,
            new Identifier("amurians", "azalea_log"),
            AZALEA_LOG
        );
        Registry.register(
            Registry.BLOCK,
            new Identifier("amurians", "azalea_planks"),
            AZALEA_PLANKS
        );
        Registry.register(
            Registry.BLOCK,
            new Identifier("amurians", "stripped_azalea_log"),
            STRIPPED_AZALEA_LOG
        );
        Registry.register(
            Registry.BLOCK,
            new Identifier("amurians", "azalea_wood"),
            AZALEA_WOOD
        );
        Registry.register(
            Registry.BLOCK,
            new Identifier("amurians", "stripped_azalea_wood"),
            STRIPPED_AZALEA_WOOD
        );

        Registry.register(
            Registry.ITEM,
            new Identifier("amurians", "ruby_block"),
            new BlockItem(RUBY_BLOCK, new FabricItemSettings().group(ItemGroup.BUILDING_BLOCKS))
        );
        Registry.register(
            Registry.ITEM,
            new Identifier("amurians", "emery_table"),
            new BlockItem(EMERY_TABLE, new FabricItemSettings().group(ItemGroup.DECORATIONS))
        );
        Registry.register(
            Registry.ITEM,
            new Identifier("amurians", "azalea_log"),
            new BlockItem(AZALEA_LOG, new FabricItemSettings().group(ItemGroup.BUILDING_BLOCKS))
        );
        Registry.register(
            Registry.ITEM,
            new Identifier("amurians", "azalea_planks"),
            new BlockItem(AZALEA_PLANKS, new FabricItemSettings().group(ItemGroup.BUILDING_BLOCKS))
        );
        Registry.register(
            Registry.ITEM,
            new Identifier("amurians", "stripped_azalea_log"),
            new BlockItem(STRIPPED_AZALEA_LOG, new FabricItemSettings().group(ItemGroup.BUILDING_BLOCKS))
        );
        Registry.register(
            Registry.ITEM,
            new Identifier("amurians", "azalea_wood"),
            new BlockItem(AZALEA_WOOD, new FabricItemSettings().group(ItemGroup.BUILDING_BLOCKS))
        );
         Registry.register(
            Registry.ITEM,
            new Identifier("amurians", "stripped_azalea_wood"),
            new BlockItem(STRIPPED_AZALEA_WOOD, new FabricItemSettings().group(ItemGroup.BUILDING_BLOCKS))
        );

        Registry.register(
            Registry.ITEM,
            new Identifier("amurians", "ruby"),
            RUBY
        );
        Registry.register(
            Registry.ITEM,
            new Identifier("amurians", "ruby_shard"),
            RUBY_SHARD
        );
        Registry.register(
            Registry.ITEM,
            new Identifier("amurians", "ruby_helmet"),
            RUBY_HELMET
        );
        Registry.register(
            Registry.ITEM,
            new Identifier("amurians", "ruby_chestplate"),
            RUBY_CHESTPLATE
        );
        Registry.register(
            Registry.ITEM,
            new Identifier("amurians", "ruby_leggings"),
            RUBY_LEGGINGS
        );
        Registry.register(
            Registry.ITEM,
            new Identifier("amurians", "ruby_boots"),
            RUBY_BOOTS
        );

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
}
