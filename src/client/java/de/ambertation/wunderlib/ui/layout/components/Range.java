package de.ambertation.wunderlib.ui.layout.components;

import de.ambertation.wunderlib.ui.layout.components.render.RangeRenderer;
import de.ambertation.wunderlib.ui.layout.values.Value;
import de.ambertation.wunderlib.ui.vanilla.Slider;

import net.minecraft.network.chat.Component;


public class Range<N extends Number> extends AbstractVanillaComponent<Slider<N>, Range<N>> {
    @FunctionalInterface
    public interface ValueChanged<N extends Number> {
        void now(Range<N> range, N newValue);
    }

    private ValueChanged<N> onChange;
    private final N minValue;
    private final N maxValue;
    private final N initialValue;

    public Range(
            Value width,
            Value height,
            Component component,
            N minValue,
            N maxValue,
            N initialValue
    ) {
        super(width, height, new RangeRenderer<>(), component);
        this.onChange = (a, b) -> {
        };
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.initialValue = initialValue;
    }

    public Range(
            Value width,
            Value height,
            N minValue,
            N maxValue,
            N initialValue
    ) {
        this(width, height, null, minValue, maxValue, initialValue);
    }

    public Range<N> onChange(ValueChanged<N> onChange) {
        this.onChange = onChange;
        return this;
    }

    @Override
    protected Slider<N> createVanillaComponent() {
        Range<N> self = this;
        return new Slider<>(
                0,
                0,
                relativeBounds.width,
                relativeBounds.height,
                component,
                minValue,
                maxValue,
                getValue(),
                (s, v) -> onChange.now(self, v)
        );
    }

    public N getValue() {
        if (vanillaComponent != null)
            return vanillaComponent.currentValue();
        return initialValue;
    }


    @Override
    protected Component contentComponent() {
        Slider<N> dummy = new Slider<>(
                0,
                0,
                100,
                20,
                component,
                minValue,
                maxValue,
                initialValue,
                (a, b) -> {
                }
        );
        return dummy.getValueComponent(maxValue);
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
