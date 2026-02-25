package cc.astralis.commands.commands.player;

import cc.astralis.commands.Command;
import cc.astralis.util.render.ChatUtil;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.world.entity.player.Player;

import static cc.astralis.interfaces.IAccess.mc;

public class TpCommand extends Command {
    public TpCommand() {
        super(new String[]{ "teleport", "tp" }, "Teleports you to a player or coordinates");
    }

    @Override
    public void execute(String[] args, String message) {
        String[] words = message.split(" ");

        if (words.length < 2) {
            ChatUtil.print("Wrong Usage .tp <x> <y> <z> | .tp <player>");
            return;
        }

        UUID uuid = mc.getPlayerSocialManager().getDiscoveredUUID(words[1]);
        if (uuid != null && uuid != Util.NIL_UUID) {
            Player entity = mc.level.getPlayerByUUID(uuid);
            if (entity != null) {
                mc.player.setPos(entity.position());
                ChatUtil.print("Teleported you to player " + words[1]);
            } else {
                ChatUtil.print("Player not found: " + words[1]);
            }
            return;
        }

        if (words.length < 4) {
            ChatUtil.print("Wrong Usage .tp <x> <y> <z>");
            return;
        }

        try {
            mc.player.setPos(Double.parseDouble(words[1]), Double.parseDouble(words[2]), Double.parseDouble(words[3]));
            ChatUtil.print("Teleported to coordinates: " + Double.parseDouble(words[1]) + ", " + Double.parseDouble(words[2]) + ", " + Double.parseDouble(words[3]));
        } catch (NumberFormatException e) {
            ChatUtil.print("Invalid coordinates.");
        }
    }
}
