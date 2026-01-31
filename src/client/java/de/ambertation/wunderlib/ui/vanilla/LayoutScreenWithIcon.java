package de.ambertation.wunderlib.ui.vanilla;

import de.ambertation.wunderlib.ui.layout.components.HorizontalStack;
import de.ambertation.wunderlib.ui.layout.components.LayoutComponent;
import de.ambertation.wunderlib.ui.layout.values.Size;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import org.jetbrains.annotations.Nullable;

public abstract class LayoutScreenWithIcon extends LayoutScreen {
    protected final Identifier icon;

    public LayoutScreenWithIcon(Identifier icon, Component component) {
        this(EMPTY_SCREEN, icon, component);
    }

    public LayoutScreenWithIcon(
            @Nullable Screen parent,
            Identifier icon,
            Component component
    ) {
        this(setScreenOnClose(parent), icon, component);
    }

    public LayoutScreenWithIcon(
            @Nullable Screen parent,
            Identifier icon,
            Component component,
            int topPadding,
            int bottomPadding,
            int sidePadding
    ) {
        this(setScreenOnClose(parent), icon, component, topPadding, bottomPadding, sidePadding);
    }

    public LayoutScreenWithIcon(
            @Nullable Runnable onClose,
            Identifier icon,
            Component component
    ) {
        this(onClose, icon, component, 20, 10, 20, 15);
    }

    public LayoutScreenWithIcon(
            @Nullable Runnable onClose,
            Identifier icon,
            Component component,
            int topPadding,
            int bottomPadding,
            int sidePadding
    ) {
        this(onClose, icon, component, topPadding, bottomPadding, sidePadding, 15);
    }

    public LayoutScreenWithIcon(
            @Nullable Runnable onClose,
            Identifier icon,
            Component component,
            int topPadding,
            int bottomPadding,
            int sidePadding,
            int titleSpacing
    ) {
        super(onClose, component, topPadding, bottomPadding, sidePadding, titleSpacing);
        this.icon = icon;
    }

    @Override
    protected LayoutComponent<?, ?> createTitle() {
        LayoutComponent<?, ?> title = super.createTitle();
        HorizontalStack row = new HorizontalStack(fill(), fit()).setDebugName("title bar");
        row.addFiller();
        row.addIcon(icon, Size.of(512)).setDebugName("icon");
        row.addSpacer(4);
        row.add(title);
        row.addFiller();
        return row;
    }
}

