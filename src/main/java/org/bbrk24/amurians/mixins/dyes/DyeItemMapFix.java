package org.bbrk24.amurians.mixins.dyes;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.item.DyeItem;
import net.minecraft.item.Items;
import net.minecraft.util.DyeColor;

@Mixin(DyeItem.class)
public abstract class DyeItemMapFix {
    /**
     * Determine the dye item associated with the given color.
     * @reason In vanilla, {@code DyeItem} uses an enum map to associate dye items with colors.
     * However, this breaks down when subclassing it, as the map is filled in the constructor.
     * Therefore, the map must be circumvented in some way.
     * @throws AssertionError if {@code color} is not one of the 16 found in vanilla.
     * @author bbrk24
     */
    @Overwrite
    public static DyeItem byColor(DyeColor color) {
        switch (color) {
        case BLACK:
            return (DyeItem)Items.BLACK_DYE;
        case BLUE:
            return (DyeItem)Items.BLUE_DYE;
        case BROWN:
            return (DyeItem)Items.BROWN_DYE;
        case CYAN:
            return (DyeItem)Items.CYAN_DYE;
        case GRAY:
            return (DyeItem)Items.GRAY_DYE;
        case GREEN:
            return (DyeItem)Items.GREEN_DYE;
        case LIGHT_BLUE:
            return (DyeItem)Items.LIGHT_BLUE_DYE;
        case LIGHT_GRAY:
            return (DyeItem)Items.LIGHT_GRAY_DYE;
        case LIME:
            return (DyeItem)Items.LIME_DYE;
        case MAGENTA:
            return (DyeItem)Items.MAGENTA_DYE;
        case ORANGE:
            return (DyeItem)Items.ORANGE_DYE;
        case PINK:
            return (DyeItem)Items.PINK_DYE;
        case PURPLE:
            return (DyeItem)Items.PURPLE_DYE;
        case RED:
            return (DyeItem)Items.RED_DYE;
        case WHITE:
            return (DyeItem)Items.WHITE_DYE;
        case YELLOW:
            return (DyeItem)Items.YELLOW_DYE;
        default:
            throw new AssertionError("Someone messed with DyeColor");
        }
    }
}
