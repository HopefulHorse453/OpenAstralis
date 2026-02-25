package cc.astralis.property.properties;

import imgui.type.ImFloat;
import cc.astralis.property.Property;
import lombok.Getter;

public class NumberProperty extends Property<ImFloat> {
    @Getter
    public Float min, max, increment, defaultValue;
    public ImFloat imFloat;

    public NumberProperty(String name, float defaultValue, float min, float max, float increment) {
        super(name, new ImFloat(defaultValue));
        this.defaultValue = defaultValue;
        this.min = min;
        this.max = max;
        this.increment = increment;
        this.imFloat = new ImFloat(defaultValue);
    }

    public void setCalculatedValue(double value, boolean limit) {
        double precision = 1 / (!limit ? increment : 0.01);
        this.setProperty(new ImFloat((float) (Math.round(Math.max(min, Math.min(limit ? max : max + 9999, value)) * precision) / precision)));
    }
}
