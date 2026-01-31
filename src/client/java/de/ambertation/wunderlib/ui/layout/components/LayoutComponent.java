package de.ambertation.wunderlib.ui.layout.components;

import de.ambertation.wunderlib.ui.layout.components.render.ComponentRenderer;
import de.ambertation.wunderlib.ui.layout.values.Alignment;
import de.ambertation.wunderlib.ui.layout.values.Rectangle;
import de.ambertation.wunderlib.ui.layout.values.Value;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;


@OnlyIn(Dist.CLIENT)
public abstract class LayoutComponent<R extends ComponentRenderer, L extends LayoutComponent<R, L>> implements ComponentWithBounds, GuiEventListener {
    protected final R renderer;
    protected final Value width;
    protected final Value height;
    protected String debugName;
    protected Rectangle relativeBounds;
    protected Rectangle screenBounds;
    protected Alignment vAlign = Alignment.MIN;
    protected Alignment hAlign = Alignment.MIN;

    protected Panel parentPanel;

    public LayoutComponent(Value width, Value height, R renderer) {
        this.width = width.attachComponent(this::getContentWidth);
        this.height = height.attachComponent(this::getContentHeight);
        this.renderer = renderer;
    }

    protected float getZIndex() {
        return parentPanel == null ? 0 : parentPanel.getZIndex();
    }

    protected int updateContainerWidth(int containerWidth) {
        return width.setCalculatedSize(containerWidth);
    }

    protected int updateContainerHeight(int containerHeight) {
        return height.setCalculatedSize(containerHeight);
    }

    void setRelativeBounds(int left, int top) {
        relativeBounds = new Rectangle(left, top, width.calculatedSize(), height.calculatedSize());
        onBoundsChanged();
    }

    public void updateScreenBounds(Panel parentpanel, int worldX, int worldY) {
        this.parentPanel = parentpanel;
        screenBounds = relativeBounds.movedBy(worldX, worldY);
    }

    protected void onBoundsChanged() {
    }

    public Rectangle getRelativeBounds() {
        return relativeBounds;
    }

    public Rectangle getScreenBounds() {
        return screenBounds;
    }

    public abstract int getContentWidth();
    public abstract int getContentHeight();

    public int fillWidth(int parentSize, int fillSize) {
        return width.fill(fillSize);
    }

    public int fillHeight(int parentSize, int fillSize) {
        return height.fill(fillSize);
    }

    public int getWidth() {
        return width.calculatedSize();
    }

    public int getHeight() {
        return height.calculatedSize();
    }

    /**
     * Set clipping rectangle using the new GuiGraphics scissor system
     */
    protected final void setClippingRect(GuiGraphics guiGraphics, Rectangle clippingRect) {

        if (clippingRect == null) {
            guiGraphics.disableScissor();
            return;
        }

//        guiGraphics.renderOutline(
//                0,
//                0,
//                renderBounds.width,
//                renderBounds.height,
//                0xFF00FF00
//        );

        guiGraphics.enableScissor(
                clippingRect.left,
                clippingRect.top,
                clippingRect.right(),
                clippingRect.bottom()
        );
    }

    public void render(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float deltaTicks,
            Rectangle parentBounds,
            Rectangle clipRect
    ) {
        Rectangle r = relativeBounds.movedBy(parentBounds.left, parentBounds.top);
        Rectangle clip = r.intersect(clipRect);

        // The new Matrix System has a max stack depth of 15,
        // We need to use it sparingly!
        //guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(relativeBounds.left, relativeBounds.top);

        //if (r.overlaps(clip))
        {
            renderInBounds(guiGraphics, mouseX - relativeBounds.left, mouseY - relativeBounds.top, deltaTicks, r, clip);
        }

        guiGraphics.pose().translate(-relativeBounds.left, -relativeBounds.top);
        //guiGraphics.pose().popMatrix();
    }

    protected void renderInBounds(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float deltaTicks,
            Rectangle renderBounds,
            Rectangle clipRect
    ) {
        if (renderer != null) {
            setClippingRect(guiGraphics, clipRect);
            renderer.renderInBounds(guiGraphics, mouseX, mouseY, deltaTicks, renderBounds, clipRect);
            setClippingRect(guiGraphics, null);
        }
    }

    @Override
    public String toString() {
        if (debugName == null) {
            return super.toString() + "(" +
                    relativeBounds + ", " +
                    width.calculatedSize() + "x" + height.calculatedSize() +
                    ")";
        } else {
            return debugName + "(" +
                    relativeBounds + ", " +
                    width.calculatedSize() + "x" + height.calculatedSize() +
                    ")";
        }
    }

    public L alignTop() {
        vAlign = Alignment.MIN;
        return (L) this;
    }

    public L alignBottom() {
        vAlign = Alignment.MAX;
        return (L) this;
    }

    public L centerVertical() {
        vAlign = Alignment.CENTER;
        return (L) this;
    }

    public L alignLeft() {
        hAlign = Alignment.MIN;
        return (L) this;
    }

    public L alignRight() {
        hAlign = Alignment.MAX;
        return (L) this;
    }

    public L centerHorizontal() {
        hAlign = Alignment.CENTER;
        return (L) this;
    }

    public L setDebugName(String d) {
        debugName = d;
        return (L) this;
    }

    @Override
    public boolean isMouseOver(double d, double e) {
        return relativeBounds.contains(d, e);
    }

    public void reCalculateLayout() {
        updateContainerWidth(relativeBounds.width);
        updateContainerHeight(relativeBounds.height);
        setRelativeBounds(relativeBounds.left, relativeBounds.top);
        updateScreenBounds(this.parentPanel, screenBounds.left, screenBounds.top);
    }

    protected void calculateLayoutInParent(Panel parentPanel) {
        this.updateContainerWidth(parentPanel.bounds.width);
        this.updateContainerHeight(parentPanel.bounds.height);
        this.setRelativeBounds(0, 0);
        this.updateScreenBounds(parentPanel, parentPanel.bounds.left, parentPanel.bounds.top);
    }

    public void recalculateLayout() {
        if (parentPanel != null)
            parentPanel.calculateLayout();
    }
}
