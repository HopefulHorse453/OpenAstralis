package cc.astralis.font;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.awt.*;

@AllArgsConstructor
public final class UIFont {
    private final FontRenderer font;
    @Getter
    private final float size;

    public FontRenderer getFontRenderer() {
        return font;
    }

    public void drawString(String text, float x, float y, Color color) {
        font.drawString(text, x, y, color);
    }

    public void drawStringWithShadow(String text, float x, float y, Color color) {
        font.drawStringWithShadow(text, x, y, color);
    }

    public void drawCenteredString(String text, float x, float y, Color color) {
        font.drawCenteredString(text, x, y, color);
    }

    public void drawCenteredStringWithShadow(String text, float x, float y, Color color) {
        font.drawCenteredStringWithShadow(text, x, y, color);
    }

    public void drawCenteredStringToo(String text, float x, float y, Color color) {
        font.drawCenteredStringToo(text, x, y, color);
    }

    public void drawCenteredStringWithShadowToo(String text, float x, float y, Color color) {
        font.drawCenteredStringWithShadowToo(text, x, y, color);
    }

    public void drawCenterStringInBox(String text, float x, float y, float width, float height, Color color) {
        font.drawCenterStringInBox(text, x, y, width, height, color);
    }

    public void drawCenterStringInBoxWithShadow(String text, float x, float y, float width, float height, Color color) {
        font.drawCenterStringInBoxWithShadow(text, x, y, width, height, color);
    }

    public void drawString(String text, float x, float y, Color color, boolean shadow) {
        if (shadow) {
            font.drawStringWithShadow(text, x, y, color);
        } else {
            font.drawString(text, x, y, color);
        }
    }

    public float getStringWidth(String text) {
        return font.getStringWidth(text);
    }

    public float getStringHeight(String text) {
        return font.getStringHeight(text);
    }
}