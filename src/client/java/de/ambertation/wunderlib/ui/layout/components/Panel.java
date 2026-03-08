package de.ambertation.wunderlib.ui.layout.components;

import de.ambertation.wunderlib.ui.layout.components.input.RelativeContainerEventHandler;
import de.ambertation.wunderlib.ui.layout.values.Rectangle;
import de.ambertation.wunderlib.ui.vanilla.LayoutScreen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;


import java.util.List;
import org.jetbrains.annotations.Nullable;

public class Panel implements ComponentWithBounds, RelativeContainerEventHandler, NarratableEntry, Renderable {
    protected LayoutComponent<?, ?> child;
    List<? extends GuiEventListener> listeners = List.of();
    public final Rectangle bounds;
    public final LayoutScreen parentScreen;

    private float zIndex = 0;
    private boolean inputEnabled = true;

    public Panel(LayoutScreen parentScreen) {
        this(parentScreen, 0, 0, parentScreen.width, parentScreen.height);
    }

    public Panel(LayoutScreen parentScreen, int width, int height) {
        this(parentScreen, 0, 0, width, height);
    }

    public Panel(LayoutScreen parentScreen, int left, int top, int width, int height) {
        this(parentScreen, new Rectangle(left, top, width, height));
    }

    public Panel(LayoutScreen parentScreen, Rectangle bounds) {
        this.parentScreen = parentScreen;
        this.bounds = bounds;
    }

    public Panel setZIndex(float zIndex) {
        this.zIndex = zIndex;
        return this;
    }

    public float getZIndex() {
        return zIndex;
    }

    public Panel setChild(LayoutComponent<?, ?> c) {
        this.child = c;
        listeners = List.of(c);
        return this;
    }

    public void calculateLayout() {
        if (child != null) {
            child.calculateLayoutInParent(this);
        }
    }

    @Override
    public Rectangle getRelativeBounds() {
        return bounds;
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return listeners;
    }

    @Override
    public Rectangle getInputBounds() {
        return bounds;
    }

    boolean dragging = false;

    @Override
    public boolean isDragging() {
        return dragging;
    }

    @Override
    public void setDragging(boolean bl) {
        dragging = bl;
    }

    GuiEventListener focused;

    @Nullable
    @Override
    public GuiEventListener getFocused() {
        return focused;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener guiEventListener) {
        focused = guiEventListener;
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float deltaTicks) {
        if (child != null) {
            guiGraphics.pose().pushMatrix();

            // For 2D UI rendering, we only need x,y translation
            guiGraphics.pose().translate(bounds.left, bounds.top);

            child.render(
                    guiGraphics,
                    inputEnabled ? mouseX - bounds.left : -1000,
                    mouseY - bounds.top,
                    deltaTicks,
                    bounds,
                    bounds
            );

            guiGraphics.pose().popMatrix();
        }
    }

    public Panel setInputEnabled(boolean inputEnabled) {
        this.inputEnabled = inputEnabled;
        return this;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isInside) {
        if (inputEnabled)
            return RelativeContainerEventHandler.super.mouseClicked(event, isInside);

        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (inputEnabled)
            return RelativeContainerEventHandler.super.mouseDragged(event, dragX, dragY);
        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (inputEnabled)
            return RelativeContainerEventHandler.super.mouseReleased(event);
        return false;
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f, double g) {
        if (inputEnabled)
            return RelativeContainerEventHandler.super.mouseScrolled(d, e, f, g);
        return false;
    }

    @Override
    public void mouseMoved(double d, double e) {
        if (inputEnabled)
            RelativeContainerEventHandler.super.mouseMoved(d, e);
    }

    @Override
    public boolean isMouseOver(double x, double y) {
        if (inputEnabled)
            return RelativeContainerEventHandler.super.isMouseOver(x, y);

        return false;
    }
}
