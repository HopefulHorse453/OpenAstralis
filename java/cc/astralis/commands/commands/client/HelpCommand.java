package cc.astralis.commands.commands.client;

import cc.astralis.Astralis;
import cc.astralis.commands.Command;
import cc.astralis.util.render.ChatUtil;

public class HelpCommand extends Command {
    public HelpCommand() {
        super(new String[]{ "help", "h"}, "Shows Information And Usages About Other Commands");
    }

    @Override
    public void execute(String[] args, String message) {
        for (Command command : Astralis.getInstance().getCommandManager().getObjects()) {
            ChatUtil.print("Expresion: " + command.getExpressions()[0]);
            ChatUtil.print("Description: " + command.getDescription());
        }
    }
}
