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

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.HorizontalFacingBlock
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.SimpleNamedScreenHandlerFactory
import net.minecraft.stat.Stats
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

private val TITLE = Text.translatable("container.emery_table")

class EmeryTableBlock(settings: Settings) : HorizontalFacingBlock(settings) {
    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        return getDefaultState().with(
            Properties.HORIZONTAL_FACING,
            ctx.getPlayerFacing().getOpposite()
        )
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(Properties.HORIZONTAL_FACING)
    }

    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity,
        hand: Hand,
        hit: BlockHitResult
    ): ActionResult {
        if (world.isClient) {
            return ActionResult.SUCCESS
        }
        player.openHandledScreen(state.createScreenHandlerFactory(world, pos))
        player.incrementStat(Stats.INTERACT_WITH_STONECUTTER)
        return ActionResult.CONSUME
    }

    override fun createScreenHandlerFactory(
    state: BlockState,
        world: World,
        pos: BlockPos
    ): NamedScreenHandlerFactory? {
        return SimpleNamedScreenHandlerFactory(
            { syncId, playerInventory, _ ->
                EmeryTableScreenHandler(
                    syncId,
                    playerInventory,
                    ScreenHandlerContext.create(world, pos)
                )
            },
            TITLE
        )
    }
}
