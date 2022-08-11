package org.bbrk24.amurians.mixins;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;

import java.util.stream.Stream;

import net.minecraft.util.SignType;

import org.bbrk24.amurians.AzaleaSignType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(SignType.class)
public abstract class AzaleaSignTypeProvider {
    private static final ObjectArraySet<SignType> SIGN_TYPES = new ObjectArraySet<>(new SignType[] {
        SignType.OAK,
        SignType.SPRUCE,
        SignType.BIRCH,
        SignType.ACACIA,
        SignType.JUNGLE,
        SignType.DARK_OAK,
        SignType.CRIMSON,
        SignType.WARPED,
        SignType.MANGROVE,
        AzaleaSignType.INSTANCE
    });

    /**
     * A stream containing all sign types
     * @author bbrk24
     * @reason {@link SignType#register()} is private and I can't be bothered to figure out how to
     * get around that.
     */
    @Overwrite
    public static Stream<SignType> stream() {
        return SIGN_TYPES.stream();
    }
}
