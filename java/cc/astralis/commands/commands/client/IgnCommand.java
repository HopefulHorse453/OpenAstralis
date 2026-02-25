package cc.astralis.commands.commands.client;

import cc.astralis.commands.Command;
import cc.astralis.util.render.ChatUtil;

import static cc.astralis.interfaces.IAccess.mc;

public class IgnCommand extends Command {
    public IgnCommand() {
        super(new String[]{ "ign", "name","n" }, "Shows you your current name and copies it to youre clip board (.ign)");
    }

    @Override
    public void execute(String[] args, String message) {
        mc.keyboardHandler.setClipboard(mc.getUser().getName());
        ChatUtil.print("Your name is: " + mc.getUser().getName());
    }
}
