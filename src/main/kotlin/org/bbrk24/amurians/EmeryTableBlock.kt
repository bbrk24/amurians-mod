package org.bbrk24.amurians

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
