package de.ambertation.wunderlib.ui.layout.components;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import de.ambertation.wunderlib.ui.layout.values.Rectangle;

@OnlyIn(Dist.CLIENT)
public interface ComponentWithBounds {
    Rectangle getRelativeBounds();
}

