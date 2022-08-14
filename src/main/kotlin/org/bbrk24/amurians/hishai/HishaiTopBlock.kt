package org.bbrk24.amurians.hishai

import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.block.BlockState
import net.minecraft.block.Fertilizable
import net.minecraft.block.ShapeContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Items
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import net.minecraft.world.WorldEvents
import net.minecraft.world.event.GameEvent

import org.bbrk24.amurians.Initializer

private val OUTLINE = Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 8.0, 15.0)

class HishaiTopBlock(settings: Settings) : Block(settings), Fertilizable {
    init {
        setDefaultState(
            stateManager.getDefaultState()
                .with(Properties.BERRIES, false)
        )
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(Properties.BERRIES);
    }

    override fun isFertilizable(
        world: BlockView,
        pos: BlockPos,
        state: BlockState,
        isClient: Boolean
    ) = !state.get(Properties.BERRIES)

    override fun canGrow(world: World, random: Random, pos: BlockPos, state: BlockState): Boolean {
        return !state.get(Properties.BERRIES) && world.getBaseLightLevel(pos, 0) >= MIN_LIGHT_LEVEL
    }

    override fun grow(world: ServerWorld, random: Random, pos: BlockPos, state: BlockState) {
        val belowPos = pos.down()
        val belowState = world.getBlockState(belowPos)
        if (belowState.isOf(Initializer.HISHAI_PLANT)) {
            Initializer.HISHAI_PLANT.grow(world, random, belowPos, belowState)
        }
    }

    override fun getOutlineShape(
        state: BlockState,
        world: BlockView,
        pos: BlockPos,
        ctx: ShapeContext
    ) = OUTLINE

    @Suppress("DEPRECATION")
    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity,
        hand: Hand,
        hit: BlockHitResult
    ): ActionResult {
        if (!state.get(Properties.BERRIES)) {
            return ActionResult.PASS
        }
        dropFruit(world, pos)
        world.setBlockState(pos, state.with(Properties.BERRIES, false), Block.NOTIFY_LISTENERS)
        world.setBlockState(
            pos.down(),
            Initializer.HISHAI_PLANT.getDefaultState()
                .with(AGE, LARGE_CUTOFF_AGE)
        )
        world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(state))
        return ActionResult.success(world.isClient)
    }

    override fun getPickStack(world: BlockView, pos: BlockPos, state: BlockState): ItemStack {
        return ItemStack(Initializer.HISHAI_PLANT)
    }

    override fun onBreak(world: World, pos: BlockPos, state: BlockState, player: PlayerEntity) {
        world.breakBlock(pos.down(), !player.isCreative())
        super.onBreak(world, pos, state, player)
    }

    override fun scheduledTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
        val belowState = world.getBlockState(pos.down())
        if (!belowState.isOf(Initializer.HISHAI_PLANT) || belowState.get(AGE) < LARGE_CUTOFF_AGE) {
            world.breakBlock(pos, false)
        }
    }

    @Suppress("DEPRECATION")
    override fun getStateForNeighborUpdate(
        state: BlockState,
        direction: Direction,
        neighborState: BlockState,
        world: WorldAccess,
        pos: BlockPos,
        neighborPos: BlockPos
    ): BlockState {
        val belowState = world.getBlockState(pos.down())
        if (!belowState.isOf(Initializer.HISHAI_PLANT) || belowState.get(AGE) < LARGE_CUTOFF_AGE) {
            world.createAndScheduleBlockTick(pos, this, 1)
        }
        return super.getStateForNeighborUpdate(
            state,
            direction,
            neighborState,
            world,
            pos,
            neighborPos
        )
    }
}
