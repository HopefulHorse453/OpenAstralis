package cc.astralis.ui.notifications;

import cc.astralis.ui.notifications.render.Notification;
import cc.astralis.ui.animations.Animation;
import cc.astralis.ui.animations.Easing;
import cc.astralis.util.math.TimeUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class NotificationInfo {
    public Notification notification;
    public TimeUtil notificationTime;
    private final Animation yAnimation = new Animation(Easing.EASE_IN_OUT_QUAD, 200);
    public int time;

    NotificationInfo() {}

    public static NotificationBuilder builder() {
        return new NotificationBuilder();
    }
}