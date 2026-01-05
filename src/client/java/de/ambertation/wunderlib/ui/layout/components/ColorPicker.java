package de.ambertation.wunderlib.ui.layout.components;

import net.minecraft.network.chat.Component;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import de.ambertation.wunderlib.ui.ColorHelper;
import de.ambertation.wunderlib.ui.layout.values.Value;

@OnlyIn(Dist.CLIENT)
public class ColorPicker extends AbstractHorizontalStack<ColorPicker> {
    ColorSwatch swatch;
    Input input;

    public ColorPicker(Value width, Value height, Component title, int color) {
        super(width, height);
        swatch = addColorSwatch(Value.fixed(20), Value.fixed(20), color);
        input = addInput(Value.fill(), Value.fit(), title, ColorHelper.toRGBHex(color));

        //input.setFilter(ColorUtil::validHexColor);
        input.setResponder(this::inputResponder);
    }

    private void inputResponder(String value) {
        if (ColorHelper.validHexColor(value)) {
            int color = ColorHelper.parseHex(value);
            swatch.setColor(color);
            swatch.setOffsetInner(false);
            swatch.setBorderColor(ColorHelper.BLACK);
        } else {
            swatch.setOffsetInner(true);
            swatch.setBorderColor(ColorHelper.RED);
        }
    }
}

