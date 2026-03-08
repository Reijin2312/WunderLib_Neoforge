package de.ambertation.wunderlib.ui.layout.components.render;

import de.ambertation.wunderlib.ui.ColorHelper;
import de.ambertation.wunderlib.ui.layout.components.AbstractVanillaComponentRenderer;
import de.ambertation.wunderlib.ui.layout.components.Button;
import de.ambertation.wunderlib.ui.layout.values.Rectangle;

import net.minecraft.client.gui.GuiGraphics;


public class ButtonRenderer extends AbstractVanillaComponentRenderer<net.minecraft.client.gui.components.Button, Button> {
    double deltaSum = 0;
    double deltaSum2 = .34;
    double deltaSum3 = .12;

    @Override
    public void renderInBounds(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float deltaTicks,
            Rectangle bounds,
            Rectangle clipRect
    ) {
        super.renderInBounds(guiGraphics, mouseX, mouseY, deltaTicks, bounds, clipRect);
        deltaSum += deltaTicks * 0.03;
        deltaSum2 += deltaTicks * 0.032;
        deltaSum3 += deltaTicks * 0.028;
        if (getLinkedComponent() != null && getLinkedComponent().isGlowing()) {
            RenderHelper.outline(guiGraphics, 0, 0, bounds.width, bounds.height, ColorHelper.YELLOW);
            int len = 2 * bounds.width + 2 * bounds.height;

            deltaSum = deltaSum - (int) deltaSum;
            int pos = (int) (len * deltaSum);

            drawMoving(guiGraphics, bounds, pos);
            drawMoving(guiGraphics, bounds, pos + 2);
            drawMoving(guiGraphics, bounds, pos + 3);
            drawMoving(guiGraphics, bounds, pos + 4);
            drawMoving(guiGraphics, bounds, pos + 5);
            drawMoving(guiGraphics, bounds, pos + 7);


            deltaSum2 = deltaSum2 - (int) deltaSum2;
            pos = (int) (len * deltaSum2);

            drawMoving(guiGraphics, bounds, pos + len / 3);
            drawMoving(guiGraphics, bounds, pos + 2 + len / 3);
            drawMoving(guiGraphics, bounds, pos + 3 + len / 3);
            drawMoving(guiGraphics, bounds, pos + 4 + len / 3);
            drawMoving(guiGraphics, bounds, pos + 5 + len / 3);
            drawMoving(guiGraphics, bounds, pos + 7 + len / 3);


            deltaSum3 = deltaSum3 - (int) deltaSum3;
            pos = (int) (len * deltaSum3);

            drawMoving(guiGraphics, bounds, pos + 2 * len / 3);
            drawMoving(guiGraphics, bounds, pos + 2 + 2 * len / 3);
            drawMoving(guiGraphics, bounds, pos + 3 + 2 * len / 3);
            drawMoving(guiGraphics, bounds, pos + 4 + 2 * len / 3);
            drawMoving(guiGraphics, bounds, pos + 5 + 2 * len / 3);
            drawMoving(guiGraphics, bounds, pos + 7 + 2 * len / 3);
        }
    }

    private void drawMoving(GuiGraphics guiGraphics, Rectangle bounds, int pos) {
        int bh = bounds.width + bounds.height;
        pos = pos % (2 * bh);
        int x, y;
        /*
         * pos <= w              : x=pos, y=0
         * pos > w && pos<=w+h   : x=w, y=pos-w
         * pos >w+h && pos<=2w+h : x=2w +h - pos, y=h
         * pos>2w+h              : x=0, y=2w+2h-pos
         */
        if (pos <= bounds.width) {
            x = pos;
            y = 0;
        } else if (pos <= bh) {
            x = bounds.width - 1;
            y = pos - bounds.width;
        } else if (pos <= bh + bounds.width) {
            x = bh + bounds.width - pos;
            y = bounds.height - 1;
        } else {
            x = 0;
            y = 2 * bh - pos;
        }
        guiGraphics.fill(x, y, x + 1, y + 1, ColorHelper.WHITE);
    }
}
