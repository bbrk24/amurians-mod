package org.bbrk24.amurians.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.ai.brain.task.LayFrogSpawnTask;
import net.minecraft.state.property.Properties;

@Mixin(LayFrogSpawnTask.class)
public class LayFrogSpawnTaskMixin {
    @Redirect(
        method = "run(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/passive/FrogEntity;J)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"
        )
    )
    private boolean checkIfValidSpawn(BlockState self, Block water) {
        Block block = self.getBlock();
        if (self.isOf(Blocks.WATER)) {
            FluidBlock fluid = (FluidBlock)block;
            return fluid.getFluidState(self).isStill();
        } else if (block instanceof Waterloggable) {
            return self.get(Properties.WATERLOGGED);
        } else {
            return false;
        }
    }
}
