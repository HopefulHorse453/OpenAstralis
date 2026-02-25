package cc.astralis.module.impl.client.cheaterfinder;

import cc.astralis.Astralis;
import cc.astralis.event.events.impl.game.MotionEvent;
import cc.astralis.ui.notifications.render.Notification;
import cc.astralis.ui.notifications.NotificationBuilder;
import cc.astralis.util.render.ChatUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Check {
    private final String name;
    private PlayerData playerData;
    private int vl;

    public Check(String name) {
        this.name = name;
        this.playerData = null;
        this.vl = 0;
    }

    // there is a better way of doing this like hooking each check to the event bus but its point less if we just use one event.
    public void onMotion(MotionEvent event) { /* w */ }

    public void warn() {
        CheaterFinderModule cheaterFinderModule = Astralis.getInstance().getModuleManager().getModule(CheaterFinderModule.class);
        String message = playerData.name + " failed " + name + " (vl: " + vl + ")";

        vl++;

        if (cheaterFinderModule.sendChatMessage.getProperty())
            ChatUtil.print(message);

        if (cheaterFinderModule.sendNotification.getProperty())
            NotificationBuilder.create()
                    .notification(message, Notification.NotificationType.WARNING)
                    .duration(1000)
                    .build();
    }
}
