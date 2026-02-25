package cc.astralis.property.properties;

import cc.astralis.property.Property;

public class BooleanProperty extends Property<Boolean> {
    public BooleanProperty(String name, Boolean toggled) {
        super(name, toggled);
    }
}
