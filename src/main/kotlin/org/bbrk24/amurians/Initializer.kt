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

package org.bbrk24.amurians

import org.slf4j.LoggerFactory

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry
import net.fabricmc.fabric.api.registry.FuelRegistry
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry
import net.minecraft.block.Block
import net.minecraft.block.FenceBlock
import net.minecraft.block.FenceGateBlock
import net.minecraft.block.MapColor
import net.minecraft.block.Material
import net.minecraft.block.PillarBlock
import net.minecraft.block.SlabBlock
import net.minecraft.block.StairsBlock
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.SpawnGroup
import net.minecraft.item.AliasedBlockItem
import net.minecraft.item.ArmorItem
import net.minecraft.item.BlockItem
import net.minecraft.item.FoodComponents
import net.minecraft.item.Item
import net.minecraft.item.ItemConvertible
import net.minecraft.item.ItemGroup
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.RecipeType
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction.Axis
import net.minecraft.util.registry.Registry

import org.bbrk24.amurians.amurian.AmurianEntity
import org.bbrk24.amurians.emerytable.EmeryTableBlock
import org.bbrk24.amurians.emerytable.EmeryTableRecipe
import org.bbrk24.amurians.emerytable.EmeryTableRecipeSerializer
import org.bbrk24.amurians.emerytable.EmeryTableScreenHandler
import org.bbrk24.amurians.hishai.*

private val AZALEA_BARK_COLOR = MapColor.SPRUCE_BROWN
private val AZALEA_WOOD_COLOR = MapColor.PALE_YELLOW

private val AZALEA_PLANKS_SETTINGS = FabricBlockSettings.of(Material.WOOD, AZALEA_WOOD_COLOR)
    .strength(2.0f, 3.0f)
    .sounds(BlockSoundGroup.WOOD)
private val STRIPPED_AZALEA_SETTINGS = FabricBlockSettings.of(Material.WOOD, AZALEA_WOOD_COLOR)
    .strength(2.0f)
    .sounds(BlockSoundGroup.WOOD)

class Initializer : ModInitializer {
    companion object {
        @JvmStatic
        val LOGGER = LoggerFactory.getLogger("amurians")

        // blocks
        val RUBY_BLOCK = Block(
            FabricBlockSettings.of(Material.METAL, MapColor.PINK)
                .strength(5.0f, 6.0f)
                .requiresTool()
                .sounds(BlockSoundGroup.METAL)
        )
        val EMERY_TABLE = EmeryTableBlock(
            FabricBlockSettings.of(Material.STONE)
                .strength(3.5f)
                .requiresTool()
        )
        val AZALEA_LOG = PillarBlock(
            FabricBlockSettings.of(Material.WOOD) { state ->
                if (state.get(PillarBlock.AXIS) == Axis.Y)
                    AZALEA_WOOD_COLOR
                else
                    AZALEA_BARK_COLOR
            }
                .strength(2.0f)
                .sounds(BlockSoundGroup.WOOD)
        )
        val AZALEA_PLANKS = Block(AZALEA_PLANKS_SETTINGS)
        val STRIPPED_AZALEA_LOG = PillarBlock(STRIPPED_AZALEA_SETTINGS)
        val AZALEA_WOOD = PillarBlock(
            FabricBlockSettings.of(Material.WOOD, AZALEA_BARK_COLOR)
                .strength(2.0f)
                .sounds(BlockSoundGroup.WOOD)
        )
        val STRIPPED_AZALEA_WOOD = PillarBlock(STRIPPED_AZALEA_SETTINGS)
        val AZALEA_SLAB = SlabBlock(AZALEA_PLANKS_SETTINGS)
        val AZALEA_STAIRS = StairsBlock(AZALEA_PLANKS.getDefaultState(), AZALEA_PLANKS_SETTINGS)
        val AZALEA_FENCE = FenceBlock(AZALEA_PLANKS_SETTINGS)
        val AZALEA_FENCE_GATE = FenceGateBlock(AZALEA_PLANKS_SETTINGS)
        val HISHAI_PLANT = HishaiBlock(
            FabricBlockSettings.of(Material.PLANT)
                .strength(0.1f)
                .sounds(BlockSoundGroup.WOOD)
                .ticksRandomly()
                .nonOpaque()
        )
        val HISHAI_TOP = HishaiTopBlock(
            FabricBlockSettings.of(Material.PLANT)
                .strength(0.1f)
                .sounds(BlockSoundGroup.WOOD)
                .nonOpaque()
                .noCollision()
                .dropsNothing()
        )

        @JvmStatic
        fun getAzaleaLog(): Block = AZALEA_LOG

        // items
        val RUBY = Item(FabricItemSettings().group(ItemGroup.MISC))
        val RUBY_SHARD = Item(FabricItemSettings().group(ItemGroup.MISC))
        val RUBY_HELMET = ArmorItem(
            RubyMaterial,
            EquipmentSlot.HEAD,
            FabricItemSettings().group(ItemGroup.COMBAT)
        )
        val RUBY_CHESTPLATE = ArmorItem(
            RubyMaterial,
            EquipmentSlot.CHEST,
            FabricItemSettings().group(ItemGroup.COMBAT)
        )
        val RUBY_LEGGINGS = ArmorItem(
            RubyMaterial,
            EquipmentSlot.LEGS,
            FabricItemSettings().group(ItemGroup.COMBAT)
        )
        val RUBY_BOOTS = ArmorItem(
            RubyMaterial,
            EquipmentSlot.FEET,
            FabricItemSettings().group(ItemGroup.COMBAT)
        )
        val HISHAI_FRUIT = Item(
            FabricItemSettings().group(ItemGroup.FOOD)
                .food(FoodComponents.APPLE)
        )
        val HISHAI_SEEDS = AliasedBlockItem(
            HISHAI_PLANT,
            FabricItemSettings().group(ItemGroup.MISC)
        )

        @JvmStatic
        fun getHishaiSeeds(): ItemConvertible = HISHAI_SEEDS

        // entities
        val AMURIAN = FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, ::AmurianEntity)
            .dimensions(EntityDimensions.fixed(0.6f, 1.8f))
            .build()

        // misc
        val EMERY_TABLE_SCREEN_HANDLER_TYPE = ScreenHandlerType(::EmeryTableScreenHandler)
        val EMERY_TABLE_RECIPE_TYPE = RecipeType.register<EmeryTableRecipe>("amurians:emery_table")
    }

    private fun registerBlock(block: Block, name: String, group: ItemGroup) {
        val id = Identifier("amurians", name)
        Registry.register(Registry.BLOCK, id, block)
        Registry.register(Registry.ITEM, id, BlockItem(block, FabricItemSettings().group(group)))
    }

    private fun registerItem(item: Item, name: String) {
        Registry.register(Registry.ITEM, Identifier("amurians", name), item)
    }

    override fun onInitialize() {
        // blocks
        registerBlock(RUBY_BLOCK, "ruby_block", ItemGroup.BUILDING_BLOCKS)
        registerBlock(EMERY_TABLE, "emery_table", ItemGroup.DECORATIONS)
        registerBlock(AZALEA_LOG, "azalea_log", ItemGroup.BUILDING_BLOCKS)
        registerBlock(AZALEA_PLANKS, "azalea_planks", ItemGroup.BUILDING_BLOCKS)
        registerBlock(STRIPPED_AZALEA_LOG, "stripped_azalea_log", ItemGroup.BUILDING_BLOCKS)
        registerBlock(AZALEA_WOOD, "azalea_wood", ItemGroup.BUILDING_BLOCKS)
        registerBlock(STRIPPED_AZALEA_WOOD, "stripped_azalea_wood", ItemGroup.BUILDING_BLOCKS)
        registerBlock(AZALEA_SLAB, "azalea_slab", ItemGroup.BUILDING_BLOCKS)
        registerBlock(AZALEA_STAIRS, "azalea_stairs", ItemGroup.BUILDING_BLOCKS)
        registerBlock(AZALEA_FENCE, "azalea_fence", ItemGroup.DECORATIONS)
        registerBlock(AZALEA_FENCE_GATE, "azalea_fence_gate", ItemGroup.REDSTONE)

        Registry.register(Registry.BLOCK, Identifier("amurians", "hishai_plant"), HISHAI_PLANT)
        Registry.register(Registry.BLOCK, Identifier("amurians", "hishai_top"), HISHAI_TOP)

        StrippableBlockRegistry.register(AZALEA_LOG, STRIPPED_AZALEA_LOG)
        StrippableBlockRegistry.register(AZALEA_WOOD, STRIPPED_AZALEA_WOOD)

        // items
        registerItem(RUBY, "ruby")
        registerItem(RUBY_SHARD, "ruby_shard")
        registerItem(RUBY_HELMET, "ruby_helmet")
        registerItem(RUBY_CHESTPLATE, "ruby_chestplate")
        registerItem(RUBY_LEGGINGS, "ruby_leggings")
        registerItem(RUBY_BOOTS, "ruby_boots")
        registerItem(HISHAI_FRUIT, "hishai_fruit")
        registerItem(HISHAI_SEEDS, "hishai_seeds")

        FuelRegistry.INSTANCE.add(AZALEA_FENCE, 300)
        FuelRegistry.INSTANCE.add(AZALEA_FENCE_GATE, 300)

        CompostingChanceRegistry.INSTANCE.add(HISHAI_FRUIT, 0.65f)
        CompostingChanceRegistry.INSTANCE.add(HISHAI_PLANT, 0.3f)

        // entities
        FabricDefaultAttributeRegistry.register(AMURIAN, AmurianEntity.createAmurianAttributes())
        Registry.register(Registry.ENTITY_TYPE, Identifier("amurians", "amurian"), AMURIAN)

        // screens
        Registry.register(
            Registry.SCREEN_HANDLER,
            Identifier("amurians", "emery_table"),
            EMERY_TABLE_SCREEN_HANDLER_TYPE
        )

        // recipes
        RecipeSerializer.register("amurians:emery_table", EmeryTableRecipeSerializer)
    }
}

