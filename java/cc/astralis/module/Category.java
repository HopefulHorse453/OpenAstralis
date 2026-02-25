package cc.astralis.module;

import lombok.Getter;

@Getter
public enum Category {
    COMBAT("Combat"),
    MOVEMENT("Movement"),
    WORLD("World"),
    PLAYER("Player"),
    VISUAL("Visual"),
    EXPLOIT("Exploit");

    private final String name;

    Category(String name) {
        this.name = name;
    }
}
