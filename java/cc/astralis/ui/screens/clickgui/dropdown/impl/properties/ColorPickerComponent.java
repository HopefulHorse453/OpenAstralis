package cc.astralis.ui.screens.clickgui.dropdown.impl.properties;

import cc.astralis.font.FontManager;
import cc.astralis.property.properties.ColorProperty;
import cc.astralis.ui.screens.clickgui.dropdown.Component;
import cc.astralis.util.render.RenderUtil;
import cc.astralis.skija.utils.SkijaUtil;

import java.awt.*;

// fps killer 9k.
public class ColorPickerComponent extends Component {
    private final ColorProperty colorProperty;
    private final int pickerWidth = 100, pickerHeight = 100;
    private final int hueSliderHeight = 12;
    private final int alphaSliderHeight = 12;
    private float selectedColorX, selectedColorY;
    private float selectedHue = 0f;
    private float selectedAlpha = 1f;
    private boolean pickingColor = false;
    private boolean mouseHeld = false;

    public ColorPickerComponent(ColorProperty colorProperty) {
        this.colorProperty = colorProperty;
        this.setProperty(colorProperty);
        this.setWidth(130);
        this.setHeight(15);
        initializeFromCurrentColor();
    }

    private void initializeFromCurrentColor() {
        Color currentColor = colorProperty.getProperty();
        float[] hsb = Color.RGBtoHSB(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), null);

        selectedHue = hsb[0];
        selectedColorX = hsb[1] * pickerWidth;
        selectedColorY = (1.0f - hsb[2]) * pickerHeight;
        selectedAlpha = currentColor.getAlpha() / 255f;
    }

    @Override
    public void render(float mouseX, float mouseY) {
        if (!mouseHeld) {
            Color currentColor = colorProperty.getProperty();
            float[] hsb = Color.RGBtoHSB(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), null);

            if (Math.abs(hsb[0] - selectedHue) > 0.001f ||
                    Math.abs(hsb[1] - selectedColorX / pickerWidth) > 0.001f ||
                    Math.abs(hsb[2] - (1.0f - selectedColorY / pickerHeight)) > 0.001f ||
                    Math.abs(currentColor.getAlpha() / 255f - selectedAlpha) > 0.001f) {
                selectedHue = hsb[0];
                selectedColorX = hsb[1] * pickerWidth;
                selectedColorY = (1.0f - hsb[2]) * pickerHeight;
                selectedAlpha = currentColor.getAlpha() / 255f;
            }
        }

        this.setHeight(15 + (pickingColor ? pickerHeight + hueSliderHeight + alphaSliderHeight + 20 : 0));

        FontManager.getFont("tenacity", 8).drawString(colorProperty.getName(), getX() + 4, getY() + 4f, Color.WHITE);

        float rectX = getX() + getWidth() - 13;
        float rectY = getY() + 2;

        SkijaUtil.roundedRectangle(rectX, rectY, 10, 10, 4, colorProperty.getProperty());

        if (pickingColor) {
            float pickerX = getX() + 15;
            float pickerY = getY() + 20;

            renderColorPickerBox(pickerX, pickerY, pickerWidth, pickerHeight, selectedHue);

            float circleX = pickerX + selectedColorX;
            float circleY = pickerY + selectedColorY;
            SkijaUtil.circle(circleX, circleY, 6, new Color(0, 0, 0, 150));
            SkijaUtil.circle(circleX, circleY, 5, Color.WHITE);
            SkijaUtil.circle(circleX, circleY, 3, colorProperty.getProperty());

            float hueSliderY = pickerY + pickerHeight + 6;
            renderHueSlider(pickerX, hueSliderY, pickerWidth, hueSliderHeight);

            float hueX = pickerX + selectedHue * pickerWidth;
            SkijaUtil.roundedRectangle(hueX - 2.5f, hueSliderY - 3, 5, hueSliderHeight + 6, 3, new Color(0, 0, 0, 150));
            SkijaUtil.roundedRectangle(hueX - 2, hueSliderY - 2, 4, hueSliderHeight + 4, 2.5f, Color.WHITE);

            float alphaSliderY = hueSliderY + hueSliderHeight + 6;
            drawCheckeredBackground(pickerX, alphaSliderY, pickerWidth, alphaSliderHeight, 4);
            renderAlphaSlider(pickerX, alphaSliderY, pickerWidth, alphaSliderHeight, selectedHue);

            float alphaX = pickerX + selectedAlpha * pickerWidth;
            SkijaUtil.roundedRectangle(alphaX - 2.5f, alphaSliderY - 3, 5, alphaSliderHeight + 6, 3, new Color(0, 0, 0, 150));
            SkijaUtil.roundedRectangle(alphaX - 2, alphaSliderY - 2, 4, alphaSliderHeight + 4, 2.5f, Color.WHITE);
        }

        if (mouseHeld) {
            handleColorPicking(mouseX, mouseY);
        }
    }

    @Override
    public void click(double mouseX, double mouseY, int button) {
        float rectX = getX() + getWidth() - 13;
        float rectY = getY() + 2;

        if (RenderUtil.isHovered(mouseX, mouseY, rectX, rectY, 10, 10)) {
            pickingColor = !pickingColor;
            if (pickingColor) {
                initializeFromCurrentColor();
            }
        }

        mouseHeld = true;
        handleColorPicking(mouseX, mouseY);
    }

    @Override
    public void release(double mouseX, double mouseY, int button) {
        mouseHeld = false;
    }

    private void handleColorPicking(double mouseX, double mouseY) {
        if (!pickingColor) return;

        float pickerX = getX() + 15;
        float pickerY = getY() + 20;

        if (RenderUtil.isHovered(mouseX, mouseY, pickerX, pickerY, pickerWidth, pickerHeight)) {
            selectedColorX = (float) Math.max(0, Math.min(pickerWidth, mouseX - pickerX));
            selectedColorY = (float) Math.max(0, Math.min(pickerHeight, mouseY - pickerY));

            float saturation = selectedColorX / pickerWidth;
            float brightness = 1.0f - selectedColorY / pickerHeight;

            Color newColor = Color.getHSBColor(selectedHue, saturation, brightness);
            colorProperty.setProperty(new Color(newColor.getRed(), newColor.getGreen(), newColor.getBlue(), (int)(selectedAlpha * 255)));
        }

        float hueSliderY = pickerY + pickerHeight + 6;
        if (RenderUtil.isHovered(mouseX, mouseY, pickerX, hueSliderY, pickerWidth, hueSliderHeight)) {
            selectedHue = (float) Math.max(0, Math.min(1, (mouseX - pickerX) / pickerWidth));

            float saturation = selectedColorX / pickerWidth;
            float brightness = 1.0f - selectedColorY / pickerHeight;

            Color newColor = Color.getHSBColor(selectedHue, saturation, brightness);
            colorProperty.setProperty(new Color(newColor.getRed(), newColor.getGreen(), newColor.getBlue(), (int)(selectedAlpha * 255)));
        }

        float alphaSliderY = hueSliderY + hueSliderHeight + 6;
        if (RenderUtil.isHovered(mouseX, mouseY, pickerX, alphaSliderY, pickerWidth, alphaSliderHeight)) {
            selectedAlpha = (float) Math.max(0, Math.min(1, (mouseX - pickerX) / pickerWidth));

            Color currentColor = colorProperty.getProperty();
            colorProperty.setProperty(new Color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), (int)(selectedAlpha * 255)));
        }
    }

    private void drawCheckeredBackground(float x, float y, float w, float h, int checkSize) {
        int checksX = (int) Math.ceil(w / checkSize);
        int checksY = (int) Math.ceil(h / checkSize);

        for (int i = 0; i < checksX; i++) {
            for (int j = 0; j < checksY; j++) {
                if ((i + j) % 2 == 0) {
                    float checkX = x + i * checkSize;
                    float checkY = y + j * checkSize;
                    float checkW = Math.min(checkSize, w - i * checkSize);
                    float checkH = Math.min(checkSize, h - j * checkSize);
                    SkijaUtil.rectangle(checkX, checkY, checkW, checkH, new Color(200, 200, 200));
                } else {
                    float checkX = x + i * checkSize;
                    float checkY = y + j * checkSize;
                    float checkW = Math.min(checkSize, w - i * checkSize);
                    float checkH = Math.min(checkSize, h - j * checkSize);
                    SkijaUtil.rectangle(checkX, checkY, checkW, checkH, new Color(255, 255, 255));
                }
            }
        }
    }

    public static void renderColorPickerBox(float x, float y, float w, float h, float hue) {
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                float s = i / w;
                float b = 1f - j / h;
                Color c = Color.getHSBColor(hue, s, b);
                SkijaUtil.pixel(x + i, y + j, c);
            }
        }
    }

    public static void renderHueSlider(float x, float y, float w, float h) {
        for (int i = 0; i < w; i++) {
            float hue = i / w;
            Color c = Color.getHSBColor(hue, 1f, 1f);
            SkijaUtil.rectangle(x + i, y, 1, h, c);
        }
    }

    public static void renderAlphaSlider(float x, float y, float w, float h, float hue) {
        Color baseColor = Color.getHSBColor(hue, 1f, 1f);
        for (int i = 0; i < w; i++) {
            float alpha = i / w;
            Color c = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), (int)(alpha * 255));
            SkijaUtil.rectangle(x + i, y, 1, h, c);
        }
    }
}