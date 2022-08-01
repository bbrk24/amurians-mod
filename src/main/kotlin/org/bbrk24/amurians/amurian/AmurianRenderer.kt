package org.bbrk24.amurians.amurian

import net.minecraft.client.render.entity.MobEntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory.Context
import net.minecraft.client.render.entity.model.EntityModelLayers
import net.minecraft.client.render.entity.model.VillagerResemblingModel
import net.minecraft.util.Identifier

class AmurianRenderer(context: Context) : MobEntityRenderer<
    AmurianEntity,
    VillagerResemblingModel<AmurianEntity>
>(context, VillagerResemblingModel(context.getPart(EntityModelLayers.WANDERING_TRADER)), 0.5f) {
    override fun getTexture(unused: AmurianEntity): Identifier {
        return Identifier("minecraft", "textures/entity/wandering_trader.png")
    }
}
