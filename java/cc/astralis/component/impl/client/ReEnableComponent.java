package cc.astralis.component.impl.client;

import cc.astralis.Astralis;
import cc.astralis.component.Component;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.UpdateEvent;
import cc.astralis.event.events.impl.network.PacketEvent;
import cc.astralis.event.events.impl.render.Render2DEvent;
import cc.astralis.event.types.EventModes;
import cc.astralis.module.impl.movement.SpeedModule;
import cc.astralis.util.math.TimeUtil;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;

public class ReEnableComponent extends Component {
    private final TimeUtil timeUtil = new TimeUtil();
    private boolean didDisable;

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (event.getEventMode() == EventModes.SEND || !Astralis.getInstance().getModuleManager().getModule(SpeedModule.class).isToggled())
            return;

        if (event.getPacket() instanceof ClientboundPlayerPositionPacket) {
            Astralis.getInstance().getModuleManager().getModule(SpeedModule.class).toggle();
            didDisable = true;
            timeUtil.reset();
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (didDisable && timeUtil.finished(1000))
            didDisable = false;
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
       /* if (!didDisable)
            return;

        // just a notice having that timer run continuously might not be the best thing.
        String text = "Disabled Due To Flag";
        Window window = mc.getWindow();

       *//* RenderUtil.drawGradientString(roboto_bold_11, text,
                (window.getScaledWidth() - product_regular_10.getStringWidth(text)) / 2,
                (window.getScaledHeight() / 2) + 5,
                Astralis.getInstance().getFirstColor(),
                Astralis.getInstance().getSecondColor(), true);*//*
        roboto_bold_11.drawCenteredStringWithShadow(text,
                (float) (window.getScaledWidth()) / 2,
                ((float) window.getScaledHeight() / 2) + 5,
                Astralis.getInstance().getFirstColor());*/
    }
}
