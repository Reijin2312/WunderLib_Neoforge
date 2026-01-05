package de.ambertation.wunderlib.ui.vanilla;

import de.ambertation.wunderlib.ui.ColorHelper;
import de.ambertation.wunderlib.ui.layout.components.*;
import de.ambertation.wunderlib.ui.layout.values.Value;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public abstract class LayoutScreen extends Screen {
    protected static final Runnable EMPTY_SCREEN = () -> {
        Minecraft.getInstance().setScreen(null);
    };

    public interface OverlayProvider {
        @NotNull
        Panel getOverlay();

        void willRemoveOverlay();
    }

    protected final int topPadding;
    protected final int bottomPadding;
    protected final int sidePadding;
    protected final int titleSpacing;

    private OverlayProvider overlayProvider = null;
    private Panel currentOverlay = null;

    public LayoutScreen(Component component) {
        this(EMPTY_SCREEN, component);
    }

    public LayoutScreen(@Nullable Screen parent, Component component) {
        this(setScreenOnClose(parent), component);
    }

    public LayoutScreen(
            @Nullable Screen parent,
            Component component,
            int topPadding,
            int bottomPadding,
            int sidePadding
    ) {
        this(setScreenOnClose(parent), component, topPadding, bottomPadding, sidePadding);
    }


    public LayoutScreen(@Nullable Runnable onClose, Component component) {
        this(onClose, component, 20, 10, 20, 15);
    }

    public LayoutScreen(
            @Nullable Runnable onClose,
            Component component,
            int topPadding,
            int bottomPadding,
            int sidePadding
    ) {
        this(onClose, component, topPadding, bottomPadding, sidePadding, 15);
    }

    public LayoutScreen(
            @Nullable Runnable onClose,
            Component component,
            int topPadding,
            int bottomPadding,
            int sidePadding,
            int titleSpacing
    ) {
        super(component);
        this.onClose = onClose;
        this.topPadding = topPadding;
        this.bottomPadding = bottomPadding;
        this.sidePadding = sidePadding;
        this.titleSpacing = titleSpacing;
    }

    @Nullable
    protected Panel main;

    @Nullable
    public final Runnable onClose;

    protected abstract LayoutComponent<?, ?> initContent();

    protected void openLink(String uri) {
        ConfirmLinkScreen cls = new ConfirmLinkScreen(
                bl -> {
                    if (bl) {
                        Util.getPlatform().openUri(uri);
                    }
                    this.minecraft.setScreen(this);
                }, uri, true
        );

        Minecraft.getInstance().setScreen(cls);
    }

    @Override
    protected final void init() {
        super.init();
        main = new Panel(this, this.width, this.height);
        main.setChild(createScreen(initContent()));

        main.calculateLayout();
        addRenderableWidget(main);
    }

    protected LayoutComponent<?, ?> createTitle() {
        var text = new Text(fit(), fit(), title).centerHorizontal()
                                                .setColor(ColorHelper.WHITE)
                                                .setDebugName("title");
        return text;
    }

    protected LayoutComponent<?, ?> createScreen(LayoutComponent<?, ?> content) {
        VerticalStack rows = new VerticalStack(fill(), fill()).setDebugName("title stack");

        if (topPadding > 0) rows.addSpacer(topPadding);
        rows.add(createTitle());
        rows.addSpacer(titleSpacing);
        rows.add(content);
        if (bottomPadding > 0) rows.addSpacer(bottomPadding);

        if (sidePadding <= 0) return rows;

        HorizontalStack cols = new HorizontalStack(fill(), fill()).setDebugName("padded side");
        cols.addSpacer(sidePadding);
        cols.add(rows);
        cols.addSpacer(sidePadding);

        return cols;
    }

    public void renderBackgroundLayer(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
       
    }


    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        renderBackgroundLayer(guiGraphics, i, j, f);
        super.render(guiGraphics, i, j, f);

//        guiGraphics.drawString(font, "HelloHello", 10, 10, ColorHelper.WHITE);
//        guiGraphics.drawManaged(() -> {
//            TooltipRenderUtil.renderTooltipBackground(guiGraphics, 0, 0, 20, 100, 400);
//
//        });
//        guiGraphics.pose().pushPose();
//        guiGraphics.pose().translate(0, 0, 400);
//        guiGraphics.fill(20, 0, 40, 100, ColorHelper.BLUE);
//        guiGraphics.drawString(font, "WorldWorld", 10, 30, ColorHelper.WHITE);
//        guiGraphics.pose().popPose();
////        guiGraphics.fill(0, 0, 20, 100, ColorHelper.RED);

    }

    protected static Runnable setScreenOnClose(Screen screen) {
        if (screen == null) return EMPTY_SCREEN;
        return () -> {
            Minecraft.getInstance().setScreen(screen);
        };
    }

    final protected void closeScreen() {
        onClose();
    }

    @Override
    public void onClose() {
        if (this.onClose != null) {
            this.onClose.run();
        } else {
            Minecraft.getInstance().setScreen(null);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    public static Value fit() {
        return Value.fit();
    }

    public static Value fitOrFill() {
        return Value.fitOrFill();
    }

    public static Value fill() {
        return Value.fill();
    }

    public static Value fixed(int size) {
        return Value.fixed(size);
    }

    public static Value relative(double percentage) {
        return Value.relative(percentage);
    }

    public static MutableComponent translatable(String key) {
        return Component.translatable(key);
    }

    public static MutableComponent literal(String content) {
        return Component.literal(content);
    }


    public void setOverlayProvider(OverlayProvider newOverlay) {
        if (overlayProvider != null) {
            overlayProvider.willRemoveOverlay();
        }
        if (currentOverlay != null) {
            removeWidget(currentOverlay);
            currentOverlay = null;
        }

        overlayProvider = newOverlay;

        if (overlayProvider != null) {
            currentOverlay = overlayProvider.getOverlay();
            addRenderableWidget(currentOverlay);
        }
    }
}

