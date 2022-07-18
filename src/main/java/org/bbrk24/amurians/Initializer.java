package org.bbrk24.amurians;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Initializer implements ModInitializer {
    private static final Item RUBY = new Item(new FabricItemSettings().group(ItemGroup.MISC));

    private static final Block RUBY_BLOCK = new Block(
        FabricBlockSettings.of(Material.METAL)
            .strength(5.0f, 6.0f)
            .requiresTool()
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
    }
}
