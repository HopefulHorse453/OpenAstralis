package cc.astralis.component.impl.ui;

import cc.astralis.Astralis;
import cc.astralis.component.Component;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.render.Render2DEvent;
import cc.astralis.event.events.impl.render.ShaderEvent;
import cc.astralis.module.impl.visual.NotificationsModule;
import cc.astralis.ui.notifications.NotificationInfo;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Getter
public class NotificationComponent extends Component {
    private static final List<NotificationInfo> notifications = new ArrayList<>();
    private final List<Runnable> shaderQueue = new ArrayList<>();

    public static void addNotification(NotificationInfo notificationInfo) {
        // IDFK HOW TO PLAY SOUNDS.
       /* Identifier soundId = IdentifierAccessor.createIdentifier("astralis", "dancing-random");


        SoundInstance sound = new SoundInstance(
                soundId,
                SoundCategory.MASTER,
                1.0F,
                1.0F,
                Vec3d.ZERO,
                true,                      // Streaming? (false for small sounds)
                0,                         // Repeat delay (0 = no repeat)
                1,                         // Attenuation (1 = distance-based volume drop)
                0.0, 0.0, 0.0,
                false
        );

        mc.getSoundManager().play(sound);*/
        notifications.add(notificationInfo);
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (!Astralis.getInstance().getModuleManager().getModule(NotificationsModule.class).isToggled())
            return;

        shaderQueue.clear();

        int increment = 40;
        int targetY = 5;

        Iterator<NotificationInfo> iterator = notifications.iterator();
        while (iterator.hasNext()) {
            NotificationInfo notificationInfo = iterator.next();

            notificationInfo.getYAnimation().run(targetY);

            float y = (float) notificationInfo.getYAnimation().getValue();
            long elapsed = notificationInfo.getNotificationTime().getElapsedTime();
            long time = notificationInfo.getTime();

            notificationInfo.getNotification().renderNotification(y, elapsed, time);
            shaderQueue.add(() -> notificationInfo.getNotification().renderNotificationShader(y, elapsed, time));

            // hard code god.
            if (notificationInfo.getNotificationTime().finished(notificationInfo.time + 400)) {
                iterator.remove();
            }

            targetY += increment;
        }
    }

    @EventTarget
    public void onShader(ShaderEvent event) {
        for (Runnable r : shaderQueue) r.run();
        shaderQueue.clear();
    }
}
