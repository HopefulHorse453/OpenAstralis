package cc.astralis.module.impl.visual;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.MotionEvent;
import cc.astralis.event.events.impl.render.Render3DEvent;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.property.properties.NumberProperty;
import cc.astralis.ui.animations.Animation;
import cc.astralis.ui.animations.Easing;
import cc.astralis.util.render.Render3DUtil;
import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.world.phys.Vec3;

public class JumpCirclesModule extends Module {
    private final NumberProperty circleDuration = new NumberProperty("Circle Duration", 500, 0, 10000, 1);
    private final NumberProperty maxRadius = new NumberProperty("Max Radius", 3.0f, 0.0f, 10.0f, 0.1f);

    private final ArrayList<Circle> circles = new ArrayList<>();
    private boolean didAddCircle = false;

    public JumpCirclesModule() {
        super(Category.VISUAL);
        this.registerProperties(circleDuration, maxRadius);
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (mc.player.onGround()) {
            if (!didAddCircle) {
                Animation anim = new Animation(Easing.EASE_OUT_BACK, circleDuration.getProperty().longValue());
                anim.setStartPoint(0f);
                anim.setEndPoint(maxRadius.getProperty().floatValue());
                anim.reset();
                circles.add(new Circle(mc.player.position(), anim));
                didAddCircle = true;
            }
        } else {
            didAddCircle = false;
        }
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        Iterator<Circle> it = circles.iterator();

        while (it.hasNext()) {
            Circle circle = it.next();

            if (!circle.poppedIn) {
                circle.animation.run(circle.animation.getEndPoint());
                if (circle.animation.isFinished()) {
                    circle.poppedIn = true;
                    double current = circle.animation.getValue();
                    circle.animation.setStartPoint(current);
                    circle.animation.setEndPoint(0d);
                    circle.animation.reset();
                }
            } else {
                circle.animation.run(circle.animation.getEndPoint());
                if (circle.animation.isFinished()) {
                    it.remove();
                    continue;
                }
            }

            float radius = (float) circle.animation.getValue();

            Render3DUtil.drawCircleAt(event.getMatricies(), circle.position, radius, 225);
        }
    }

    private static class Circle {
        final Vec3 position;
        final Animation animation;
        boolean poppedIn = false;

        Circle(Vec3 position, Animation animation) {
            this.position = position;
            this.animation = animation;
        }
    }
}
