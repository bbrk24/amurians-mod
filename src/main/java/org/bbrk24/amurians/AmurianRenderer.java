package org.bbrk24.amurians;

import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.VillagerResemblingModel;
import net.minecraft.util.Identifier;

public class AmurianRenderer extends MobEntityRenderer<AmurianEntity, VillagerResemblingModel<AmurianEntity>> {
    public AmurianRenderer(Context context) {
        super(
            context,
            new VillagerResemblingModel<>(context.getPart(EntityModelLayers.WANDERING_TRADER)),
            0.5f
        );
    }

    @Override
    public Identifier getTexture(AmurianEntity var1) {
        return new Identifier("textures/entity/wandering_trader.png");
    }
}
