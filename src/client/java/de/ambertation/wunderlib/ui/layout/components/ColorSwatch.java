package de.ambertation.wunderlib.ui.layout.components;

import de.ambertation.wunderlib.ui.ColorHelper;
import de.ambertation.wunderlib.ui.layout.components.render.RenderHelper;
import de.ambertation.wunderlib.ui.layout.values.Rectangle;
import de.ambertation.wunderlib.ui.layout.values.Value;

import net.minecraft.client.gui.GuiGraphics;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ColorSwatch extends CustomRenderComponent<ColorSwatch> {
    private int color;
    private int borderColor = ColorHelper.BLACK;
    private boolean offsetInner = false;

    public ColorSwatch(Value width, Value height, int color) {
        super(width, height);
        this.color = color;
    }

    @Override
    protected void customRender(
            GuiGraphics guiGraphics,
            int x,
            int y,
            float deltaTicks,
            Rectangle bounds,
            Rectangle clipRect
    ) {
        int o = offsetInner ? 2 : 1;
        RenderHelper.outline(guiGraphics, 0, 0, bounds.width, bounds.height, borderColor);
        guiGraphics.fill(o, o, bounds.width - o, bounds.height - o, color);
    }

    public int getColor() {
        return color;
    }

    public ColorSwatch setColor(int color) {
        this.color = color;
        return this;
    }

    public int getBorderColor() {
        return borderColor;
    }

    public ColorSwatch setBorderColor(int color) {
        this.borderColor = color;
        return this;
    }

    public boolean getOffsetInner() {
        return offsetInner;
    }

    public ColorSwatch setOffsetInner(boolean val) {
        this.offsetInner = val;
        return this;
    }

    @Override
    public int getContentWidth() {
        return 20;
    }

    @Override
    public int getContentHeight() {
        return 20;
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

