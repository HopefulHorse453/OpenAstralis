package cc.astralis.module.impl.combat;

import cc.astralis.Astralis;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.property.properties.BooleanProperty;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.player.Player;

public class AntiBotModule extends Module {
    private static final BooleanProperty IsInTablist = new BooleanProperty("Is In Tablist", true);
    private static final BooleanProperty ValidName = new BooleanProperty("Valid Name", true);
    private static final BooleanProperty NameStartsWith = new BooleanProperty("Name Starts With", true);
    private static final BooleanProperty Age = new BooleanProperty("Age", true);
    private static final BooleanProperty Latency = new BooleanProperty("Latency", true);;

    public AntiBotModule() {
        super(Category.COMBAT);
        this.registerProperties(IsInTablist, ValidName, NameStartsWith, Age, Latency);
    }

    private static final String VALID_USERNAME_REGEX = "^[a-zA-Z0-9_]{1,16}+$";

    public static boolean isBot(Player entityPlayer) {
        final AntiBotModule module = Astralis.getInstance()
                .getModuleManager()
                .getModule(AntiBotModule.class);

        if (module == null || !module.isToggled()) {
            return false;
        }

        if (entityPlayer == null) return true;
        PlayerInfo info = mc.getConnection().getPlayerInfo(entityPlayer.getUUID());

        if (Age.getProperty() && entityPlayer.tickCount < 20) return true;

        if (IsInTablist.getProperty() && (info == null || !isInTab(info))) return true;

        if (Latency.getProperty() && info != null && info.getLatency() == 0) return true;

        String name = info != null ? info.getProfile().name() : null;
        if (ValidName.getProperty() &&
                (name == null || !name.matches(VALID_USERNAME_REGEX) || name.contains(" ") || name.startsWith("§") || name.contains("NPC"))) return true;

        return info == null;
    }

    public static boolean isBot(PlayerInfo playerInfo) {
        final AntiBotModule module = Astralis.getInstance()
                .getModuleManager()
                .getModule(AntiBotModule.class);

        if (module == null || !module.isToggled()) {
            return false;
        }

        if (playerInfo == null) {
            return true;
        }

        if (IsInTablist.getProperty() && !isInTab(playerInfo)) {
            return true;
        }

        if (NameStartsWith.getProperty() && nameStartsWith(playerInfo, "[NPC] ")) {
            return true;
        }

        return ValidName.getProperty() && !playerInfo.getProfile().name().matches(VALID_USERNAME_REGEX);
    }

    private static boolean isInTab(PlayerInfo player) {
        for (PlayerInfo info : mc.getConnection().getOnlinePlayers()) {
            if (info.getProfile().id().compareTo(player.getProfile().id()) == 0) {
                return true;
            }
        }

        return false;
    }

    private static boolean nameStartsWith(PlayerInfo networkPlayerInfo, String prefix) {
        return networkPlayerInfo.getTabListDisplayName() != null &&
                ChatFormatting.stripFormatting(networkPlayerInfo.getTabListDisplayName().getString()).startsWith(prefix);
    }
}
