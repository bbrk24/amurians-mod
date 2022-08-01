package org.bbrk24.amurians.amurian

// Normally I would use kotlin.collections.List, but Brain#setTaskList requires this ImmutableList
// class specifically, not just any list.
import com.google.common.collect.ImmutableList
import com.mojang.datafixers.util.Pair as MojangPair

import net.minecraft.entity.ai.brain.Brain
import net.minecraft.entity.ai.brain.EntityLookTarget
import net.minecraft.entity.ai.brain.MemoryModuleState
import net.minecraft.entity.ai.brain.MemoryModuleType
import net.minecraft.entity.ai.brain.WalkTarget
import net.minecraft.entity.ai.brain.task.LookAroundTask
import net.minecraft.entity.ai.brain.task.OpenDoorsTask
import net.minecraft.entity.ai.brain.task.StayAboveWaterTask
import net.minecraft.entity.ai.brain.task.Task
import net.minecraft.entity.ai.brain.task.WakeUpTask
import net.minecraft.entity.ai.brain.task.WalkToNearestVisibleWantedItemTask
import net.minecraft.entity.ai.brain.task.WanderAroundTask
import net.minecraft.server.world.ServerWorld

private const val WANDER_RADIUS = 4

object AmurianTaskListProvider {
    fun createCoreTasks(): ImmutableList<
        MojangPair<Int, out Task<in AmurianEntity>>
    > = ImmutableList.of(
        MojangPair.of(0, StayAboveWaterTask(0.8f)),
        MojangPair.of(0, WakeUpTask()),
        MojangPair.of(1, OpenDoorsTask()),
        MojangPair.of(1, AmurianFollowCustomerTask()),
        MojangPair.of(2, LookAroundTask(45, 90)),
        MojangPair.of(2, WanderAroundTask()),
        MojangPair.of(
            2,
            WalkToNearestVisibleWantedItemTask(
                AmurianEntity.WANDER_SPEED.toFloat(),
                false,
                WANDER_RADIUS
            )
        )
    )

    class AmurianFollowCustomerTask : Task<AmurianEntity>(
        mapOf(
            Pair(MemoryModuleType.WALK_TARGET, MemoryModuleState.REGISTERED),
            Pair(MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED)
        ),
        Int.MAX_VALUE
    ) {
        override fun shouldRun(world: ServerWorld, mob: AmurianEntity): Boolean {
            if (!mob.isAlive() || mob.velocityModified) {
                return false
            }
            val customer = mob.getCustomer()
            return customer != null &&
                customer.currentScreenHandler != null &&
                mob.squaredDistanceTo(customer) <= WANDER_RADIUS * WANDER_RADIUS
        }

        override fun shouldKeepRunning(world: ServerWorld, mob: AmurianEntity, l: Long): Boolean {
            return shouldRun(world, mob)
        }

        private fun update(mob: AmurianEntity) {
            val brain = mob.getBrain()
            val customer = mob.getCustomer()

            brain.remember(
                MemoryModuleType.WALK_TARGET,
                WalkTarget(
                    EntityLookTarget(customer, false),
                    AmurianEntity.WANDER_SPEED.toFloat(),
                    WANDER_RADIUS
                )
            )
            brain.remember(MemoryModuleType.LOOK_TARGET, EntityLookTarget(customer, true))
        }

        override fun run(world: ServerWorld, mob: AmurianEntity, l: Long) {
            update(mob)
        }

        override fun keepRunning(world: ServerWorld, mob: AmurianEntity, l: Long) {
            update(mob)
        }

        override fun finishRunning(world: ServerWorld, mob: AmurianEntity, l: Long) {
            val brain = mob.getBrain()
            brain.forget(MemoryModuleType.WALK_TARGET)
            brain.forget(MemoryModuleType.LOOK_TARGET)
        }
    }
}
