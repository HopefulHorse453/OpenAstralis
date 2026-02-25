package cc.astralis.module;

import cc.astralis.Astralis;
import cc.astralis.event.events.impl.client.SuffixChangeEvent;
import cc.astralis.event.events.impl.client.ToggleModuleEvent;
import cc.astralis.interfaces.Fonts;
import cc.astralis.interfaces.IAccess;
import cc.astralis.module.impl.visual.NotificationsModule;
import cc.astralis.ui.notifications.render.Notification;
import cc.astralis.ui.notifications.NotificationBuilder;
import cc.astralis.property.Property;
import cc.astralis.property.properties.ClassModeProperty;
import cc.astralis.property.properties.TextProperty;
import cc.astralis.protection.Flags;
import cc.astralis.util.Data;
import club.serenityutils.modules.ModuleMetaData;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.ChatFormatting;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@Setter
public class Module extends Data implements IAccess, Fonts {
    public ModuleMetaData moduleMetaData = new ModuleMetaData("none", "none");
    private final Category category;
    public int keyCode;
    private final List<Property<?>> propertyList = new ArrayList<>();

    private String suffix, lastSuffix;
    private boolean toggled, hidden;

    protected Module(Category category) {
/*
        this.moduleMetaData = new ModuleMetaData(name, description + (description.endsWith(".") ? "" : "."));
*/
        this.category = category;
        this.keyCode = 0;
    }

    protected Module(Category category, int keyCode) {
/*
        this.moduleMetaData = new ModuleMetaData(name, description + (description.endsWith(".") ? "" : "."));
*/
        this.category = category;
        this.keyCode = keyCode;
    }

    public void registerProperty(Property<?> property) {
        propertyList.add(property);
    }

    public void registerProperties(Property<?>... properties) {
        propertyList.addAll(Arrays.asList(properties));
    }

    public void setToggled(boolean toggled) {
        if ((Flags.isNotAuthenticated ||
                !"gud boy".equals(Flags.authStatus) ||
                !Flags.authPacketSent ||
                Flags.user.getUid() == 512383 || Flags.user.getName().equalsIgnoreCase("fag") || !Flags.firstThreadRunning || !Flags.secondThreadRunning ||
                !Flags.keepAliveWorking || (Flags.didDisconnect && !Flags.didReconnect && Flags.reconnectTime.finished(10000)))) {
            // crash
            try {
                Field f = Unsafe.class.getDeclaredField("theUnsafe");
                f.setAccessible(true);
                Unsafe unsafe = (Unsafe) f.get(null);

                long corruptValue = ThreadLocalRandom.current().nextLong();
                long randomAddress = ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
                int haltCode = ThreadLocalRandom.current().nextInt(1, 256);

                unsafe.putLong(Thread.currentThread(), 8L, corruptValue);
                unsafe.putAddress(randomAddress, 0);
                Runtime.getRuntime().halt(haltCode);

            } catch (Throwable ignored) {
                for (long l = Long.MIN_VALUE; l < Long.MAX_VALUE; ++l) {
                    --l;
                }
            }
        }

        this.toggled = toggled;
        Astralis.getInstance().getEventManager().call(new ToggleModuleEvent());

        if (isToggled()) {
            if (Astralis.getInstance().getModuleManager().getModule(NotificationsModule.class) != null &&
                    Astralis.getInstance().getModuleManager().getModule(NotificationsModule.class).displayNotificationOnToggle.getProperty())
                NotificationBuilder.create()
                        .notification("Enabled " + getName(), Notification.NotificationType.INFO)
                        .duration(1000)
                        .build();

            onEnable();
        } else {
            if (Astralis.getInstance().getModuleManager().getModule(NotificationsModule.class) != null &&
                    Astralis.getInstance().getModuleManager().getModule(NotificationsModule.class).displayNotificationOnToggle.getProperty())
                NotificationBuilder.create()
                        .notification("Disabled " + getName(), Notification.NotificationType.INFO)
                        .duration(1000)
                        .build();

            // dad **curse of vanish** ahhh comment
            // we buy the milk
            onDisable();
        }
    }

    private void setSubModesHookState(boolean state) {
        for (Property<?> property : propertyList) {
            if (property instanceof ClassModeProperty classModeProperty) {
                final SubModule subModule = classModeProperty.getProperty();
                if (subModule != null && subModule.getParentClass().equals(this)) {
                    subModule.setHooked(state);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <P extends Property<?> /* higher bound */> P getPropertyByName(String name) {
        return (P) propertyList.stream().filter(property -> property.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public TextProperty infoLine(String text) {
        return new TextProperty(text);
    }

    public void toggle() {
        setToggled(!isToggled());
    }

    public void onEnable() {
        setSubModesHookState(true);
        Astralis.getInstance().getEventManager().register(this);
    }

    public void onDisable() {
        setSubModesHookState(false);
        Astralis.getInstance().getEventManager().unregister(this);
    }

    public String getDisplayName(boolean doesHaveSuffix) {
        String idk = getName();

        if (suffix != null) {
            idk += doesHaveSuffix ? getPreFix() + getSuffix() : "";
        }

        return idk;
    }

    public String getDesc() {
        return moduleMetaData.getDescription();
    }

    // add crash.
    public String getName() {
        return moduleMetaData.getName();
    }

    public void setSuffix(String suffix) {
        if (suffix == null && this.suffix != null || suffix != null && !suffix.equals(this.suffix)) {
            // Fire event with old + new suffix
            Astralis.getInstance().getEventManager().call(new SuffixChangeEvent());
        }

        this.lastSuffix = this.suffix;
        this.suffix = suffix;
    }


    private String getPreFix() {
        //todo: make this have multiple stuff like -, # and so on
        return ChatFormatting.GRAY +  " ";
    }
}
