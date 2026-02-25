package cc.astralis.commands.commands.player;

import cc.astralis.commands.Command;
import cc.astralis.util.render.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class VclipCommand extends Command {

    public VclipCommand() {
        super(new String[]{"vclip", "vc"}, "Teleports you vertically by the amount you said.");
    }

    @Override
    public void execute(String[] args, String message) {
        String[] words = message.split(" ");

        if (words.length < 2) {
            ChatUtil.print("Wrong Usage .vclip <amount>");
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

        player.absSnapTo(player.getX(), player.getY() + amount, player.getZ());
        ChatUtil.print("§aVclipped " + amount + " blocks.");
    }
}
