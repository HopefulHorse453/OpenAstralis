package cc.astralis.util.render;

import cc.astralis.Astralis;
import cc.astralis.interfaces.IAccess;
import cc.astralis.module.impl.client.DebugModule;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class ChatUtil implements IAccess {
    public static void print(Object message) {
        if(mc.player == null || mc.level == null) return;
       mc.player.displayClientMessage(translateToGradient(Astralis.NAME,
                Astralis.getInstance().getFirstColor().getRGB(), Astralis.getInstance().getSecondColor().getRGB())
                .append(" » " + message), false
        );
    }

    public static void printDebug(Object message) {
        if (!Astralis.getInstance().getModuleManager().getModule(DebugModule.class).isToggled())
            return;

        print(message);
    }

    public static MutableComponent translateToGradient(String message, int firstColor, int secondColor) {
        MutableComponent text = Component.empty();

        text.append(ChatFormatting.DARK_GRAY + "[");
        for (int i = 0; i < message.length(); i++) {
            float fraction = (float) (message.length() - 1 - i) / (message.length() - 1);
            // weird :3
            Style style = Style.EMPTY.withColor(ColorUtil.interpolateColor(secondColor, firstColor, fraction));
            text.append(Component.literal(String.valueOf(message.charAt(i))).setStyle(style));
        }
        text.append(ChatFormatting.DARK_GRAY + "]");

        return text;
    }
}
