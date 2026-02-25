package cc.astralis.module.impl.movement;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.MoveEvent;
import cc.astralis.event.events.impl.game.TickEvent;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.property.properties.BooleanProperty;
import cc.astralis.property.properties.NumberProperty;
import cc.astralis.util.player.MoveUtil;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

public class ElytraFlightModule extends Module {
    private final NumberProperty speed = new NumberProperty("Speed", 1.5f, 0.1f, 5.0f, 0.1f);
    private final NumberProperty verticalSpeed = new NumberProperty("Vertical Speed", 0.8f, 0.1f, 2.0f, 0.1f);
    private final BooleanProperty onlyWithElytra = new BooleanProperty("Only With Elytra", true);

    public ElytraFlightModule() {
        super(Category.MOVEMENT);
        this.registerProperties(speed, verticalSpeed, onlyWithElytra);
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (!canFly()) return;
        mc.options.keyShift.setDown(false);
    }

    @EventTarget
    public void onMove(MoveEvent event) {
        if (!canFly()) return;

        float moveSpeed = speed.getProperty().floatValue();

        double yawRad = Math.toRadians(mc.player.getYRot());

        int moveForward = (mc.options.keyUp.isDown() ? 1 : 0) - (mc.options.keyDown.isDown() ? 1 : 0);
        int moveStrafe = (mc.options.keyRight.isDown() ? -1 : 0) - (mc.options.keyLeft.isDown() ? -1 : 0);
        
        double x, z;
        x = (moveForward * -Math.sin(yawRad) + moveStrafe * Math.cos(yawRad)) * moveSpeed;
        z = (moveForward * Math.cos(yawRad) + moveStrafe * Math.sin(yawRad)) * moveSpeed;

        double y = 0;
        if (mc.options.keyJump.isDown()) {
            y = verticalSpeed.getProperty().floatValue();
        } else if (InputConstants.isKeyDown(mc.getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
            y -= verticalSpeed.getProperty().floatValue();
        }

        event.setX(x);
        event.setY(y);
        event.setZ(z);
    }

    @Override
    public void onEnable() {
        if (canFly() && !mc.player.isFallFlying())
            mc.player.startFallFlying();

        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (mc.player != null)
            MoveUtil.stop();
        super.onDisable();
    }

    private boolean canFly() {
        if (mc.player == null) return false;

        if (onlyWithElytra.getProperty()) {
            ItemStack chest = mc.player.getItemBySlot(EquipmentSlot.CHEST);
            if (chest == null || chest.getItem() != Items.ELYTRA) return false;
        }

        return mc.player.isFallFlying();
    }
}
