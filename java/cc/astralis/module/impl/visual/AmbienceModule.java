package cc.astralis.module.impl.visual;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.TickEvent;
import cc.astralis.event.events.impl.network.PacketEvent;
import cc.astralis.event.types.EventModes;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.property.properties.BooleanProperty;
import cc.astralis.property.properties.ColorProperty;
import cc.astralis.property.properties.NumberProperty;
import java.awt.*;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;

// nigger why arent you following proper naming?
// and proper spacing?????????????????????????????????????????????????????????????????????
// im not fixing your shit variable naming fuck you
public class AmbienceModule extends Module {
    private final BooleanProperty tom = new BooleanProperty("Time Changer", true);
    private final NumberProperty time = new NumberProperty("Time", 21000, 0, 23000, 1);
    public final BooleanProperty fogshi = new BooleanProperty("Fog Modifier", true);
    public final BooleanProperty sky = new BooleanProperty("Sky Modifier", true);
    public final BooleanProperty customGradient = new BooleanProperty("Custom Gradient", true);
    public final ColorProperty gradientTop = new ColorProperty("Gradient Top", new Color(0, 0, 255)).setVisible(customGradient::getProperty);
    public final ColorProperty gradientBottom = new ColorProperty("Gradient Bottom", new Color(0, 0, 128)).setVisible(customGradient::getProperty);
    public final ColorProperty skycolor = new ColorProperty("Sky Color", Color.cyan).setVisible(sky::getProperty);
    public final ColorProperty fogcolor = new ColorProperty("Fog Color", Color.cyan).setVisible(fogshi::getProperty);
    public final NumberProperty fogstart = new NumberProperty("Fog Start", 0, 0, 256, 1).setVisible(fogshi::getProperty);
    public final NumberProperty fogend = new NumberProperty("Fog End", 64, 10, 256, 1).setVisible(fogshi::getProperty);
    public final NumberProperty environmentalStart = new NumberProperty("Environmental Start", 0, 0, 256, 1).setVisible(fogshi::getProperty);
    public final NumberProperty environmentalEnd = new NumberProperty("Environmental End", 64, 10, 256, 1).setVisible(fogshi::getProperty);
    public final NumberProperty gradientTopAlpha = new NumberProperty("Top Alpha", 1.0f, 0.0f, 1.0f, 0.01f)
            .setVisible(customGradient::getProperty);
    public final NumberProperty gradientBottomAlpha = new NumberProperty("Bottom Alpha", 1.0f, 0.0f, 1.0f, 0.01f)
            .setVisible(customGradient::getProperty);
    public final BooleanProperty customBlur = new BooleanProperty("Custom Blur", false);
    public final NumberProperty blurStrength = new NumberProperty("Blur Strength", 5, 0, 20, 1).setVisible(customBlur::getProperty);

    private long originalTime;

    public AmbienceModule() {
        super(Category.VISUAL);
        registerProperties(tom, time, fogshi, fogcolor, fogstart, environmentalStart,
                environmentalEnd, fogend, sky, skycolor, customGradient, gradientTop, gradientBottom,
                gradientTopAlpha,gradientBottomAlpha, customBlur,blurStrength);
    }

    @Override
    public void onEnable() {
        if (mc.level != null)
            originalTime = mc.level.getGameTime();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (mc.level != null)
            mc.level.getLevelData().setDayTime(originalTime);
        super.onDisable();
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (event.getEventMode() == EventModes.RECEIVE && event.getPacket() instanceof ClientboundSetTimePacket) {
            originalTime = ((ClientboundSetTimePacket) event.getPacket()).dayTime();
            event.setCancelled(true);
            mc.level.getLevelData().setDayTime(time.getProperty().longValue());
        }
    }

    @EventTarget
    public void onTick(TickEvent e) {
        if (tom.getProperty() && mc.level != null) {
            mc.level.getLevelData().setDayTime(time.getProperty().longValue());
        }
    }
}



