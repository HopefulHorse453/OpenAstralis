package cc.astralis.module.impl.combat;

import cc.astralis.component.impl.player.RotationComponent;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.UpdateEvent;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.property.properties.BooleanProperty;
import cc.astralis.property.properties.NumberProperty;
import cc.astralis.util.math.TimeUtil;
import cc.astralis.util.player.RotationUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.phys.AABB;

public class AntiFireBallModule extends Module {
    private final NumberProperty range = new NumberProperty("Range", 5, 1, 6, 0.1F);
    private final BooleanProperty onedotEight = new BooleanProperty("1.8 Swing Order", false);
    private final NumberProperty hitCoolDown = new NumberProperty("Hit Cool Down", 200, 0, 1000, 1);
    private final NumberProperty minRotationSpeed = new NumberProperty("Min Rotation Speed", 100, 0, 180, 1),
            maxRotationSpeed = new NumberProperty("Max Rotation Speed", 100, 0, 180, 1);

    public LargeFireball target;
    public List<LargeFireball> possibleTargets = new ArrayList<>();
    private final TimeUtil timeUtil = new TimeUtil();

    public AntiFireBallModule() {
        super(Category.COMBAT);
        this.registerProperties(range, onedotEight, hitCoolDown, minRotationSpeed, maxRotationSpeed);
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        this.setSuffix(range.getProperty().toString());

   /*     String renderer = GL11.glGetString(GL11.GL_RENDERER);
        String version = GL11.glGetString(GL11.GL_VERSION);
        String vendor = GL11.glGetString(GL11.GL_VENDOR);
        Astralis.LOGGER.info("Vendor: {}, OpenGL Version {}, renderer {}", vendor, version, renderer);*/

        this.possibleTargets = mc.level.getEntitiesOfClass(LargeFireball.class,
                        new AABB(mc.player.blockPosition()).inflate(range.getProperty().floatValue()),
                        entity -> true).stream()
                .filter(fireball -> fireball.getOwner() != mc.player)
                .sorted(Comparator.comparingDouble(entity -> mc.player.distanceTo(entity)))
                .collect(Collectors.toList());

        if (possibleTargets.isEmpty()) {
            target = null;
        }

        for (LargeFireball fireball : possibleTargets) {
            if (mc.player.distanceTo(fireball) <= range.getProperty().floatValue() && timeUtil.finished(hitCoolDown.getProperty().longValue())) {
                target = fireball;

                RotationComponent.setRotations(RotationUtil.getRotations(target),
                        minRotationSpeed.getProperty().floatValue(),
                        maxRotationSpeed.getProperty().floatValue()
                );

                if (onedotEight.getProperty()) {
                    mc.player.swing(InteractionHand.MAIN_HAND);
                }

                mc.gameMode.attack(mc.player, target);

                if (!onedotEight.getProperty()) {
                    mc.player.swing(InteractionHand.MAIN_HAND);
                }
                timeUtil.reset();
            }
        }
    }
}