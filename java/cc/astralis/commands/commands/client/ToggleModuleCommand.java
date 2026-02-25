package cc.astralis.commands.commands.client;

import cc.astralis.Astralis;
import cc.astralis.commands.Command;
import cc.astralis.module.Module;
import cc.astralis.util.render.ChatUtil;

public class ToggleModuleCommand extends Command {
    public ToggleModuleCommand() {
        super(new String[]{ "toggle", "t" }, "Toggles Modules (.toggle <module>)");
    }

    @Override
    public void execute(String[] args, String message) {
        String[] words = message.split(" ");

        if (args.length < 1) {
            ChatUtil.print("Wrong Usage .t <Module>");
            return;
        }

        Module module = Astralis.getInstance().getModuleManager().getModuleBySimplifiedName(words[1]);
        if (module != null) {
            module.toggle();
            ChatUtil.print("Toggled " + module.getName() + " module.");
        } else {
            ChatUtil.print("Module not found.");
        }
    }
}
