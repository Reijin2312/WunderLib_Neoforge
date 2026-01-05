package de.ambertation.wunderlib.ui.layout.components.render;


import de.ambertation.wunderlib.ui.layout.values.Rectangle;

import net.minecraft.client.gui.GuiGraphics;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ScrollerRenderer {
    default int scrollerHeight() {
        return 16;
    }
    default int scrollerWidth() {
        return 8;
    }

    default int scrollerPadding() {
        return 2;
    }

    default Rectangle getScrollerBounds(Rectangle renderBounds) {
        // relative top the current bounds
        return new Rectangle(
                renderBounds.right() - this.scrollerWidth() - renderBounds.left,
                0,
                this.scrollerWidth(),
                renderBounds.height
        );
    }

    default Rectangle getPickerBounds(Rectangle renderBounds, int pickerOffset, int pickerSize) {
        return new Rectangle(
                renderBounds.left,
                renderBounds.top + pickerOffset,
                renderBounds.width,
                pickerSize
        );
    }

    void renderScrollBar(
            GuiGraphics guiGraphics,
            Rectangle renderBounds,
            int pickerOffset,
            int pickerSize,
            float zIndex
    );
}

