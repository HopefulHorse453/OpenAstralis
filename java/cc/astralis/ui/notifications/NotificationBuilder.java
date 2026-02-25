package cc.astralis.ui.notifications;

import cc.astralis.component.impl.ui.NotificationComponent;
import cc.astralis.ui.notifications.render.Notification;
import cc.astralis.util.math.TimeUtil;

public class NotificationBuilder {
    private final NotificationInfo info = new NotificationInfo();

    public NotificationBuilder notification(String subHeader, Notification.NotificationType type) {
        info.notification = new Notification(subHeader, type);
        return this;
    }

    public NotificationBuilder duration(int milliseconds) {
        info.time = milliseconds;
        return this;
    }

    public NotificationInfo build() {
        validate();
        info.notificationTime = new TimeUtil();
        info.notificationTime.reset();

        NotificationComponent.addNotification(info);
        return info;
    }

    private void validate() {
        if (info.notification == null) {
            throw new IllegalArgumentException("Notification content (subHeader and type) must be set.");
        }
        if (info.time <= 0) {
            info.time = 5000;
        }
    }

    public static NotificationBuilder create() {
        return new NotificationBuilder();
    }

    public static void send(String subHeader, Notification.NotificationType type) {
        create()
                .notification(subHeader, type)
                .build();
    }

    public static void send(String subHeader, Notification.NotificationType type, int durationMs) {
        create()
                .notification(subHeader, type)
                .duration(durationMs)
                .build();
    }
}