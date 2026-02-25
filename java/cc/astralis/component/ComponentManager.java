package cc.astralis.component;

import cc.astralis.Astralis;
import cc.astralis.component.impl.client.BanDetectorComponent;
import cc.astralis.component.impl.client.BedWhiteListComponent;
import cc.astralis.component.impl.client.ReEnableComponent;
import cc.astralis.component.impl.network.BlinkComponent;
import cc.astralis.component.impl.network.PacketLossDetector;
import cc.astralis.component.impl.player.RotationComponent;
import cc.astralis.component.impl.ui.NotificationComponent;
import cc.astralis.component.impl.ui.ProgressBarComponent;
import cc.astralis.manager.Manager;

import java.util.Objects;


public class ComponentManager extends Manager<Component> {
    public void registerComponents() {
        this.register(
                new RotationComponent(),
                new NotificationComponent(),
                new ReEnableComponent(),
                new PacketLossDetector(),
                new BlinkComponent(),
                new ProgressBarComponent(),
                new BanDetectorComponent(),
                new BedWhiteListComponent()
        );

        Astralis.getInstance().getEventManager().register(this);
    }

    @SuppressWarnings("unchecked")
    public <T extends Component> T getComponent(final Class<T> clazz) {
        return (T) this.getBy(module -> Objects.equals(module.getClass(), clazz));
    }
}
