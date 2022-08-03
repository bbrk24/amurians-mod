package org.bbrk24.amurians.mixins;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

@Mixin(AxeItem.class)
public abstract class BarkStrippingMixin {
    private final Map<Block, Identifier> lootTableIDs = new HashMap<>();

    private Identifier getLootTableID(Block block) {
        if (lootTableIDs.containsKey(block)) {
            return lootTableIDs.get(block);
        }

        Identifier blockID = Registry.BLOCK.getId(block);
        Identifier lootTableID = new Identifier(
            blockID.getNamespace(),
            "blocks/stripped/" + blockID.getPath()
        );

        lootTableIDs.put(block, lootTableID);
        return lootTableID;
    }

    @Inject(
        method = "useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V",
            ordinal = 0,
            shift = At.Shift.AFTER
        )
    )
    private void spawnBarkItem(ItemUsageContext ctx, CallbackInfoReturnable<ActionResult> info) {
        World world = ctx.getWorld();
        if (world.isClient) {
            return;
        }

        BlockPos position = ctx.getBlockPos();
        PlayerEntity player = ctx.getPlayer();
        BlockState state = world.getBlockState(position);
        ServerWorld serverWorld = (ServerWorld)world;
        Identifier lootTableID = getLootTableID(state.getBlock());

        LootContext.Builder lootCtxBuilder = new LootContext.Builder(serverWorld)
            .random(world.random)
            .parameter(LootContextParameters.BLOCK_STATE, state)
            .parameter(LootContextParameters.ORIGIN, Vec3d.ofCenter(position))
            .parameter(LootContextParameters.TOOL, ctx.getStack())
            .optionalParameter(LootContextParameters.THIS_ENTITY, player);

        if (player != null) {
            lootCtxBuilder.luck(player.getLuck());
        }

        LootContext lootCtx = lootCtxBuilder.build(LootContextTypes.BLOCK);

        List<ItemStack> drops = serverWorld.getServer()
            .getLootManager()
            .getTable(lootTableID)
            .generateLoot(lootCtx);

        for (ItemStack stack : drops) {
            Block.dropStack(world, position, stack);
        }
    }
}
