package org.bbrk24.amurians;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.LookAroundTask;
import net.minecraft.entity.ai.brain.task.OpenDoorsTask;
import net.minecraft.entity.ai.brain.task.StayAboveWaterTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.WakeUpTask;
import net.minecraft.entity.ai.brain.task.WalkToNearestVisibleWantedItemTask;
import net.minecraft.entity.ai.brain.task.WanderAroundTask;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class AmurianTaskListProvider {
    static int WANDER_RADIUS = 4;

    public static ImmutableList<
        Pair<Integer, ? extends Task<? super AmurianEntity>>
    > createCoreTasks() {
        return ImmutableList.of(
            Pair.of(0, new StayAboveWaterTask(0.8f)),
            Pair.of(0, new WakeUpTask()),
            Pair.of(1, new OpenDoorsTask()),
            Pair.of(1, new AmurianFollowCustomerTask()),
            Pair.of(2, new LookAroundTask(45, 90)),
            Pair.of(2, new WanderAroundTask()),
            Pair.of(
                2,
                new WalkToNearestVisibleWantedItemTask<>(
                    (float)AmurianEntity.getWanderSpeed(),
                    false,
                    WANDER_RADIUS
                )
            )
        );
    }

    static class AmurianFollowCustomerTask extends Task<AmurianEntity> {
        public AmurianFollowCustomerTask() {
            super(
                ImmutableMap.of(
                    MemoryModuleType.WALK_TARGET, MemoryModuleState.REGISTERED,
                    MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED
                ),
                Integer.MAX_VALUE
            );
        }

        @Override
        protected boolean shouldRun(ServerWorld world, AmurianEntity amurian) {
            PlayerEntity customer = amurian.getCustomer();
            return amurian.isAlive() &&
                customer != null &&
                !amurian.velocityModified &&
                amurian.squaredDistanceTo(customer) <= (double)(WANDER_RADIUS * WANDER_RADIUS) &&
                customer.currentScreenHandler != null;
        }

        @Override
        protected boolean shouldKeepRunning(ServerWorld world, AmurianEntity amurian, long l) {
            return this.shouldRun(world, amurian);
        }

        private void update(AmurianEntity amurian) {
            Brain<?> brain = amurian.getBrain();
            PlayerEntity customer = amurian.getCustomer();
            brain.remember(
                MemoryModuleType.WALK_TARGET,
                new WalkTarget(
                    new EntityLookTarget(customer, false),
                    (float)AmurianEntity.getWanderSpeed(),
                    WANDER_RADIUS
                )
            );
            brain.remember(
                MemoryModuleType.LOOK_TARGET,
                new EntityLookTarget(customer, true)
            );
        }

        @Override
        protected void run(ServerWorld world, AmurianEntity amurian, long l) {
            this.update(amurian);
        }

        @Override
        protected void keepRunning(ServerWorld world, AmurianEntity amurian, long l) {
            this.update(amurian);
        }

        @Override
        protected void finishRunning(ServerWorld world, AmurianEntity amurian, long l) {
            Brain<?> brain = amurian.getBrain();
            brain.forget(MemoryModuleType.WALK_TARGET);
            brain.forget(MemoryModuleType.LOOK_TARGET);
        }
    }
}
