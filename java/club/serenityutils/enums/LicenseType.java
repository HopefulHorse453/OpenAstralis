package club.serenityutils.enums;

import lombok.Getter;

public enum LicenseType {
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    LIFETIME("Lifetime");

    @Getter
    private final String name;

    LicenseType(String name) {
        this.name = name;
    }

    public static LicenseType fromString(String name) {
        for (LicenseType type : values()) {
            if (type.name.equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown license type: " + name);
    }
}
