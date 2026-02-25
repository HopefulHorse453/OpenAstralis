package cc.astralis.module.impl.client;

import cc.astralis.Astralis;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.network.BackendMessageEvent;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.util.render.ChatUtil;

public class IrcModule extends Module {

    public IrcModule() {
        super(Category.VISUAL);
    }

    @EventTarget
    public void onBackendMessage(BackendMessageEvent event) {
        mc.gui.getChat().addMessage(ChatUtil.translateToGradient("IRC",
                Astralis.getInstance().getFirstColor().getRGB(), Astralis.getInstance().getSecondColor().getRGB())
                .append(" » " + event.getMessage())
        );
    }
}
