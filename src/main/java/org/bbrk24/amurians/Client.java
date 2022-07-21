package org.bbrk24.amurians;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.VillagerResemblingModel;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class Client implements ClientModInitializer {
    public static final EntityModelLayer AMURIAN_MODEL_LAYER = new EntityModelLayer(
        new Identifier("amurians", "amurian"), 
        "main"
    );

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(Initializer.AMURIAN, AmurianRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(
            AMURIAN_MODEL_LAYER,
            () -> TexturedModelData.of(VillagerResemblingModel.getModelData(), 64, 64)
        );

        HandledScreens.register(Initializer.EMERY_TABLE_SCREEN_HANDLER_TYPE, EmeryTableScreen::new);
    }
}
