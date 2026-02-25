package cc.astralis.property.properties;

import cc.astralis.property.Property;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModeProperty extends Property<String> {
    private String[] modes;

    public ModeProperty(String name, String mode, String... modes) {
        super(name, mode);
        this.modes = modes;
    }

    //we use this cause its kind of annoying to write that every time
    public boolean is(String theMode) {
        return getProperty().equalsIgnoreCase(theMode);
    }
}
