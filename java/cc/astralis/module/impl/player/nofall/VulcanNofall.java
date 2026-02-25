package cc.astralis.module.impl.player.nofall;

import cc.astralis.Astralis;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.MotionEvent;
import cc.astralis.module.Module;
import cc.astralis.module.SubModule;
import cc.astralis.module.impl.movement.ScaffoldRecodeModule;
import cc.astralis.property.properties.NumberProperty;
import cc.astralis.util.player.PlayerUtil;

public class VulcanNofall extends SubModule {
    private final NumberProperty motion = new NumberProperty("Motion", 10f, 0f, 10f, 1f);

    public VulcanNofall(Module parentClass){
        super(parentClass,"Vulcan");
        this.registerPropertyToParentClass(motion);
    }
    private int fallticks;

    @Override
    public void onDisable(){
        PlayerUtil.setMotionY(mc.player.getDeltaMovement().y());
        super.onDisable();
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (mc.player.onGround()) {
            fallticks = 0;
        }
        if (mc.player.fallDistance - mc.player.getDeltaMovement().y > 3 && !Astralis.getInstance().getModuleManager().getModule(ScaffoldRecodeModule.class).isToggled()) {
            fallticks++;
            if (fallticks > 1) {
                event.setOnGround(true);
            }
            if (fallticks > 2) {
                PlayerUtil.setMotionY(-motion.getProperty().floatValue());
            }
        }
    }

}
