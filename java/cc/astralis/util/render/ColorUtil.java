package cc.astralis.util.render;

import cc.astralis.interfaces.IAccess;
import java.awt.*;
import net.minecraft.client.model.geom.builders.UVPair;

public class ColorUtil implements IAccess {
    public static int getSwap(Color color) {
        return new Color(color.getBlue(), color.getGreen(), color.getRed()).getRGB();
    }

    public static Color getAccentColor(UVPair screenCoordinates, Color firstColor, Color secondColor) {
        return mixColors(firstColor, secondColor, getBlendFactor(screenCoordinates));
    }

    static Color mixColors(final Color color1, final Color color2, final double percent) {
        final double inverse_percent = 1.0 - percent;
        final int redPart = (int) (color1.getRed() * percent + color2.getRed() * inverse_percent);
        final int greenPart = (int) (color1.getGreen() * percent + color2.getGreen() * inverse_percent);
        final int bluePart = (int) (color1.getBlue() * percent + color2.getBlue() * inverse_percent);

        return new Color(redPart, greenPart, bluePart);
    }

    public static double getBlendFactor(UVPair screenCoordinates) {
        return Math.sin(
                System.currentTimeMillis() / 175.0D + screenCoordinates.u() * 0.0007D + screenCoordinates.v() * 0.0007D
        ) * 0.5D + 0.5D;
    }

    public static Color intToColor(int color) {
        int alpha = (color >> 24) & 0xFF;
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        return new Color(red, green, blue, alpha);
    }

    public static int interpolateColor(int color1, int color2, float fraction) {
        int red1 = (color1 >> 16) & 0xFF;
        int green1 = (color1 >> 8) & 0xFF;
        int blue1 = color1 & 0xFF;

        int red2 = (color2 >> 16) & 0xFF;
        int green2 = (color2 >> 8) & 0xFF;
        int blue2 = color2 & 0xFF;

        int red = (int) (red1 + fraction * (red2 - red1));
        int green = (int) (green1 + fraction * (green2 - green1));
        int blue = (int) (blue1 + fraction * (blue2 - blue1));

        return (red << 16) | (green << 8) | blue;
    }

    public static float[] toGLColor(final int color) {
        final float f = (float) (color >> 16 & 255) / 255.0F;
        final float f1 = (float) (color >> 8 & 255) / 255.0F;
        final float f2 = (float) (color & 255) / 255.0F;
        final float f3 = (float) (color >> 24 & 255) / 255.0F;
        return new float[]{f, f1, f2, f3};
    }

    public static Color toColor(float[] colors){
        return new Color(colors[0], colors[1], colors[2], colors[3]);
    }

    public static Color interpolateColorsBackAndForth(int speed, int index, Color start, Color end, boolean trueColor) {
        int angle = (int) (((System.currentTimeMillis()) / speed + index) % 360);
        angle = (angle >= 180 ? 360 - angle : angle) * 2;
        return trueColor ? ColorUtil.interpolateColorHue(start, end, angle / 360f) : ColorUtil.interpolateColorC(start, end, angle / 360f);
    }

    // just dont look down here :3
    public static Double interpolate(double oldValue, double newValue, double interpolationValue) {
        return (oldValue + (newValue - oldValue) * interpolationValue);
    }

    public static int interpolateInt(int oldValue, int newValue, double interpolationValue) {
        return interpolate(oldValue, newValue, (float) interpolationValue).intValue();
    }

    public static float interpolateFloat(float oldValue, float newValue, double interpolationValue) {
        return interpolate(oldValue, newValue, (float) interpolationValue).floatValue();
    }

    public static Color withAlpha(final Color color, final int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    public static Color interpolateColorC(Color color1, Color color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));
        return new Color(interpolateInt(color1.getRed(), color2.getRed(), amount),
                interpolateInt(color1.getGreen(), color2.getGreen(), amount),
                interpolateInt(color1.getBlue(), color2.getBlue(), amount),
                interpolateInt(color1.getAlpha(), color2.getAlpha(), amount));
    }

    public static int[] RGBIntToRGB(int in) {
        int red = in >> 8 * 2 & 0xFF;
        int green = in >> 8 & 0xFF;
        int blue = in & 0xFF;
        return new int[]{red, green, blue};
    }

    public static Color interpolateColorHue(Color color1, Color color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));

        float[] color1HSB = Color.RGBtoHSB(color1.getRed(), color1.getGreen(), color1.getBlue(), null);
        float[] color2HSB = Color.RGBtoHSB(color2.getRed(), color2.getGreen(), color2.getBlue(), null);

        Color resultColor = Color.getHSBColor(interpolateFloat(color1HSB[0], color2HSB[0], amount),
                interpolateFloat(color1HSB[1], color2HSB[1], amount), interpolateFloat(color1HSB[2], color2HSB[2], amount));

        return ColorUtil.applyOpacity(resultColor, interpolateInt(color1.getAlpha(), color2.getAlpha(), amount) / 255f);
    }

    public static Color applyOpacity(Color color, float opacity) {
        opacity = Math.min(1, Math.max(0, opacity));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (color.getAlpha() * opacity));
    }

    public static Color applyTint(int baseGrey, Color tintColor, double tintWeight) {
        int finalRed = (int) (baseGrey + tintColor.getRed() * tintWeight);
        int finalGreen = (int) (baseGrey + tintColor.getGreen() * tintWeight);
        int finalBlue = (int) (baseGrey + tintColor.getBlue() * tintWeight);

        finalRed = Math.min(255, Math.max(0, finalRed));
        finalGreen = Math.min(255, Math.max(0, finalGreen));
        finalBlue = Math.min(255, Math.max(0, finalBlue));

        return new Color(finalRed, finalGreen, finalBlue);
    }
}
