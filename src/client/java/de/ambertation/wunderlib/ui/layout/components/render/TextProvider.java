package de.ambertation.wunderlib.ui.layout.components.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface TextProvider {
    default Font getFont() {
        return Minecraft.getInstance().font;
    }

    default int getWidth(net.minecraft.network.chat.Component c) {
        return getFont().width(c.getVisualOrderText()) + 24;
    }

    default int getLineHeight(net.minecraft.network.chat.Component c) {
        return getFont().lineHeight;
    }

    default int getHeight(net.minecraft.network.chat.Component c) {
        return getLineHeight(c) + 11;
    }
}

