package cc.astralis.ui.screens.clickgui.astralis.impl.display.property;

import cc.astralis.font.UIFont;
import cc.astralis.font.FontManager;
import cc.astralis.property.properties.ColorProperty;
import cc.astralis.ui.screens.clickgui.astralis.Component;
import cc.astralis.ui.screens.clickgui.astralis.impl.display.ModuleDisplay;
import cc.astralis.util.render.RenderUtil;
import cc.astralis.skija.utils.SkijaUtil;

import java.awt.*;

public class ColorDisplay extends Component {
    private final ColorProperty property;

    private static final int HUE_SLIDER_HEIGHT = 10;
    private float selectedColorX, selectedColorY;
    private float selectedHue = 0f;
    private boolean mouseHeld, changingColor, changingHue;

    private static final int PICKER_WIDTH = 80, ENABLED_PADDING = 3;

    public ColorDisplay(final ColorProperty property, final float width, final float height) {
        this.property = property;
        setWidth(width);
        setHeight(height);
    }

    @Override
    public void render(final float x, final float y, final float mouseX, final float mouseY) {
        setX(x);
        setY(y);

        final UIFont uiFont = FontManager.getFont("Sf-Ui", 10);
        uiFont.drawStringWithShadow(property.getName(), x + ModuleDisplay.PADDING, y + ModuleDisplay.PROPERTY_PADDING, ModuleDisplay.DESCRIPTION);

        final float enabledSize = super.getHeight() - (ENABLED_PADDING * 2);
        SkijaUtil.roundedRectangle(x + getWidth() - (enabledSize + ModuleDisplay.PADDING), y + ENABLED_PADDING, enabledSize, enabledSize, 2, property.getProperty());

        float pickerX = getX() - 1 + ModuleDisplay.PROPERTY_PADDING * 2;
        float pickerY = getY() + super.getHeight();

        renderColorPickerBox(pickerX, pickerY, PICKER_WIDTH, PICKER_WIDTH, selectedHue);

        SkijaUtil.roundedRectangle(
                pickerX + selectedColorX - 4,
                pickerY + selectedColorY - 4,
                8, 8, 4,
                new Color(255, 255, 255, 200)
        );

        float sliderY = pickerY + PICKER_WIDTH + ModuleDisplay.PROPERTY_PADDING;
        renderHueSlider(pickerX, sliderY, PICKER_WIDTH, HUE_SLIDER_HEIGHT);

        float hueX = pickerX + selectedHue * PICKER_WIDTH;
        SkijaUtil.roundedRectangle(hueX - 1, sliderY - 2, 2, HUE_SLIDER_HEIGHT + 4, 1, Color.WHITE);

        if (mouseHeld) {
            handleColorPicking(mouseX, mouseY);
        }
    }

    @Override
    public void mouseClicked(final double mouseX, final double mouseY, final int button) {
        mouseHeld = true;

        float pickerX = getX() - 1 + ModuleDisplay.PROPERTY_PADDING * 2;
        float pickerY = getY() + super.getHeight();
        if (RenderUtil.isHovered(mouseX, mouseY, pickerX, pickerY, PICKER_WIDTH, PICKER_WIDTH)) {
            changingColor = true;
        } else if (RenderUtil.isHovered(mouseX, mouseY, pickerX, pickerY + PICKER_WIDTH + ModuleDisplay.PROPERTY_PADDING, PICKER_WIDTH, HUE_SLIDER_HEIGHT)) {
            changingHue = true;
        }
    }

    @Override
    public void mouseReleased(final double mouseX, final double mouseY, final int button) {
        mouseHeld = changingColor = changingHue = false;
    }

    @Override
    public float getHeight() {
        return super.getHeight() + (ModuleDisplay.PROPERTY_PADDING * 2) + PICKER_WIDTH + HUE_SLIDER_HEIGHT;
    }

    private void handleColorPicking(double mouseX, double mouseY) {
        float pickerX = getX() - 1 + ModuleDisplay.PROPERTY_PADDING * 2;
        float pickerY = getY() + super.getHeight();

        if (changingColor) {
            mouseX = Math.clamp(mouseX, pickerX, pickerX + PICKER_WIDTH);
            mouseY = Math.clamp(mouseY, pickerY, pickerY + PICKER_WIDTH);
            selectedColorX = (float) (mouseX - pickerX);
            selectedColorY = (float) (mouseY - pickerY);

            float saturation = selectedColorX / PICKER_WIDTH;
            float brightness = 1.0f - selectedColorY / PICKER_WIDTH;

            property.setProperty(Color.getHSBColor(selectedHue, saturation, brightness));
        } else if (changingHue) {
            mouseX = Math.clamp(mouseX, pickerX, pickerX + PICKER_WIDTH);

            selectedHue = (float) (mouseX - pickerX) / PICKER_WIDTH;
            selectedHue = Math.max(0f, Math.min(1f, selectedHue));

            float saturation = selectedColorX / PICKER_WIDTH;
            float brightness = 1.0f - selectedColorY / PICKER_WIDTH;

            property.setProperty(Color.getHSBColor(selectedHue, saturation, brightness));
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
}
