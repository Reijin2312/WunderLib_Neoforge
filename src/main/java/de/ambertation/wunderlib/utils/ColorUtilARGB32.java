package de.ambertation.wunderlib.utils;

import net.minecraft.util.Mth;

/**
 * Utility class for working with ARGB32 color values.
 * This is a replacement for the formerly used FastColor class.
 */
public class ColorUtilARGB32 {
    /**
     * Creates a color with a different alpha value.
     *
     * @param rgb   Base Color
     * @param alpha Alpha component (0-255)
     * @return ARGB32 color value
     */
    public static int color(int rgb, int alpha) {
        return ((alpha & 0xFF) << 24) | (rgb & 0x00FFFFFF);
    }

    /**
     * Creates a color value from alpha, red, green, and blue components.
     *
     * @param alpha Alpha component (0-255)
     * @param red   Red component (0-255)
     * @param green Green component (0-255)
     * @param blue  Blue component (0-255)
     * @return ARGB32 color value
     */
    public static int color(int alpha, int red, int green, int blue) {
        return ((alpha & 0xFF) << 24) | ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF);
    }

    /**
     * Creates a color value with full alpha (255) from red, green, and blue components.
     *
     * @param red   Red component (0-255)
     * @param green Green component (0-255)
     * @param blue  Blue component (0-255)
     * @return ARGB32 color value with alpha=255
     */
    public static int color(int red, int green, int blue) {
        return color(255, red, green, blue);
    }

    /**
     * Creates a color value from alpha, red, green, and blue float components.
     * Each float value should be in the range [0.0, 1.0]
     *
     * @param alpha Alpha component (0.0-1.0)
     * @param red   Red component (0.0-1.0)
     * @param green Green component (0.0-1.0)
     * @param blue  Blue component (0.0-1.0)
     * @return ARGB32 color value
     */
    public static int color(float alpha, float red, float green, float blue) {
        return color(
                (int) (alpha * 255.0f),
                (int) (red * 255.0f),
                (int) (green * 255.0f),
                (int) (blue * 255.0f)
        );
    }

    /**
     * Creates a color value with full alpha (1.0) from red, green, and blue float components.
     * Each float value should be in the range [0.0, 1.0]
     *
     * @param red   Red component (0.0-1.0)
     * @param green Green component (0.0-1.0)
     * @param blue  Blue component (0.0-1.0)
     * @return ARGB32 color value with alpha=255
     */
    public static int color(float red, float green, float blue) {
        return color(1.0f, red, green, blue);
    }

    /**
     * Extracts the red component from an ARGB32 color value.
     *
     * @param color ARGB32 color value
     * @return Red component (0-255)
     */
    public static int red(int color) {
        return (color >> 16) & 0xFF;
    }

    /**
     * Extracts the green component from an ARGB32 color value.
     *
     * @param color ARGB32 color value
     * @return Green component (0-255)
     */
    public static int green(int color) {
        return (color >> 8) & 0xFF;
    }

    /**
     * Extracts the blue component from an ARGB32 color value.
     *
     * @param color ARGB32 color value
     * @return Blue component (0-255)
     */
    public static int blue(int color) {
        return color & 0xFF;
    }

    /**
     * Extracts the alpha component from an ARGB32 color value.
     *
     * @param color ARGB32 color value
     * @return Alpha component (0-255)
     */
    public static int alpha(int color) {
        return (color >> 24) & 0xFF;
    }

    /**
     * Returns a new color with a modified alpha component.
     *
     * @param color Original ARGB32 color value
     * @param alpha New alpha component (0-255)
     * @return Modified ARGB32 color with the new alpha value
     */
    public static int withAlpha(int color, int alpha) {
        return ((alpha & 0xFF) << 24) | (color & 0x00FFFFFF);
    }

    /**
     * Returns a fully opaque version of the provided color (alpha = 255).
     *
     * @param color Original ARGB32 color value
     * @return Fully opaque version of the color
     */
    public static int opaque(int color) {
        return withAlpha(color, 255);
    }

    /**
     * Linearly interpolates between two colors by the given amount.
     * When amount = 0, returns the first color.
     * When amount = 1, returns the second color.
     * Otherwise returns a proportional blend between the two.
     *
     * @param color1 The first ARGB32 color value
     * @param color2 The second ARGB32 color value
     * @param amount The blend amount, in the range [0, 1]
     * @return The interpolated color
     */
    public static int lerp(int color1, int color2, float amount) {
        if (amount <= 0) return color1;
        if (amount >= 1) return color2;

        return color(
                Mth.lerpInt(amount, alpha(color1), alpha(color2)),
                Mth.lerpInt(amount, red(color1), red(color2)),
                Mth.lerpInt(amount, green(color1), green(color2)),
                Mth.lerpInt(amount, blue(color1), blue(color2))
        );
    }
}
