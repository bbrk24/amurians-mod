package org.bbrk24.amurians

import kotlin.math.min

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Fertilizable
import net.minecraft.block.PlantBlock
import net.minecraft.block.ShapeContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
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

private val LAND_BLOCKS = TagKey.of(Registry.BLOCK_KEY, Identifier("amurians", "hishai_plantable"))

private val AGE = Properties.AGE_15
private const val MAX_AGE = 15
private const val MEDIUM_CUTOFF_AGE = 4
private const val LARGE_CUTOFF_AGE = 10

private val SMALL_OUTLINE = Block.createCuboidShape(5.0, 0.0, 5.0, 11.0, 8.0, 11.0)
private val MEDIUM_OUTLINE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 15.0, 14.0)
private val LARGE_OUTLINE = Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 24.0, 15.0)

private val SMALL_COLLISION = VoxelShapes.empty()
private val MEDIUM_COLLISION = Block.createCuboidShape(6.0, 0.0, 6.0, 10.0, 16.0, 10.0)
private val LARGE_COLLISION = Block.createCuboidShape(6.0, 0.0, 6.0, 10.0, 24.0, 10.0)

class HishaiBlock(settings: Settings) : PlantBlock(settings), Fertilizable {
    init {
        setDefaultState(
            stateManager.getDefaultState()
                .with(AGE, 0)
        )
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
        return state.get(AGE) < MAX_AGE
    }

    override fun grow(world: ServerWorld, random: Random, pos: BlockPos, state: BlockState) {
        val age = state.get(AGE)
        val toAdd = random.nextBetween(2, 3)
        world.setBlockState(pos, state.with(AGE, min(age + toAdd, MAX_AGE)), Block.NOTIFY_LISTENERS)
    }

    override fun hasRandomTicks(state: BlockState) = state.get(AGE) < MAX_AGE

    override fun randomTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
        val age = state.get(AGE)
        if (age < MAX_AGE && world.getBaseLightLevel(pos.up(), 0) >= 9) {
            world.setBlockState(pos, state.with(AGE, age + 1), Block.NOTIFY_LISTENERS)
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(state))
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

    @Suppress("DEPRECATION")
    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity,
        hand: Hand,
        hit: BlockHitResult
    ): ActionResult {
        if (player.getStackInHand(hand).isOf(Items.BONE_MEAL)) {
            return ActionResult.PASS
        }
        if (state.get(AGE) < MAX_AGE) {
            return super.onUse(state, world, pos, player, hand, hit)
        }
        Block.dropStack(world, pos, ItemStack(Initializer.HISHAI_FRUIT))
        world.setBlockState(pos, state.with(AGE, LARGE_CUTOFF_AGE), Block.NOTIFY_LISTENERS)
        world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(state))
        return ActionResult.success(world.isClient)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(AGE);
    }

    // c'mon, let me use transparent pixels
    override fun isTranslucent(state: BlockState, world: BlockView, pos: BlockPos) = true
}
