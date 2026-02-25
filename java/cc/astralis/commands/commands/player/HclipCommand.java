package cc.astralis.commands.commands.player;

import cc.astralis.commands.Command;
import cc.astralis.util.render.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class HclipCommand extends Command {

    public HclipCommand() {
        super(new String[]{"hclip", "hc"}, "Teleports you horizontally forward/backward.");
    }

    @Override
    public void execute(String[] args, String message) {
        String[] words = message.split(" ");

        if (words.length < 2) {
            ChatUtil.print("Wrong Usage .hclip <amount>");
            return;
        }

        String input = words[1];
        double amount;

        try {
            amount = Double.parseDouble(input);
        } catch (NumberFormatException e) {
            ChatUtil.print("Invalid number: " + input);
            return;
        }

        if (Math.abs(amount) > 10) {
            ChatUtil.print("§cAmount too large. Max ±10 blocks.");
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null) {
            ChatUtil.print("§cPlayer not found.");
            return;
        }

        float yaw = player.getYRot() * (float) Math.PI / 180f;
        double offsetX = -Math.sin(yaw) * amount;
        double offsetZ = Math.cos(yaw) * amount;

        player.absSnapTo(player.getX() + offsetX, player.getY(), player.getZ() + offsetZ);
        ChatUtil.print("§aHclipped " + amount + " blocks.");
    }
}
