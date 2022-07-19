package org.bbrk24.amurians;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Initializer implements ModInitializer {
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
        FabricBlockSettings.of(Material.METAL)
            .strength(5.0f, 6.0f)
            .requiresTool()
    );

    public static final EntityType<AmurianEntity> AMURIAN = Registry.register(
        Registry.ENTITY_TYPE,
        new Identifier("amurians", "amurian"),
        FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, AmurianEntity::new)
            .dimensions(EntityDimensions.fixed(0.6f, 1.8f))
            .build()
    );

    @Override
	public void onInitialize() {
        Registry.register(
            Registry.BLOCK,
            new Identifier("amurians", "ruby_block"),
            RUBY_BLOCK
        );

        Registry.register(
            Registry.ITEM,
            new Identifier("amurians", "ruby_block"),
            new BlockItem(RUBY_BLOCK, new FabricItemSettings().group(ItemGroup.BUILDING_BLOCKS))
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
    }
}
