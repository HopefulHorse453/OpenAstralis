package cc.astralis.skija.utils;

import cc.astralis.Astralis;
import cc.astralis.module.impl.visual.HudModule;
import cc.astralis.skija.io.ImageUtil;
import cc.astralis.util.language.Lazy;
import cc.astralis.skija.ShaderRenderer;
import cc.astralis.util.network.AccountUtil;
import io.github.humbleui.skija.*;
import io.github.humbleui.types.RRect;
import io.github.humbleui.types.Rect;
import lombok.Getter;
import lombok.Setter;

import java.awt.Color;
import static cc.astralis.skija.utils.SkijaHelperUtil.*;

public class SkijaUtil {
    private static ShaderRenderer currentShaderRenderer;

    private static final Paint PAINT = new Paint();
    private static final Paint STROKE = new Paint().setAntiAlias(true).setMode(PaintMode.STROKE);

    private static final Lazy<Paint> color = Lazy.of(() -> {
        final Paint paint = new Paint();
        paint.setColor(io.github.humbleui.skija.Color.makeARGB(255, 255, 255, 255));
        return paint;
    });

    private static final Paint blendClear = new Paint()
            .setBlendMode(BlendMode.CLEAR);

    private static final Paint tintBlend = new Paint().setAntiAlias(true);

    private static final Paint blurPaint = new Paint();

    @Setter
    @Getter
    private static Canvas canvas;

    public static void beginShaderFrame() {
        currentShaderRenderer = new ShaderRenderer(canvas);
    }

    public static void drawShaderRectangle(float x, float y, float width, float height) {
        if (currentShaderRenderer == null)
            return;

        currentShaderRenderer.addRectangle(x, y, width, height);
    }

    public static void drawShaderRoundRectangle(float x, float y, float width, float height, float radius) {
        if (currentShaderRenderer == null)
            return;

        currentShaderRenderer.addRoundedRectangle(x, y, width, height, radius);
    }

    public static void drawShaderRectangleVarying(float x, float y, float width, float height,
                                                  float tl, float tr, float br, float bl) {
        if (currentShaderRenderer == null)
            return;

        currentShaderRenderer.addRoundedRectangleVarying(x, y, width, height, tl, tr, br, bl);
    }

    public static void drawShaderPoint4Rectangle(float x1, float y1, float x2, float y2) {
        if (currentShaderRenderer == null)
            return;

        currentShaderRenderer.addPoint4Rectangle(x1, y1, x2, y2);
    }

    public static void drawShaders(boolean blur, float blurRadius, boolean bloom, float bloomRadius) {
        if (currentShaderRenderer != null) {
            currentShaderRenderer.renderShaders(blur, blurRadius, bloom, bloomRadius);
            currentShaderRenderer = null;
        }
    }

    public static void drawShaders() {
        HudModule hud = Astralis.getInstance().getModuleManager().getModule(HudModule.class);

        drawShaders(hud.blur.getProperty(), hud.blurRadius.getProperty().floatValue(), hud.bloom.getProperty(), hud.bloomRadius.getProperty().floatValue());
    }

    public static void drawLiquidGlass(float x, float y, float width, float height, int opacity) {
        x = s(x);
        y = s(y);
        width = s(width);
        height = s(height);

        canvas.save();
        RRect glassRect = RRect.makeXYWH(x, y, width, height, 20);

        canvas.clipRRect(glassRect, true);

        try (Paint glassPaint = new Paint()) {
            int topColor = io.github.humbleui.skija.Color.makeARGB(opacity, 255, 255, 255);
            int bottomColor = io.github.humbleui.skija.Color.makeARGB(opacity - 30, 240, 245, 255);

            try (Shader gradient = Shader.makeLinearGradient(
                    x, y, x, y + height,
                    new int[]{topColor, bottomColor})) {
                glassPaint.setShader(gradient);
                canvas.drawRRect(glassRect, glassPaint);
            }
        }

        try (Paint highlightPaint = new Paint()) {
            highlightPaint.setColor(io.github.humbleui.skija.Color.makeARGB(60, 255, 255, 255));
            highlightPaint.setStrokeWidth(2);
            highlightPaint.setMode(PaintMode.STROKE);

            try (Path highlightPath = new Path()) {
                highlightPath.moveTo(x + 20, y);
                highlightPath.lineTo(x + width - 20, y);
                canvas.drawPath(highlightPath, highlightPaint);
            }
        }

        try (Paint borderPaint = new Paint()) {
            borderPaint.setColor(io.github.humbleui.skija.Color.makeARGB(100, 255, 255, 255));
            borderPaint.setStrokeWidth(1.5f);
            borderPaint.setMode(PaintMode.STROKE);
            canvas.drawRRect(glassRect, borderPaint);
        }

        canvas.restore();
    }

    private static Shader makeLinear(float x, float y, float w, float h, int c1, int c2, boolean vertical) {
        x = s(x);
        y = s(y);
        w = s(w);
        h = s(h);
        float x2 = vertical ? x : x + w;
        float y2 = vertical ? y + h : y;
        return Shader.makeLinearGradient(
                x, y, x2, y2,
                new int[]{c1, c2},
                null,
                new GradientStyle(FilterTileMode.CLAMP, false, null)
        );
    }

    public static void rectangle(float x, float y, float w, float h, Color color) {
        PAINT.reset().setAntiAlias(true).setColor(color.getRGB());
        canvas.drawRect(r(x, y, w, h), PAINT);
    }

    public static void rectangleOutline(float x, float y, float w, float h,
                                        Color fillColor, float thickness, Color outlineColor) {
        rectangle(x, y, w, h, fillColor);
        STROKE.reset().setAntiAlias(true).setMode(PaintMode.STROKE)
                .setColor(outlineColor.getRGB()).setStrokeWidth(s(thickness));
        canvas.drawRect(r(x, y, w, h), STROKE);
    }

    public static void rectangleGradient(float x, float y, float w, float h,
                                         Color c1, Color c2, boolean vertical) {
        Paint p = color.get().setAntiAlias(true)
                .setShader(makeLinear(x, y, w, h, c1.getRGB(), c2.getRGB(), vertical));
        canvas.drawRect(r(x, y, w, h), p);
    }

    public static void rectangleGradientOutline(float x, float y, float w, float h,
                                                Color c1, Color c2, boolean vertical,
                                                float thickness, Color outlineColor) {
        rectangleGradient(x, y, w, h, c1, c2, vertical);
        rectangleOutline(x, y, w, h, new Color(0, true), thickness, outlineColor);
    }

    public static void roundedRectangle(float x, float y, float w, float h, float radius, Color color) {
        PAINT.reset().setAntiAlias(true).setColor(color.getRGB());
        canvas.drawRRect(rr(x, y, w, h, radius), PAINT);
    }

    public static void roundedRectangleVarying(float x, float y, float w, float h,
                                               float tl, float tr, float br, float bl, Color color) {
        float[] radii = sr(tl, tl, tr, tr, br, br, bl, bl);
        PAINT.reset().setAntiAlias(true).setColor(color.getRGB());
        canvas.drawRRect(RRect.makeComplexXYWH(s(x), s(y), s(w), s(h), radii), PAINT);
    }

    public static void roundedRectangleOutline(float x, float y, float w, float h,
                                               float radius, Color fillColor,
                                               float thickness, Color outlineColor) {
        roundedRectangle(x, y, w, h, radius, fillColor);
        RRect rr0 = rr(x, y, w, h, radius);
        STROKE.reset().setAntiAlias(true).setMode(PaintMode.STROKE)
                .setColor(outlineColor.getRGB()).setStrokeWidth(s(thickness));
        canvas.drawRRect(rr0, STROKE);
    }

    public static void roundedRectangleOutlineVarying(float x, float y, float w, float h,
                                                      float tl, float tr, float br, float bl,
                                                      Color fillColor, float thickness, Color outlineColor) {
        roundedRectangleVarying(x, y, w, h, tl, tr, br, bl, fillColor);
        float[] radii = sr(tl, tl, tr, tr, br, br, bl, bl);
        RRect rr1 = RRect.makeComplexXYWH(s(x), s(y), s(w), s(h), radii);
        STROKE.reset().setAntiAlias(true).setMode(PaintMode.STROKE)
                .setColor(outlineColor.getRGB()).setStrokeWidth(s(thickness));
        canvas.drawRRect(rr1, STROKE);
    }

    public static void roundedRectangleGradient(float x, float y, float w, float h,
                                                float radius, Color c1, Color c2, boolean vertical) {
        Paint p = new Paint().setAntiAlias(true)
                .setShader(makeLinear(x, y, w, h, c1.getRGB(), c2.getRGB(), vertical));
        canvas.drawRRect(rr(x, y, w, h, radius), p);
        p.close();
    }

    public static void roundedRectangleDiagonalGradient(float x, float y, float w, float h,
                                                        float radius, Color c1, Color c2) {
        Shader shader = Shader.makeLinearGradient(
                s(x), s(y), s(x + w), s(y + h),
                new int[]{c1.getRGB(), c2.getRGB()}, null,
                new GradientStyle(FilterTileMode.CLAMP, false, null));
        Paint p = new Paint().setAntiAlias(true).setShader(shader);
        canvas.drawRRect(rr(x, y, w, h, radius), p);
        p.close();
    }

    public static void roundedRectangleGradientVarying(float x, float y, float w, float h,
                                                       float tl, float tr, float br, float bl,
                                                       Color c1, Color c2, boolean vertical) {
        float[] radii = sr(tl, tl, tr, tr, br, br, bl, bl);
        Paint p = new Paint().setAntiAlias(true)
                .setShader(makeLinear(x, y, w, h, c1.getRGB(), c2.getRGB(), vertical));
        canvas.drawRRect(RRect.makeComplexXYWH(s(x), s(y), s(w), s(h), radii), p);
        p.close();
    }

    public static void roundedRectangleGradientOutline(float x, float y, float w, float h,
                                                       float radius, Color c1, Color c2, boolean vertical,
                                                       float thickness, Color fillColor) {
        roundedRectangle(x, y, w, h, radius, fillColor);

        Paint stroke = new Paint()
                .setAntiAlias(true)
                .setMode(PaintMode.STROKE)
                .setStrokeWidth(s(thickness))
                .setShader(makeLinear(x, y, w, h, c1.getRGB(), c2.getRGB(), vertical));

        canvas.drawRRect(rr(x, y, w, h, radius), stroke);
        stroke.close();
    }

    public static void roundedRectangleGradientOutlineVarying(float x, float y, float w, float h,
                                                              float tl, float tr, float br, float bl,
                                                              Color c1, Color c2, boolean vertical,
                                                              float thickness, Color fillColor) {
        roundedRectangleVarying(x, y, w, h, tl, tr, br, bl, fillColor);

        float[] radii = sr(tl, tl, tr, tr, br, br, bl, bl);
        RRect rrect = RRect.makeComplexXYWH(s(x), s(y), s(w), s(h), radii);

        Paint stroke = new Paint()
                .setAntiAlias(true)
                .setMode(PaintMode.STROKE)
                .setStrokeWidth(s(thickness))
                .setShader(makeLinear(x, y, w, h, c1.getRGB(), c2.getRGB(), vertical));

        canvas.drawRRect(rrect, stroke);
        stroke.close();
    }

    public static void line(float x1, float y1, float x2, float y2, Color color) {
        PAINT.reset().setAntiAlias(true).setColor(color.getRGB());
        canvas.drawLine(s(x1), s(y1), s(x2), s(y2), PAINT);
    }

    public static void line(float x1, float y1, float x2, float y2, float thickness, Color color) {
        STROKE.reset().setAntiAlias(true).setMode(PaintMode.STROKE)
                .setColor(color.getRGB()).setStrokeWidth(s(thickness));
        canvas.drawLine(s(x1), s(y1), s(x2), s(y2), STROKE);
    }

    public static void drawTriangle(float x1, float y1, float x2, float y2, float x3, float y3, Color color) {
        Path p = new Path();
        p.moveTo(s(x1), s(y1));
        p.lineTo(s(x2), s(y2));
        p.lineTo(s(x3), s(y3));
        p.closePath();
        PAINT.reset().setAntiAlias(true).setColor(color.getRGB());
        canvas.drawPath(p, PAINT);
        p.close();
    }

    public static void circle(float x, float y, float radius, Color color) {
        PAINT.reset().setAntiAlias(true).setImageFilter(null).setColor(color.getRGB());
        canvas.drawCircle(s(x), s(y), s(radius), PAINT);
    }

    public static void drawCircle(float cx, float cy, float radius, Color color) {
        circle(cx, cy, radius, color);
    }

    public static void drawCircleOutline(float cx, float cy, float radius, float thickness, Color color) {
        STROKE.reset().setAntiAlias(true).setMode(PaintMode.STROKE)
                .setColor(color.getRGB()).setStrokeWidth(s(thickness));
        canvas.drawCircle(s(cx), s(cy), s(radius), STROKE);
    }

    public static void drawCircleOutline(float cx, float cy, float radius, float thickness,
                                         Color startColor, Color endColor, boolean vertical) {
        float x1 = cx - radius, y1 = cy - radius;
        STROKE.reset().setAntiAlias(true).setMode(PaintMode.STROKE).setStrokeWidth(s(thickness))
                .setShader(makeLinear(x1, y1, radius * 2, radius * 2, startColor.getRGB(), endColor.getRGB(), vertical));

        canvas.drawCircle(s(cx), s(cy), s(radius), STROKE);
    }

    public static void pixel(float x, float y, Color color) {
        rectangle(x, y, 1, 1, color);
    }

    public static void drawRectPoint4(float x1, float y1, float x2, float y2, Color color) {
        PAINT.reset().setAntiAlias(true).setColor(color.getRGB());
        canvas.drawRect(makeNormalizedRect(x1, y1, x2, y2), PAINT);
    }

    public static void drawRectPoint4VerticalGradient(float x1, float y1, float x2, float y2,
                                                      Color topColor, Color bottomColor) {
        Rect rct = makeNormalizedRect(x1, y1, x2, y2);
        Paint p = new Paint()
                .setAntiAlias(true)
                .setShader(makeLinear(
                        (rct.getLeft() / getUiScale()), (rct.getTop() / getUiScale()), // makeLinear scales again
                        (rct.getWidth() / getUiScale()), (rct.getHeight() / getUiScale()),
                        topColor.getRGB(), bottomColor.getRGB(),
                        true
                ));
        canvas.drawRect(rct, p);
        p.close();
    }

    public static void drawRectPoint4HorizontalGradient(float x1, float y1, float x2, float y2,
                                                        Color leftColor, Color rightColor) {
        Rect rct = makeNormalizedRect(x1, y1, x2, y2);
        Paint p = new Paint()
                .setAntiAlias(true)
                .setShader(makeLinear(
                        (rct.getLeft() / getUiScale()), (rct.getTop() / getUiScale()),
                        (rct.getWidth() / getUiScale()), (rct.getHeight() / getUiScale()),
                        leftColor.getRGB(), rightColor.getRGB(),
                        false
                ));
        canvas.drawRect(rct, p);
        p.close();
    }

    public static void renderRoundedImage(float x, float y, float w, float h, float radius, Image image, Color tint) {
        x = s(x);
        y = s(y);
        w = s(w);
        h = s(h);
        radius = s(radius);

        RRect rrect = RRect.makeXYWH(x, y, w, h, radius);

        canvas.save();
        canvas.clipRRect(rrect, ClipMode.INTERSECT, true);
        canvas.drawImageRect(image, Rect.makeXYWH(x, y, w, h));
        canvas.restore();
    }

    public static void renderImage(float x, float y, float w, float h, Image image, Color tint) {
        if (tint != null)
            tintBlend.setColorFilter(ColorFilter.makeBlend(tint.getRGB(), BlendMode.SRC_ATOP));
        canvas.drawImageRect(image, r(x, y, w, h), tintBlend);
    }

    public static void scissor(float x, float y, float width, float height) {
        Rect rect = r(x, y, width, height);
        push();
        canvas.clipRect(rect);
    }

    public static void scissored(float x, float y, float width, float height, Runnable draw) {
        scissor(x, y, width, height);
        try {
            draw.run();
        } finally {
            pop();
        }
    }

    public static void push() {
        canvas.save();
    }

    public static void pop() {
        canvas.restore();
    }

    public static void scale(float x, float y) {
        canvas.scale(x, y);
    }

    public static void rotate(float rotationAngle) {
        canvas.rotate(rotationAngle);
    }

    public static void translate(float x, float y) {
        canvas.translate(s(x), s(y));
    }
}
