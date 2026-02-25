package cc.astralis.skija;

import cc.astralis.Astralis;
import cc.astralis.util.language.Lazy;
import io.github.humbleui.skija.*;
import io.github.humbleui.types.RRect;
import io.github.humbleui.types.Rect;

import java.util.ArrayList;
import java.util.List;
import static cc.astralis.skija.utils.SkijaHelperUtil.*;

/**
 * @author Kawase
 * @since 18.10.2025
 */
public class ShaderRenderer {
    private static final Paint blendClear = new Paint()
            .setBlendMode(BlendMode.CLEAR);

    private static final Paint blurPaint = new Paint();

    private static final Lazy<Paint> bloomPaint = Lazy.of(() -> {
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        return paint;
    });

    private final List<ShaderShape> shapes = new ArrayList<>();
    private final Canvas canvas;

    public ShaderRenderer(Canvas canvas) {
        this.canvas = canvas;
    }

    private record ShaderShape(Path path, Rect bounds) { /* w */ }

    public void addRectangle(float x, float y, float width, float height) {
        Path path = new Path();
        path.addRect(Rect.makeXYWH(s(x), s(y), s(width), s(height)));
        shapes.add(new ShaderShape(path, Rect.makeXYWH(s(x), s(y), s(width), s(height))));
    }

    public void addRoundedRectangle(float x, float y, float width, float height, float radius) {
        Path path = new Path();
        path.addRRect(RRect.makeXYWH(s(x), s(y), s(width), s(height), s(radius)));
        shapes.add(new ShaderShape(path, Rect.makeXYWH(s(x), s(y), s(width), s(height))));
    }

    public void addRoundedRectangleVarying(float x, float y, float width, float height,
                                           float tl, float tr, float br, float bl) {
        float[] radii = sr(tl, tl, tr, tr, br, br, bl, bl);
        Path path = new Path();
        path.addRRect(RRect.makeComplexXYWH(s(x), s(y), s(width), s(height), radii));
        shapes.add(new ShaderShape(path, Rect.makeXYWH(s(x), s(y), s(width), s(height))));
    }

    public void addPoint4Rectangle(float x1, float y1, float x2, float y2) {
        Path path = new Path();
        Rect rect = makeNormalizedRect(x1, y1, x2, y2);
        path.addRect(rect);
        shapes.add(new ShaderShape(path, rect));
    }

    public void renderShaders(boolean blur, float blurRadius, boolean bloom, float bloomRadius) {
        if (shapes.isEmpty())
            return;

        Paint bloomPaintInstance = bloomPaint.get();

        if (bloom) {
            float radius = s(bloomRadius);

            bloomPaintInstance.setImageFilter(ImageFilter.makeBlur(radius, radius, FilterTileMode.DECAL));
            bloomPaintInstance.setColor(java.awt.Color.BLACK.getRGB());

            for (ShaderShape shape : shapes) {
                canvas.drawPath(shape.path, bloomPaintInstance);
            }

            bloomPaintInstance.setImageFilter(null);
        }

        if (blur) {
            float radius = s(blurRadius);

            blurPaint.setImageFilter(ImageFilter.makeBlur(radius, radius, FilterTileMode.CLAMP));

            Image fb = Astralis.getInstance().getSkija().framebufferImage();

            for (ShaderShape shape : shapes) {
                canvas.save();
                canvas.clipPath(shape.path, ClipMode.INTERSECT, true);
                canvas.drawImageRect(fb,
                        Rect.makeXYWH(0, 0, s(fb.getWidth()), s(fb.getHeight())),
                        blurPaint
                );
                canvas.restore();
            }

            blurPaint.setImageFilter(null);
        }

        shapes.forEach(shape -> shape.path.close());
        shapes.clear();
    }
}