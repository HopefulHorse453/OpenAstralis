package cc.astralis.property.properties;

import cc.astralis.property.Property;

import java.util.HashMap;

/**
 * @author Kawase
 * @since 26.10.2025
 */
public class BooleanListProperty extends Property<HashMap<String, BooleanListProperty.BooleanEntry>> {
    private final HashMap<String, BooleanEntry> booleanMap = new HashMap<>();

    public BooleanListProperty(String name, BooleanEntry... booleanEntries) {
        super(name, new HashMap<>());

        for (BooleanEntry entry : booleanEntries) {
            booleanMap.put(entry.name(), entry);
        }

        setProperty(booleanMap);
    }

    public record BooleanEntry(String name, Boolean value) {
        /* w */
    }

    public BooleanEntry getEntry(String entryName) {
        return booleanMap.get(entryName);
    }

    public void setValueToEntry(String entryName, boolean newValue) {
        BooleanEntry old = booleanMap.get(entryName);

        if (old != null) {
            BooleanEntry updated = new BooleanEntry(old.name(), newValue);
            booleanMap.put(entryName, updated);
            setProperty(booleanMap);
        }
    }

    public HashMap<String, BooleanEntry> getEntries() {
        return new HashMap<>(booleanMap);
    }
}
