package de.ambertation.wunderlib.ui.layout.components;

import de.ambertation.wunderlib.ui.layout.components.render.ComponentRenderer;
import de.ambertation.wunderlib.ui.layout.components.render.TextProvider;
import de.ambertation.wunderlib.ui.layout.values.Rectangle;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;


public class AbstractVanillaComponentRenderer<C extends AbstractWidget, V extends AbstractVanillaComponent<C, V>> implements ComponentRenderer, TextProvider {
    V linkedComponent;

    protected V getLinkedComponent() {
        return linkedComponent;
    }

    @Override
    public void renderInBounds(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float deltaTicks,
            Rectangle bounds,
            Rectangle clipRect
    ) {
        if (linkedComponent != null) {
            if (linkedComponent.vanillaComponent != null) {
                if (!linkedComponent.enabled) {
                    linkedComponent.vanillaComponent.setAlpha(linkedComponent.alpha / 2);
                }
                linkedComponent.vanillaComponent.render(guiGraphics, mouseX, mouseY, deltaTicks);
                if (!linkedComponent.enabled) {
                    linkedComponent.vanillaComponent.setAlpha(linkedComponent.alpha);
                }
            }

        }
    }
}
