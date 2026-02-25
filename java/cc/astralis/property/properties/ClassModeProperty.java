package cc.astralis.property.properties;

import cc.astralis.module.SubModule;
import cc.astralis.property.Property;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@Getter
@Setter
public class ClassModeProperty extends Property<SubModule> {
    private final HashMap<String, SubModule> classModes = new HashMap<>();

    public ClassModeProperty(final String name, SubModule... classes) {
        super(name, classes.length > 0 ? classes[0] : null);

        classModes.put("None", new SubModule(getProperty().getParentClass(), "None"));

        for (SubModule mode : classes) {
            classModes.put(mode.getFormatedName(), mode);
        }

        if (getVisible().get()) {

            if (getProperty() != null && getProperty().getParentClass().isToggled()) {
                getProperty().setHooked(true);
                getProperty().setSelected(true);
            }
        }
    }

    @Override
    public void setProperty(SubModule newMode) {
        if (getProperty() != null && getProperty() != newMode || !getVisible().get()) {
            getProperty().setHooked(false);
        }

        super.setProperty(newMode);

        if (newMode.getParentClass().isToggled() && getVisible().get()) {
            newMode.setHooked(true);
        }
    }

    public boolean is(String name) {
        return getProperty().getFormatedName().equalsIgnoreCase(name);
    }
}
