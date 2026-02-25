package cc.astralis.util.player;

import cc.astralis.interfaces.IAccess;
import java.util.regex.Pattern;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.scores.PlayerTeam;

public class TeamUtil implements IAccess {
    // dw about it ;3
    public static boolean isSameTeam(PlayerInfo entry) {
        PlayerTeam entryTeam = entry.getTeam();
        PlayerTeam playerTeam = mc.player.getTeam();

        if (entryTeam != null && playerTeam != null) {
            return entryTeam.isAlliedTo(playerTeam);
        }

        Component targetDisplayName = entry.getTabListDisplayName();
        Component clientDisplayName = mc.player.getDisplayName();

        if (clientDisplayName == null || targetDisplayName == null) {
            return false;
        }

        return checkName(clientDisplayName, targetDisplayName) ||
                checkPrefix(targetDisplayName, clientDisplayName);
    }

    public static boolean isSameTeam(LivingEntity entity) {
        if (mc.player.isAlliedTo(entity)) {
            return true;
        }

        Component clientDisplayName = mc.player.getDisplayName();
        Component targetDisplayName = entity.getDisplayName();

        if (clientDisplayName == null || targetDisplayName == null) {
            return false;
        }

        return checkName(clientDisplayName, targetDisplayName) ||
                checkPrefix(targetDisplayName, clientDisplayName); /* ||
                checkArmor(entity); */
    }

    private static boolean checkName(Component clientDisplayName, Component targetDisplayName) {
        Style targetStyle = targetDisplayName.getStyle();
        Style clientStyle = clientDisplayName.getStyle();

        return targetStyle.getColor() != null &&
                clientStyle.getColor() != null &&
                targetStyle.getColor().equals(clientStyle.getColor());
    }

    private static boolean checkPrefix(Component targetDisplayName, Component clientDisplayName) {
        String targetName = stripMinecraftColorCodes(targetDisplayName.getString());
        String clientName = stripMinecraftColorCodes(clientDisplayName.getString());
        String[] targetSplit = targetName.split(" ");
        String[] clientSplit = clientName.split(" ");

        return targetSplit.length > 1 &&
                clientSplit.length > 1 &&
                targetSplit[0].equals(clientSplit[0]);
    }

    private static boolean checkArmor(LivingEntity entity) {
        if (!(entity instanceof Player)) {
            return false;
        }

        Player playerEntity = (Player) entity;
        return (matchesArmorColor(playerEntity, 3)) || (matchesArmorColor(playerEntity, 2));
    }

    private static final Pattern COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");

    public static String stripMinecraftColorCodes(String input) {
        return COLOR_PATTERN.matcher(input).replaceAll("");
    }

    private static boolean matchesArmorColor(Player player, int armorSlot) {
 /*       PlayerInventory inventory = player.getInventory();
        EntityEquipment equipment = ((PlayerInventoryAccessor)inventory).astralis$getEquipment();

        var ownStack = mc.player.getInventory().getArmorStack(armorSlot);
        var otherStack = equipment.get(armorSlot);

        Integer ownColor = getArmorColor(ownStack);
        Integer otherColor = getArmorColor(otherStack);
*/
        return true;
    }

    private static Integer getArmorColor(ItemStack otherStack) {
        if (otherStack.is(ItemTags.DYEABLE)) {
            return DyedItemColor.getOrDefault(otherStack, -6265536);
        } else {
            return null;
        }
    }
}
