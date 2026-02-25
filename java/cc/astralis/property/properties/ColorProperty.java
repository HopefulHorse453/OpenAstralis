package cc.astralis.property.properties;

import cc.astralis.property.Property;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;

@Getter
@Setter
public class ColorProperty extends Property<Color> {
    private float hue = 0, saturation = 1, brightness = 1;

    public ColorProperty(String name, Color color) {
        super(name, color);
        this.setColor(color);
    }

    public void setColor(Color color) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        hue = hsb[0];
        saturation = hsb[1];
        brightness = hsb[2];
    }

    public void setColor(float hue, float saturation, float brightness, float alpha) {
        this.hue = hue;
        this.saturation = saturation;
        this.brightness = brightness;
    }
}
