package org.bbrk24.amurians.hishai

import kotlin.math.min

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Fertilizable
import net.minecraft.block.PlantBlock
import net.minecraft.block.ShapeContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Items
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.tag.TagKey
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.random.Random
import net.minecraft.util.registry.Registry
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.event.GameEvent

import org.bbrk24.amurians.Initializer

private val LAND_BLOCKS = TagKey.of(Registry.BLOCK_KEY, Identifier("amurians", "hishai_plantable"))

internal val AGE = Properties.AGE_15
internal const val MAX_AGE = 15
internal const val MEDIUM_CUTOFF_AGE = 4
internal const val LARGE_CUTOFF_AGE = 10

private val SMALL_OUTLINE = Block.createCuboidShape(5.0, 0.0, 5.0, 11.0, 8.0, 11.0)
private val MEDIUM_OUTLINE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 15.0, 14.0)
private val LARGE_OUTLINE = Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 16.0, 15.0)

private val SMALL_COLLISION = VoxelShapes.empty()
private val MEDIUM_COLLISION = Block.createCuboidShape(7.0, 0.0, 7.0, 9.0, 16.0, 9.0)
private val LARGE_COLLISION = Block.createCuboidShape(6.0, 0.0, 6.0, 10.0, 24.0, 10.0)

internal fun dropFruit(world: World, pos: BlockPos) {
    Block.dropStack(world, pos, ItemStack(Initializer.HISHAI_FRUIT))
}

class HishaiBlock(settings: Settings) : PlantBlock(settings), Fertilizable {
    init {
        setDefaultState(
            stateManager.getDefaultState()
                .with(AGE, 0)
        )
    }

    private fun shouldGrow(state: BlockState, pos: BlockPos, world: World): Boolean {
        if (state.get(AGE) >= MAX_AGE) {
            return false
        }
        val abovePos = pos.up()
        if (!world.isInBuildLimit(abovePos)) {
            return false
        }
        val aboveState = world.getBlockState(abovePos)

        return (aboveState.isAir() || aboveState.isOf(Initializer.HISHAI_TOP)) &&
            world.getBaseLightLevel(abovePos, 0) >= 9
    }

    private fun growBy(state: BlockState, world: ServerWorld, pos: BlockPos, amount: Int) {
        val newAge = min(state.get(AGE) + amount, MAX_AGE)
        world.setBlockState(pos, state.with(AGE, newAge), Block.NOTIFY_LISTENERS)

        if (newAge == MAX_AGE) {
            world.setBlockState(
                pos.up(),
                Initializer.HISHAI_TOP.getDefaultState()
                    .with(Properties.BERRIES, true)
            )
        } else if (newAge >= LARGE_CUTOFF_AGE) {
            world.setBlockState(pos.up(), Initializer.HISHAI_TOP.getDefaultState())
        }
    }

    override fun canPlantOnTop(floor: BlockState, world: BlockView, pos: BlockPos): Boolean {
        return floor.isIn(LAND_BLOCKS)
    }

    override fun isFertilizable(
        world: BlockView,
        pos: BlockPos,
        state: BlockState,
        isClient: Boolean
    ) = true

    override fun canGrow(world: World, random: Random, pos: BlockPos, state: BlockState): Boolean {
        return shouldGrow(state, pos, world)
    }

    override fun grow(world: ServerWorld, random: Random, pos: BlockPos, state: BlockState) {
        growBy(state, world, pos, random.nextBetween(2, 3))
    }

    override fun hasRandomTicks(state: BlockState) = state.get(AGE) < MAX_AGE

    override fun randomTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
        val age = state.get(AGE)
        if (shouldGrow(state, pos, world)) {
            growBy(state, world, pos, 1)
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(state))
            val abovePos = pos.up()
            world.emitGameEvent(
                GameEvent.BLOCK_CHANGE,
                abovePos,
                GameEvent.Emitter.of(world.getBlockState(abovePos))
            )
        }
    }

    override fun getOutlineShape(
        state: BlockState,
        world: BlockView,
        pos: BlockPos,
        ctx: ShapeContext
    ): VoxelShape {
        val age = state.get(AGE)
        if (age < MEDIUM_CUTOFF_AGE) {
            return SMALL_OUTLINE
        }
        if (age < LARGE_CUTOFF_AGE) {
            return MEDIUM_OUTLINE
        }
        return LARGE_OUTLINE
    }

    override fun getCollisionShape(
        state: BlockState,
        world: BlockView,
        pos: BlockPos,
        ctx: ShapeContext
    ): VoxelShape {
        val age = state.get(AGE)
        if (age < MEDIUM_CUTOFF_AGE) {
            return SMALL_COLLISION
        }
        if (age < LARGE_CUTOFF_AGE) {
            return MEDIUM_COLLISION
        }
        return LARGE_COLLISION
    }

    override fun getCameraCollisionShape(
        state: BlockState,
        world: BlockView,
        pos: BlockPos,
        ctx: ShapeContext
    ) = getOutlineShape(state, world, pos, ctx)

    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity,
        hand: Hand,
        hit: BlockHitResult
    ): ActionResult {
        val age = state.get(AGE)
        if (state.get(AGE) < MAX_AGE) {
            return ActionResult.PASS
        }
        dropFruit(world, pos)
        world.setBlockState(pos, state.with(AGE, LARGE_CUTOFF_AGE), Block.NOTIFY_LISTENERS)
        world.setBlockState(pos.up(), Initializer.HISHAI_TOP.getDefaultState())
        world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(state))
        return ActionResult.success(world.isClient)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(AGE);
    }

    // c'mon, let me use transparent pixels
    override fun isTranslucent(state: BlockState, world: BlockView, pos: BlockPos) = true
}