package cc.astralis.font;

import cc.astralis.skija.utils.SkijaUtil;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Data;
import io.github.humbleui.skija.FilterTileMode;
import io.github.humbleui.skija.Font;
import io.github.humbleui.skija.FontEdging;
import io.github.humbleui.skija.FontHinting;
import io.github.humbleui.skija.FontMetrics;
import io.github.humbleui.skija.ImageFilter;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.Typeface;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;

import static cc.astralis.skija.utils.SkijaHelperUtil.getUiScale;

public final class FontRenderer {
    private final String name;
    private final float size;

    private final Typeface typeface;
    private final Font skijaFont;
    private final UIFont uiFont;

    private final Paint paint = new Paint();

    private final ImageFilter glowFilter = ImageFilter.makeBlur(4f * getUiScale(), 4f * getUiScale(), FilterTileMode.DECAL);

    private volatile boolean disposed = false;

    public FontRenderer(final String name, final float size) {
        this.name = name;
        this.size = size;

        Typeface tf;
        try (InputStream in = FontRenderer.class.getResourceAsStream("/assets/astralis/fonts/" + name + ".ttf")) {
            if (in == null) throw new IllegalArgumentException("Can't load font: " + name);
            byte[] bytes = in.readAllBytes();
            tf = Typeface.makeFromData(Data.makeFromBytes(bytes));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load font: " + name, e);
        }

        this.typeface = tf;

        this.skijaFont = new Font(typeface, this.size * getUiScale());
        this.skijaFont.setSubpixel(true);
        this.skijaFont.setHinting(FontHinting.FULL);
        this.skijaFont.setEdging(FontEdging.SUBPIXEL_ANTI_ALIAS);

        this.uiFont = new UIFont(this, size * getUiScale());
    }

    public Font getSkijaFont() {
        return skijaFont;
    }

    public UIFont asUiFont() {
        return uiFont;
    }

    public void drawString(String text, float x, float y, Color baseColor) {
        drawInternal(text, x, y, baseColor, false, false);
    }

    public void drawStringWithShadow(String text, float x, float y, Color baseColor) {
        drawInternal(text, x, y, baseColor, false, true);
    }

    // this shi might be fucked idk tbh.
    public void drawCenteredString(String text, float x, float y, Color color) {
        float dx = x - (this.getStringWidth(text) / (2f * getUiScale()));
        drawInternal(text, dx, y, color, false, false);
    }

    public void drawCenteredStringWithShadow(String text, float x, float y, Color color) {
        float dx = x - (this.getStringWidth(text) / (2f * getUiScale()));
        drawInternal(text, dx, y, color, false, true);
    }

    public void drawCenteredStringToo(String text, float x, float y, Color color) {
        float dx = x - (this.getStringWidth(text) / (2f));
        float dy = y - (this.getStringHeight(text) / (2f));
        drawInternal(text, dx, dy, color, false, false);
    }

    public void drawCenteredStringWithShadowToo(String text, float x, float y, Color color) {
        float dx = x - (this.getStringWidth(text) / (2f));
        float dy = y - (this.getStringHeight(text) / (2f));
        drawInternal(text, dx, dy, color, false, true);
    }

    public void drawCenterStringInBox(String text, float x, float y, float width, float height, Color color) {
        float dx = x + (width - this.getStringWidth(text))  / (2f);
        float dy = y + (height - this.getStringHeight(text)) / (2f);
        drawInternal(text, dx, dy, color, false, false);
    }

    public void drawCenterStringInBoxWithShadow(String text, float x, float y, float width, float height, Color color) {
        float dx = x + (width  - this.getStringWidth(text)) / 2;
        float dy = y + (height - this.getStringHeight(text)) / 2;
        drawInternal(text, dx, dy, color, false, true);
    }

    public void drawString(String text, float x, float y, boolean glow, Color textColor) {
        drawInternal(text, x, y, textColor, glow, false);
    }

    public float getStringWidth(String text) {
        return this.skijaFont.measureTextWidth(ChatFormatting.stripFormatting(text), null) / getUiScale();
    }

    public float getStringHeight(String ignored) {
        FontMetrics metrics = skijaFont.getMetrics();
        return (metrics.getDescent() - metrics.getAscent()) / getUiScale();
    }

    public synchronized void dispose() {
        if (disposed)
            return;

        glowFilter.close();
        paint.close();
        skijaFont.close();
        typeface.close();

        disposed = true;
    }

    private static final ImageFilter SHADOW_FILTER = ImageFilter.makeDropShadow(
            1f, 1f,
            0.5f, 0.5f,
            0xFF000000, null, null
    );

    private void drawInternal(String text, float x, float y, Color color, boolean glow, boolean shadow) {
        Canvas canvas = SkijaUtil.getCanvas();

        if (canvas == null || text.isEmpty()) return;

        float drawX = x * getUiScale();

        FontMetrics metrics = skijaFont.getMetrics();
        float ascent = -metrics.getAscent();
        float drawY = (y * getUiScale()) + ascent;

        float currentX = drawX;

        for (TextSegment textSegment : parseMinecraftFormattedText(text, paint.setColor(color.getRGB()))) {
            String segmentText = textSegment.text();
            Paint paint = textSegment.paint();

            if (shadow) {
                Paint shadowPaint = paint.makeClone().setColor((color.getRGB() & 0xFCFCFC) >> 2 | color.getRGB() & 0xFF000000);
                canvas.drawString(segmentText, currentX + 1f, drawY + 1f, this.skijaFont, shadowPaint);
                shadowPaint.close();
            }

            if (glow) {
                try (Paint glowPaint = paint.makeClone().setColor(color.getRGB()).setImageFilter(glowFilter)) {
                    canvas.drawString(segmentText, currentX, drawY, this.skijaFont, glowPaint);
                }
            }

            canvas.drawString(segmentText, currentX, drawY, this.skijaFont, paint);
            currentX += this.skijaFont.measureTextWidth(segmentText, paint);
        }
    }

    public List<TextSegment> parseMinecraftFormattedText(String input, Paint originalPaint) {
        if (input.isEmpty()) return Collections.emptyList();

        List<TextSegment> result = new ArrayList<>();
        Pattern pattern = Pattern.compile("\u00A7([0-9a-fklmnor])");
        Matcher matcher = pattern.matcher(input);
        Paint currentPaint = originalPaint.makeClone();
        int lastIndex = 0;

        while (matcher.find()) {
            char formatChar = matcher.group(1).charAt(0);
            ChatFormatting formatting = ChatFormatting.getByCode(formatChar);
            if (formatting == null) continue;

            String textSegment = input.substring(lastIndex, matcher.start());
            if (!textSegment.isEmpty()) {
                result.add(new TextSegment(textSegment, currentPaint.makeClone()));
            }

            if (formatting.isColor()) {
                Integer color = formatting.getColor();
                if (color == null) continue;
                currentPaint = clonePaintWithColor(originalPaint, color);
            } else if (formatting == ChatFormatting.RESET) {
                currentPaint = originalPaint.makeClone();
            }

            lastIndex = matcher.end();
        }

        if (lastIndex < input.length()) {
            String textSegment = input.substring(lastIndex);
            if (!textSegment.isEmpty()) {
                result.add(new TextSegment(textSegment, currentPaint.makeClone()));
            }
        }

        return result;
    }

    private Paint clonePaintWithColor(Paint base, int color) {
        Paint newPaint = base.makeClone();
        newPaint.setShader(null);
        newPaint.setColor(color | 0xFF000000);
        return newPaint;
    }

    private static int darkerARGB(Color c) {
        int a = (int) (0.60f * 255);
        int r = (int) (c.getRed() * 0.3f);
        int g = (int) (c.getGreen() * 0.3f);
        int b = (int) (c.getBlue() * 0.3f);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public record TextSegment(String text, Paint paint) {
        /* w */
    }
}
