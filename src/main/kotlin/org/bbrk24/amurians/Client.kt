/*
 * Copyright 2022 William Baker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bbrk24.amurians

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.minecraft.client.gui.screen.ingame.HandledScreens
import net.minecraft.client.model.TexturedModelData
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.entity.model.EntityModelLayer
import net.minecraft.client.render.entity.model.VillagerResemblingModel
import net.minecraft.util.Identifier

import org.bbrk24.amurians.amurian.AmurianRenderer
import org.bbrk24.amurians.emerytable.EmeryTableScreen

private val AMURIAN_MODEL_LAYER = EntityModelLayer(Identifier("amurians", "amurian"), "main")

@Environment(EnvType.CLIENT)
class Client : ClientModInitializer {
    override fun onInitializeClient() {
        EntityRendererRegistry.register(Initializer.AMURIAN, ::AmurianRenderer)
        EntityModelLayerRegistry.registerModelLayer(
            AMURIAN_MODEL_LAYER,
            { TexturedModelData.of(VillagerResemblingModel.getModelData(), 64, 64) }
        )

        HandledScreens.register(Initializer.EMERY_TABLE_SCREEN_HANDLER_TYPE, ::EmeryTableScreen)

        BlockRenderLayerMap.INSTANCE.putBlock(Initializer.HISHAI_PLANT, RenderLayer.getCutout())
        BlockRenderLayerMap.INSTANCE.putBlock(Initializer.HISHAI_TOP, RenderLayer.getCutout())
    }
}
