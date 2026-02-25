package cc.astralis.module.impl.combat.velocity;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.input.InputTickEvent;
import cc.astralis.module.Module;
import cc.astralis.module.SubModule;
import cc.astralis.property.properties.NumberProperty;

public class JumpVelocity extends SubModule {
    private final NumberProperty chance = new NumberProperty("Chance", 100, 0, 100, 1);

    public JumpVelocity(Module parent) {
        super(parent, "Jump");
        registerPropertyToParentClass(chance);
    }

    @EventTarget
    public void onInputTick(InputTickEvent event) {
        if (mc.player.hurtTime > 0) {
            if (Math.random() * 100 < chance.getProperty().intValue()) {
                event.up = true;
                if (mc.player.hurtTime == 9) {
                    event.jump = true;
                }
            }
        }
    }
}