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

package org.bbrk24.amurians.emerytable

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.CraftingResultInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.screen.Property
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.slot.Slot
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.world.World

import org.bbrk24.amurians.Initializer

class EmeryTableScreenHandler(
    syncId: Int,
    playerInventory: PlayerInventory,
    private val context: ScreenHandlerContext
) : ScreenHandler(Initializer.EMERY_TABLE_SCREEN_HANDLER_TYPE, syncId) {
    private val world = playerInventory.player.world
    private val selectedRecipe = Property.create()
    private var inputStack = ItemStack.EMPTY
    var availableRecipes = mutableListOf<EmeryTableRecipe>()
        private set

    var contentsChangedListener = Runnable { }
    private val input = object : SimpleInventory(1) {
        override fun markDirty() {
            super.markDirty()
            onContentChanged(this)
            contentsChangedListener.run()
        }
    }
    private val output = CraftingResultInventory()

    private val inputSlot = addSlot(Slot(input, 0, 20, 33))
    private val outputSlot = addSlot(object : Slot(output, 1, 143, 33) {
        override fun canInsert(stack: ItemStack) = false

        override fun onTakeItem(player: PlayerEntity, stack: ItemStack) {
            stack.onCraft(player.world, player, stack.getCount())
            output.unlockLastRecipe(player)
            val itemStack = inputSlot.takeStack(1)
            if (!itemStack.isEmpty) {
                populateResult()
            }
            context.run { world, pos ->
                val time = world.getTime()
                if (time != lastTakeTime) {
                    world.playSound(
                        null,
                        pos,
                        SoundEvents.UI_STONECUTTER_TAKE_RESULT,
                        SoundCategory.BLOCKS,
                        1.0f,
                        1.0f
                    )
                    lastTakeTime = time
                }
            }
            super.onTakeItem(player, stack)
        }
    })
    private var lastTakeTime = 0L

    init {
        for (i in 1..3) {
            for (j in 0 until 9) {
                addSlot(Slot(playerInventory, j + i * 9, 8 + j * 18, 66 + i * 18));
            }
        }
        for (i in 0 until 9) {
            addSlot(Slot(playerInventory, i, 8 + i * 18, 142));
        }
        addProperty(selectedRecipe);
    }

    constructor(syncId: Int, playerInventory: PlayerInventory) : this(
        syncId,
        playerInventory,
        ScreenHandlerContext.EMPTY
    ) { }

    override fun onContentChanged(inventory: Inventory) {
        val itemStack = inputSlot.getStack()
        if (!itemStack.isOf(inputStack.getItem())) {
            inputStack = itemStack.copy()
            updateInput(inventory, inputStack)
        }
    }

    private fun updateInput(inputInv: Inventory, stack: ItemStack) {
        availableRecipes.clear()
        selectedRecipe.set(-1)
        outputSlot.setStack(ItemStack.EMPTY)
        if (!stack.isEmpty()) {
            availableRecipes = world.getRecipeManager()
                .getAllMatches(Initializer.EMERY_TABLE_RECIPE_TYPE, inputInv, world)
        }
    }

    private fun isInBounds(i: Int) = i >= 0 && i < availableRecipes.size

    fun populateResult() {
        if (!availableRecipes.isEmpty() && isInBounds(selectedRecipe.get())) {
            val recipe = availableRecipes.get(selectedRecipe.get())
            output.setLastRecipe(recipe)
            outputSlot.setStack(recipe.craft(input))
        } else {
            outputSlot.setStack(ItemStack.EMPTY)
        }
        sendContentUpdates()
    }

    fun getSelectedRecipe() = selectedRecipe.get()
    fun getAvailableRecipeCount() = availableRecipes.size
    fun canCraft() = inputSlot.hasStack() && !availableRecipes.isEmpty()

    override fun canInsertIntoSlot(unused: ItemStack, slot: Slot) = slot.inventory != output

    override fun canUse(player: PlayerEntity): Boolean {
        return ScreenHandler.canUse(context, player, Initializer.EMERY_TABLE)
    }

    override fun onButtonClick(player: PlayerEntity, id: Int): Boolean {
        if (isInBounds(id)) {
            selectedRecipe.set(id)
            populateResult()
        }
        return true
    }

    public override fun transferSlot(player: PlayerEntity, index: Int): ItemStack {
        var retval = ItemStack.EMPTY
        val slot = slots.get(index) as Slot?
        if (slot != null && slot.hasStack()) {
            val slotStack = slot.getStack()
            retval = slotStack.copy()
            if (index == 1) {
                slotStack.getItem().onCraft(slotStack, player.world, player)
                if (!insertItem(slotStack, 2, 38, true)) {
                    return ItemStack.EMPTY
                }
                slot.onQuickTransfer(slotStack, retval)
            } else if (
                // This part is translated from some decompiled Java code that had a deeply nested
                // ternary expression. I'm not quite sure how it works.
                if (index == 0)
                    !insertItem(slotStack, 2, 38, false)
                else if (
                        world.getRecipeManager()
                            .getFirstMatch(
                                Initializer.EMERY_TABLE_RECIPE_TYPE,
                                SimpleInventory(slotStack),
                                world
                            )
                            .isPresent()
                )
                    !insertItem(slotStack, 0, 1, false)
                else if (index >= 2 && index < 29)
                    !insertItem(slotStack, 29, 38, false)
                else
                    index >= 29 && index < 38 && !insertItem(slotStack, 2, 29, false)
            ) {
                return ItemStack.EMPTY
            }
            if (slotStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY)
            }
            slot.markDirty()
            if (slotStack.getCount() == retval.getCount()) {
                return ItemStack.EMPTY
            }
            slot.onTakeItem(player, slotStack)
            sendContentUpdates()
        }
        return retval
    }
}
