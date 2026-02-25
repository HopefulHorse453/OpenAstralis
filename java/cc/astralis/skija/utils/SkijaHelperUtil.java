package cc.astralis.skija.utils;

import io.github.humbleui.types.RRect;
import io.github.humbleui.types.Rect;
import org.lwjgl.opengl.GL30;

/**
 * @author Kawase
 * @since 19.10.2025
 */
public class SkijaHelperUtil {
    private static float SCALE = 2f;

    public static void setUiScale(float scale) {
        SCALE = Math.max(0.5f, scale);
    }

    public static float getUiScale() {
        return SCALE;
    }

    // methods for scaling shit.
    public static float s(float v) {
        return v * SCALE;
    }

    public static Rect r(float x, float y, float w, float h) {
        return Rect.makeXYWH(s(x), s(y), s(w), s(h));
    }

    public static RRect rr(float x, float y, float w, float h, float rad) {
        return RRect.makeXYWH(s(x), s(y), s(w), s(h), s(rad));
    }

    public static RRect rr(float x, float y, float w, float h, float tl, float tr, float br, float bl) {
        float[] radii = sr(tl, tl, tr, tr, br, br, bl, bl);
        return RRect.makeComplexXYWH(s(x), s(y), s(w), s(h), radii);
    }

    public static float[] sr(float... radii) {
        for (int i = 0; i < radii.length; i++) radii[i] *= SCALE;
        return radii;
    }

    public static Rect makeNormalizedRect(float x1, float y1, float x2, float y2) {
        float left = Math.min(x1, x2);
        float right = Math.max(x1, x2);
        float top = Math.min(y1, y2);
        float bottom = Math.max(y1, y2);

        // Ensures non-degenerate rectangles.
        if (right - left < 0.5f)
            right = left + 0.5f;
        if (bottom - top < 0.5f)
            bottom = top + 0.5f;

        return Rect.makeLTRB(s(left), s(top), s(right), s(bottom));
    }

    public static int getGLVersion() {
        int[] major = new int[1];
        int[] minor = new int[1];
        GL30.glGetIntegerv(GL30.GL_MAJOR_VERSION, major);
        GL30.glGetIntegerv(GL30.GL_MINOR_VERSION, minor);
        return major[0] * 100 + minor[0] * 10;
    }
}
