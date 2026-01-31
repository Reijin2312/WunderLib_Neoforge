package de.ambertation.wunderlib.ui.layout.components;

import de.ambertation.wunderlib.ui.layout.values.Value;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

public abstract class AbstractVanillaComponent<C extends AbstractWidget, V extends AbstractVanillaComponent<C, V>> extends LayoutComponent<AbstractVanillaComponentRenderer<C, V>, V> {
    protected C vanillaComponent;
    protected final net.minecraft.network.chat.Component component;
    protected float alpha = 1.0f;
    protected boolean enabled = true;

    public AbstractVanillaComponent(
            Value width,
            Value height,
            AbstractVanillaComponentRenderer<C, V> renderer,
            net.minecraft.network.chat.Component component
    ) {
        super(width, height, renderer);
        this.component = component;
        renderer.linkedComponent = (V) this;
    }

    protected abstract C createVanillaComponent();

    @Override
    protected void onBoundsChanged() {
        vanillaComponent = createVanillaComponent();
        vanillaComponent.setAlpha(this.alpha);
    }

    protected net.minecraft.network.chat.Component contentComponent() {
        return component;
    }

    @Override
    public int getContentWidth() {
        return renderer.getWidth(contentComponent());
    }

    @Override
    public int getContentHeight() {
        return renderer.getHeight(contentComponent());
    }

    public float getAlpha() {
        return alpha;
    }

    public V setAlpha(float alpha) {
        this.alpha = alpha;
        if (vanillaComponent != null) {
            vanillaComponent.setAlpha(alpha);
        }
        return (V) this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public V setEnabled(boolean enabled) {
        this.enabled = enabled;
        return (V) this;
    }

    @Override
    public void mouseMoved(double x, double y) {
        if (vanillaComponent != null && enabled)
            vanillaComponent.mouseMoved(x - relativeBounds.left, y - relativeBounds.top);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isInside) {
        if (vanillaComponent != null && enabled) {
            MouseButtonEvent localEvent = new MouseButtonEvent(
                    event.x() - relativeBounds.left,
                    event.y() - relativeBounds.top,
                    event.buttonInfo()
            );
            return vanillaComponent.mouseClicked(localEvent, isInside);
        }
        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (vanillaComponent != null && enabled) {
            MouseButtonEvent localEvent = new MouseButtonEvent(
                    event.x() - relativeBounds.left,
                    event.y() - relativeBounds.top,
                    event.buttonInfo()
            );
            return vanillaComponent.mouseReleased(localEvent);
        }
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (vanillaComponent != null && enabled) {
            MouseButtonEvent localEvent = new MouseButtonEvent(
                    event.x() - relativeBounds.left,
                    event.y() - relativeBounds.top,
                    event.buttonInfo()
            );
            return vanillaComponent.mouseDragged(localEvent, dragX, dragY);
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double f, double g) {
        if (vanillaComponent != null && enabled)
            return vanillaComponent.mouseScrolled(x - relativeBounds.left, y - relativeBounds.top, f, g);
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (vanillaComponent != null && enabled)
            return vanillaComponent.keyPressed(event);
        return false;
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        if (vanillaComponent != null && enabled)
            return vanillaComponent.keyReleased(event);
        return false;
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (vanillaComponent != null && enabled)
            return vanillaComponent.charTyped(event);
        return false;
    }


    @Override
    public boolean isFocused() {
        if (vanillaComponent != null) return vanillaComponent.isFocused();
        return false;
    }

    @Override
    public void setFocused(boolean bl) {
        if (vanillaComponent != null) vanillaComponent.setFocused(bl);
    }

    @Override
    public boolean isMouseOver(double x, double y) {
        if (vanillaComponent != null && enabled)
            return vanillaComponent.isMouseOver(x - relativeBounds.left, y - relativeBounds.top);
        return false;
    }

}

