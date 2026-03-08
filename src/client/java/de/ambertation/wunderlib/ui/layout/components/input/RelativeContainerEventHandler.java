package de.ambertation.wunderlib.ui.layout.components.input;

import de.ambertation.wunderlib.ui.layout.values.Rectangle;

import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.MouseButtonEvent;


import java.util.Optional;

public interface RelativeContainerEventHandler extends ContainerEventHandler {
    Rectangle getInputBounds();

    default Optional<GuiEventListener> getChildAt(double d, double e) {
        return ContainerEventHandler.super.getChildAt(d, e);
    }

    private MouseButtonEvent offsetMouseEvent(MouseButtonEvent event) {
        Rectangle r = getInputBounds();
        return new MouseButtonEvent(event.x() - r.left, event.y() - r.top, event.buttonInfo());
    }

    @Override
    default boolean mouseClicked(MouseButtonEvent event, boolean isInside) {
        if (getFocused() != null) {
            //getFocused().mouseClicked(d, e, i);
        }
        return ContainerEventHandler.super.mouseClicked(offsetMouseEvent(event), isInside);
    }

    @Override
    default boolean mouseReleased(MouseButtonEvent event) {
        return ContainerEventHandler.super.mouseReleased(offsetMouseEvent(event));
    }

    @Override
    default boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        return ContainerEventHandler.super.mouseDragged(offsetMouseEvent(event), dragX, dragY);
    }

    default boolean mouseScrolled(double d, double e, double f, double g) {
        Rectangle r = getInputBounds();
        return ContainerEventHandler.super.mouseScrolled(d - r.left, e - r.top, f, g);
    }

    default boolean isMouseOver(double x, double y) {
        Rectangle r = getInputBounds();
        boolean res = false;
        for (GuiEventListener c : children()) {
            res |= c.isMouseOver(x - r.left, y - r.top);
        }

        return res || r.contains(x, y);
    }
}
