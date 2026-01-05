package de.ambertation.wunderlib.ui.layout.components;

import de.ambertation.wunderlib.ui.layout.components.render.RenderHelper;
import de.ambertation.wunderlib.ui.layout.values.Rectangle;
import de.ambertation.wunderlib.ui.layout.values.Size;
import de.ambertation.wunderlib.ui.layout.values.Value;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Image extends CustomRenderComponent {
    protected Rectangle uvRect;
    public final ResourceLocation location;
    protected float alpha;
    protected Size resourceSize;

    public Image(Value width, Value height, ResourceLocation location) {
        this(width, height, location, new Size(16, 16));
    }

    public Image(Value width, Value height, ResourceLocation location, Size resourceSize) {
        super(width, height);
        this.location = location;
        this.uvRect = new Rectangle(0, 0, resourceSize.width(), resourceSize.height());
        this.resourceSize = resourceSize;
        this.alpha = 1f;
    }


    public Image setAlpha(float a) {
        alpha = a;
        return this;
    }

    public float getAlpha() {
        return alpha;
    }


    public Image setUvRect(Rectangle rect) {
        uvRect = rect;
        return this;
    }

    public Image setUvRect(int left, int top, int width, int height) {
        uvRect = new Rectangle(left, top, width, height);
        return this;
    }

    public Rectangle getUvRect() {
        return uvRect;
    }

    public Image setResourceSize(int width, int height) {
        resourceSize = new Size(width, height);
        return this;
    }

    public Image setResourceSize(Size sz) {
        resourceSize = sz;
        return this;
    }

    public Size getResourceSize() {
        return resourceSize;
    }

    @Override
    public int getContentWidth() {
        return uvRect.width;
    }

    @Override
    public int getContentHeight() {
        return uvRect.height;
    }


    @Override
    protected void customRender(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float deltaTicks,
            Rectangle bounds,
            Rectangle clipRect
    ) {
        RenderHelper.renderImage(guiGraphics, 0, 0, bounds.width, bounds.height, location, resourceSize, uvRect, alpha);
    }

    @Override
    public boolean isMouseOver(double d, double e) {
        return false;
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

