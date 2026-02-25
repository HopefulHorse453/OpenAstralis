package cc.astralis.property.properties;

import imgui.type.ImString;
import cc.astralis.property.Property;
import lombok.Getter;

@Getter
public class InputProperty extends Property<String> {
    private ImString imString;

    public InputProperty(String name) {
        this(name, "");
    }

    public InputProperty(String name, String value) {
        super(name, value);
        this.imString = new ImString(value != null ? value : "", 512);
    }
}
