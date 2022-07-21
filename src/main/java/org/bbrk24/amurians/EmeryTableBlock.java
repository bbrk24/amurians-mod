package org.bbrk24.amurians;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EmeryTableBlock extends HorizontalFacingBlock {
    private static final Text TITLE = Text.translatable("container.emery_table");

    public EmeryTableBlock() {
        super(
            FabricBlockSettings.of(Material.STONE)
                .strength(3.5f)
                .requiresTool()
        );
    }
    
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(
            Properties.HORIZONTAL_FACING,
            ctx.getPlayerFacing().getOpposite()
        );
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(Properties.HORIZONTAL_FACING);
    }

    @Override
    public ActionResult onUse(
        BlockState state,
        World world,
        BlockPos pos,
        PlayerEntity player,
        Hand hand,
        BlockHitResult hit
    ) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }
        player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
        player.incrementStat(Stats.INTERACT_WITH_STONECUTTER);
        return ActionResult.CONSUME;
    }

    @Override
    @Nullable
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        return new SimpleNamedScreenHandlerFactory(
            (syncId, playerInventory, player) ->
                new EmeryTableScreenHandler(
                    syncId,
                    playerInventory,
                    ScreenHandlerContext.create(world, pos)
                ),
            TITLE
        );
    }
}
