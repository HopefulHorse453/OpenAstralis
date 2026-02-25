package cc.astralis.util.player;

public enum ToolType {
    Axe,
    Hoe,
    Shovel,
    Shears,
    Pickaxe,
    Sword;

    public boolean isDigger() {
        return this == Axe || this == Shovel || this == Pickaxe;
    }

    public int getDiggerIndex() {
        if (this == Shovel) return 0;
        if (this == Pickaxe) return 1;
        if (this == Axe) return 2;
        return -1;
    }
}
