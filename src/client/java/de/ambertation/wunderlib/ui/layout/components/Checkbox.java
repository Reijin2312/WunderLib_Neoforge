package de.ambertation.wunderlib.ui.layout.components;

import de.ambertation.wunderlib.ui.layout.components.render.CheckboxRenderer;
import de.ambertation.wunderlib.ui.layout.values.Value;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Checkbox extends AbstractVanillaComponent<net.minecraft.client.gui.components.Checkbox, Checkbox> {
    public static SelectionChanged IGNORE_CHANGE = (a, b) -> {
    };

    @FunctionalInterface
    public interface SelectionChanged {
        void now(Checkbox checkBox, boolean selected);
    }

    private final boolean selected;
    private final boolean showLabel;

    private SelectionChanged onSelectionChange;

    public Checkbox(
            Value width,
            Value height,
            Component component,
            boolean selected, boolean showLabel
    ) {
        super(width, height, new CheckboxRenderer(), component);
        onSelectionChange = IGNORE_CHANGE;
        this.selected = selected;
        this.showLabel = showLabel;
    }

    public Checkbox onChange(SelectionChanged onSelectionChange) {
        this.onSelectionChange = onSelectionChange;
        return this;
    }

    public boolean isChecked() {
        if (vanillaComponent != null) return vanillaComponent.selected();
        return selected;
    }

    @Override
    protected net.minecraft.client.gui.components.Checkbox createVanillaComponent() {
        Checkbox self = this;
        var builder = net.minecraft.client.gui.components.Checkbox.builder(showLabel
                ? component
                : Component.empty(), Minecraft.getInstance().font);
        builder.selected(selected);
        builder.pos(0, 0);
        builder.onValueChange((cb, selected) -> {
            onSelectionChange.now(self, selected);
        });
        net.minecraft.client.gui.components.Checkbox cb = builder.build();

        onSelectionChange.now(this, cb.selected());
        return cb;
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

    @Override
    public int getContentWidth() {
        if (!showLabel) return 24;
        return super.getContentWidth();
    }
}

