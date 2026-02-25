package club.serenityutils.clientprofile;

import lombok.Getter;

@Getter
public enum ClientProfileType {
    ASTRALIS;

    private final String name;

    ClientProfileType() {
        this.name = capitalize(name());
    }

    private static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    public static ClientProfileType fromString(String name) {
        for (ClientProfileType type : values())
            if (type.name.equalsIgnoreCase(name)) return type;

        throw new IllegalArgumentException("Unknown client type: " + name);
    }
}
