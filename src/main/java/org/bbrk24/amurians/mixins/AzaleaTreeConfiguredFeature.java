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

package org.bbrk24.amurians.mixins;

import net.minecraft.block.Block;
import net.minecraft.world.gen.feature.TreeConfiguredFeatures;

import org.bbrk24.amurians.Initializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(TreeConfiguredFeatures.class)
public abstract class AzaleaTreeConfiguredFeature {
    @Redirect(
        method = "<clinit>",
        at = @At(
            target = "Lnet/minecraft/block/Blocks;OAK_LOG:Lnet/minecraft/block/Block;",
            value = "FIELD",
            ordinal = 0
        ),
        slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=azalea_tree"))
    )
    private static Block getAzaleaTreeOakLog() {
        return Initializer.getAzaleaLog();
    }
}
