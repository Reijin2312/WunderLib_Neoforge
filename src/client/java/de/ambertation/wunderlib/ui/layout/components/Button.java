package de.ambertation.wunderlib.ui.layout.components;

import de.ambertation.wunderlib.ui.layout.components.render.ButtonRenderer;
import de.ambertation.wunderlib.ui.layout.values.Value;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Button extends AbstractVanillaComponent<net.minecraft.client.gui.components.Button, Button> {
    public static final OnTooltip NO_TOOLTIP = (button, guiGraphics, i, j) -> {
    };
    public static final OnPress NO_ACTION = (button) -> {
    };

    @OnlyIn(Dist.CLIENT)
    public interface OnTooltip {
        void onTooltip(Button button, GuiGraphics guiGraphics, int mouseX, int mouseY);
    }

    @OnlyIn(Dist.CLIENT)
    public interface OnPress {
        void onPress(Button button);
    }

    OnPress onPress;
    Component onTooltip;

    boolean glow = false;

    public Button(
            Value width,
            Value height,
            Component component
    ) {
        super(width, height, new ButtonRenderer(), component);
        this.onPress = NO_ACTION;
        this.onTooltip = null;
    }

    public Button onPress(OnPress onPress) {
        this.onPress = onPress;
        return this;
    }

    public Button onToolTip(Component message) {
        this.onTooltip = onTooltip;
        return this;
    }

    @Override
    protected net.minecraft.client.gui.components.Button createVanillaComponent() {
        Button self = this;
        var builder = net.minecraft.client.gui.components.Button
                .builder(contentComponent(), (bt) -> onPress.onPress(self))
                .bounds(0, 0, relativeBounds.width, relativeBounds.height);
        if (onTooltip != null) {
            builder.tooltip(Tooltip.create(onTooltip));
        }
        return builder.build();
    }

    public boolean isGlowing() {
        return glow;
    }

    private boolean focused;

    @Override
    public boolean isFocused() {
        return focused;
    }

    @Override
    public void setFocused(boolean bl) {
        focused = bl;
    }
}

