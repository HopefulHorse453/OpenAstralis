package cc.astralis.module.impl.combat;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.network.PacketEvent;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.module.impl.combat.velocity.*;
import cc.astralis.property.properties.ClassModeProperty;

public class VelocityModule extends Module {
    // Club velocity code is in MixinEntityVelocityUpdateS2CPacket.java.
    public final ClassModeProperty mode = new ClassModeProperty("Mode",
            new AirVelocity(this), new CancelVelocity(this),
            new IntaveVelocity(this),
            new NormalVelocity(this), new ReverseVelocity(this),
            new VulcanVelocity(this)/*, new WatchdogAirVelocity(this),
            new WatchdogFullVelocity(this)*/, new MMCVelocity(this),
            new DelayVelocity(this), new BufferVelocity(this),
            new ModernWatchdogVelocity(this), new GrimVelocity(this),
            new JumpVelocity(this), new ReducePacketVelocity(this)
    );

    public VelocityModule() {
        super(Category.COMBAT);
        this.registerProperty(mode);
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        this.setSuffix(mode.getProperty().getFormatedName());
    }
}
