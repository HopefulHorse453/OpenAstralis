package cc.astralis.commands;

import cc.astralis.Astralis;
import cc.astralis.commands.commands.client.*;
import cc.astralis.commands.commands.player.HclipCommand;
import cc.astralis.commands.commands.player.SelfBanCommand;
import cc.astralis.commands.commands.player.TpCommand;
import cc.astralis.commands.commands.player.VclipCommand;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.input.ChatInputEvent;
import cc.astralis.manager.Manager;
import cc.astralis.util.render.ChatUtil;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public final class CommandManager extends Manager<Command> {
    public CommandManager() {
        Astralis.getInstance().getEventManager().register(this);
    }

    @EventTarget
    public void onChatInputEvent(ChatInputEvent event) {
        String message = event.getInput();

        if (!message.startsWith("."))
            return;

        message = message.substring(1);
        final String[] args = message.split(" ");

        final AtomicBoolean commandFound = new AtomicBoolean(false);

        try {
            String finalMessage = message;
            getObjects().stream().filter(command ->
                            Arrays.stream(command.getExpressions())
                                    .anyMatch(expression -> expression.equalsIgnoreCase(args[0])))
                    .forEach(cmd -> {
                        commandFound.set(true);
                        cmd.execute(args, finalMessage);
                    });
        } catch (final Exception ex) {
            ex.printStackTrace();
        }

        if (!commandFound.get())
            ChatUtil.print("Not found.");

        event.setCancelled(true);
    }

    public void registerCommands() {
        register(
                new BindCommand(), new ToggleModuleCommand(),
                new FriendCommand(), new HelpCommand(),
                new VclipCommand(), new HclipCommand(),
                new IrcCommand(), new ConfigCommand(),
                new IgnCommand(), new TpCommand(),
                new HideCommand(), new SelfBanCommand()
        );
    }
}
