package cc.astralis.module.impl.movement;

import cc.astralis.Astralis;
import cc.astralis.component.impl.player.RotationComponent;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.UpdateEvent;
import cc.astralis.event.events.impl.input.ClickEvent;
import cc.astralis.event.events.impl.input.InputTickEvent;
import cc.astralis.event.events.impl.render.Render2DEvent;
import cc.astralis.event.events.impl.render.Render3DEvent;
import cc.astralis.event.events.impl.render.ShaderEvent;
import cc.astralis.font.FontManager;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.module.impl.visual.HudModule;
import cc.astralis.property.properties.BooleanProperty;
import cc.astralis.property.properties.ModeProperty;
import cc.astralis.property.properties.NumberProperty;
import cc.astralis.util.math.RandomUtil;
import cc.astralis.util.network.PacketUtil;
import cc.astralis.util.player.MoveUtil;
import cc.astralis.util.player.RayTraceUtil;
import cc.astralis.util.player.RotationUtil;
import cc.astralis.util.player.scaffold.BlockCache;
import cc.astralis.util.player.scaffold.ScaffoldUtil;
import cc.astralis.util.player.scaffold.ScaffoldWalkUtil;
import cc.astralis.util.render.ChatUtil;
import cc.astralis.util.render.Render3DUtil;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import cc.astralis.skija.utils.SkijaUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

import static cc.astralis.module.impl.movement.ScaffoldWalkModule.wrapTo90;
import static cc.astralis.util.player.scaffold.ScaffoldWalkUtil.getBlockStateRelativeToPlayer;

public class ScaffoldRecodeModule extends Module {
    private final BooleanProperty modernWatchdog = new BooleanProperty("Modern Watchdog", false);
    private final ModeProperty rotationMode = new ModeProperty("Rotation Mode", "Basic", "Basic", "Brute Force", "Modern Watchdog", "Snap", "Edge", "None");
    private final ModeProperty rayCastMode = new ModeProperty("Raycast Mode", "None", "Minecraft", "Astralis", "Normal", "None");
    private final ModeProperty rotSpeed = new ModeProperty("Rotation Speed", "Fast", "Fast", "Normal", "Slow");
    private final ModeProperty bypassMode = new ModeProperty("Bypass Mode", "Telly", "Telly", "Telly Slow", "Astralis");
    private final NumberProperty towerTiming = new NumberProperty("Tower Jump Timing", 1, 0, 10, 1);
    private final BooleanProperty spin = new BooleanProperty("Spin", false);
    private final NumberProperty spinSpeed = new NumberProperty("Spin Speed", 30f, 0, 100, 1);
    private final NumberProperty rotationSpeedYaw = new NumberProperty("Rotation Speed Yaw", 180, 1, 180, 1);
    private final NumberProperty rotationSpeedPitch = new NumberProperty("Rotation Speed Pitch", 180, 1, 180, 1);
    private final BooleanProperty keepY = new BooleanProperty("KeepY", false);
    private final BooleanProperty jump = new BooleanProperty("Jump", false);
    private final BooleanProperty dontJumpOnDiag = new BooleanProperty("Don't jump diagonally", false);
    private final NumberProperty jumpEvery = new NumberProperty("Jump Every", 1, 1, 30, 1);
    private final BooleanProperty blockCounter = new BooleanProperty("Block Counter", true);
    private final ModeProperty blockCounterMode = new ModeProperty("Block Counter Mode", "Astralis", "Astralis", "Adjust");
    private final BooleanProperty blockESP = new BooleanProperty("Block ESP", true);
    private final NumberProperty blockESPAlpha = new NumberProperty("Block ESP Alpha", 200, 1, 255, 5);
    public final BooleanProperty spoofItem = new BooleanProperty("Spoof Item", false),
            clientSwing = new BooleanProperty("Swing", true);

    private BlockCache blockCache = null;
    private float targetYaw, targetPitch;
    private double startY;
    private boolean firstJump = true;
    private int ticksOnAir, blocksPlaced;
    private int jumpPlaced;
    public int spoofedSlot = -1, startSlot;

    public ScaffoldRecodeModule() {
        super(Category.MOVEMENT);
        registerProperties(modernWatchdog, rotationMode, rayCastMode,
                rotSpeed.setVisible(() -> rotationMode.is("Modern Watchdog")),
                bypassMode.setVisible(() -> rotationMode.is("Modern Watchdog")),
                towerTiming,
                spin, spinSpeed.setVisible(spin::getProperty),
                rotationSpeedYaw, rotationSpeedPitch, keepY, jump,
                dontJumpOnDiag.setVisible(jump::getProperty),
                jumpEvery.setVisible(jump::getProperty),
                blockCounter, blockCounterMode.setVisible(blockCounter::getProperty),
                blockESP, blockESPAlpha.setVisible(blockESP::getProperty), spoofItem, clientSwing
        );
    }

    @Override
    public void onEnable() {
        if (mc.player != null && mc.level != null) {
            startY = mc.player.getY();

            firstJump = true;
            this.startSlot = mc.player.getInventory().getSelectedSlot();
            this.spoofedSlot = startSlot;

            int blockSlot = ScaffoldUtil.findBiggestBlockStack();

            if (blockSlot == -1) {
                setToggled(false);
                return;
            }

            mc.player.getInventory().setSelectedSlot(blockSlot);

            this.jumpPlaced = 0;
            this.blocksPlaced = 0;
            this.lastYaw = mc.player.getYRot() - 180;
            this.lastPitch = 81.5f;
        }

        super.onEnable();
    }

    @Override
    public void onDisable() {
        blockCache = null;
        if (mc.player != null) {
            mc.options.keyUse.setDown(false);
            mc.options.keyAttack.setDown(false);
            mc.options.keyJump.setDown(false);
        }

        RotationComponent.setSpoofRotations(false);
        RotationComponent.setRotations(targetYaw, targetPitch,
                rotationSpeedYaw.getProperty().floatValue(),
                rotationSpeedPitch.getProperty().floatValue());

        spoofedSlot = -1;
        mc.player.getInventory().setSelectedSlot(startSlot);

        super.onDisable();
    }

    private float currentYaw = 0.0f;

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        this.setSuffix("Normal");

        if (keepY.getProperty() && mc.options.keyJump.isDown() && !mc.player.onGround()) {
            startY = mc.player.getY();
        }

        final BlockPos playerPos = mc.player.blockPosition(),
                belowPos = playerPos.below();

        mc.player.setSprinting(MoveUtil.isMoving());

        blockCache = ScaffoldUtil.getBlockData(keepY.getProperty() && (!mc.options.keyJump.isDown() || firstJump) ? (int) startY : playerPos.getY());

        float[] rotations = calculateRotations();

        targetYaw = rotations[0];
        targetPitch = rotations[1];

        rotate();

        float yaw = RotationComponent.getYaw(), pitch = RotationComponent.getPitch();

        if (rotationMode.is("Modern Watchdog")) {
            RotationComponent.setSpoofRotations(true);
            RotationComponent.setFakeYaw(lastYaw);
            RotationComponent.setFakePitch(lastPitch);
        }

        if (spin.getProperty()) {
            currentYaw += spinSpeed.getProperty().floatValue();
            if (currentYaw >= 360f) {
                currentYaw -= 360f;
            }

            RotationComponent.setSpoofRotations(true);
            RotationComponent.setFakeYaw(currentYaw);
            RotationComponent.setFakePitch(pitch);
        }

        switch (rayCastMode.getProperty()) {
            case "Minecraft": {
                BlockHitResult hit = RayTraceUtil.rayTrace(4.5f, yaw, pitch);
                if (hit == null || hit.getType() != HitResult.Type.BLOCK || !hit.getBlockPos().equals(blockCache.blockPos)) {
                    return;
                }
                break;
            }
            case "Normal": {
                if (!RayTraceUtil.getOver(blockCache.direction, blockCache.blockPos, false, 5f, yaw, pitch)) {
                    return;
                }
                break;
            }
            case "Astralis": {
                if (!RayTraceUtil.isLookingAtBlock(blockCache.direction, blockCache.blockPos, true, 4.5f, yaw, pitch)) {
                    return;
                }
                break;
            }
            case "None": {
                break;
            }
        }

        ticksOnAir = getBlockStateRelativeToPlayer(0, -1, 0).isAir() ? ++ticksOnAir : 0;

        if (ticksOnAir > 0) {
            int blockSlot = ScaffoldUtil.findBiggestBlockStack();
            if (blockSlot == -1)
                return;

            mc.player.getInventory().setSelectedSlot(blockSlot);

            if (blockCache == null) {
                return;
            }

            Vec3 vec3 = ScaffoldUtil.getVec(blockCache);
            BlockHitResult hitResult = new BlockHitResult(
                    vec3,
                    blockCache.direction,
                    blockCache.blockPos,
                    false
            );

            lastBlockPos = hitResult.getBlockPos();
            place(InteractionHand.MAIN_HAND, hitResult);
            firstJump = false;
        }
    }

    @EventTarget
    public void onKeyboard(ClickEvent event) {
        mc.options.keyUse.setDown(false);
        mc.options.keyAttack.setDown(false);
    }

    @EventTarget
    public void onInputTickEvent(InputTickEvent event) {
        boolean spaceDown = InputConstants.isKeyDown(mc.getWindow(), GLFW.GLFW_KEY_SPACE);

        if (rotationMode.is("Modern Watchdog") && spaceDown) {
            event.jump = shouldJumpForTower() || !MoveUtil.isMoving();
        } else {
            jump(event);
        }
    }

    private void rotate() {
       /* float calculateSpeedFast = switch (offGroundTicks) {
            case 0 -> 180;
            case 1 -> 130;
            case 3, 4 -> 20;
            case 5 -> 10;
            case 6 -> 30;
            case 7, 8 -> 80;

            default -> 40;
        };*/
        float easing = Math.max(1, Math.min(offGroundTicks, 5)) / 5f;
        float inverseEasing = 1 - easing;
        float nigga = 130 - (20 * inverseEasing);

        float calculateSpeedFast = mc.player.onGround() ? 180 : offGroundTicks < 2 ? 130 : 40;

        float calculateSpeedNormal = switch (offGroundTicks) {
            case 0 -> 180;
            case 1 -> 130;

            case 3 -> 60;
            case 6, 7, 8 -> 80;

            default -> 40;
        };

        float calculateSpeedSlow = switch (offGroundTicks) {
            case 0 -> 180;

            default -> 40;
        };

        float calculatedSpeed = switch (rotSpeed.getProperty()) {
            case "Fast" -> calculateSpeedFast;
            case "Normal" -> calculateSpeedNormal;
            case "Slow" -> calculateSpeedSlow;

            default -> 0;
        };
        
        RotationComponent.setRotations(
                targetYaw,
                targetPitch,
                rotationMode.is("Modern Watchdog")
                        ?  calculatedSpeed
                        : rotationSpeedYaw.getProperty().floatValue(),
                rotationSpeedPitch.getProperty().floatValue()
        );
    }

    private void place(InteractionHand hand, BlockHitResult hitResult) {
        assert mc.gameMode != null;
        mc.gameMode.useItemOn(mc.player, hand, hitResult);

        if (clientSwing.getProperty())
            mc.player.swing(hand);
        else
            PacketUtil.send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
    }

    private boolean didJump;

    // I love it when watchdog flags mc.player.jump() :broken_heart:.
    private void jump(InputTickEvent event) {
        if (!jump.getProperty()
                || !mc.player.onGround()
                || !MoveUtil.isMoving()
                || mc.options.keyJump.isDown()
                || Astralis.getInstance().getModuleManager().getModule(SpeedModule.class).isToggled()) {
            didJump = false;
            return;
        }

        final boolean isGodBridgeMode = rotationMode.is("God Bridge");
        final boolean shouldSkipDiagonal = MoveUtil.isGoingDiagonally() && dontJumpOnDiag.getProperty();

        boolean shouldJump;

        if (isGodBridgeMode) {
            shouldJump = ticksOnAir > 0 && jumpPlaced > jumpEvery.getProperty().intValue();
        } else {
            shouldJump = blocksPlaced != 0 && blocksPlaced % jumpEvery.getProperty().intValue() == 0 || jumpEvery.getProperty().intValue() == 1;
        }

        if (shouldJump && !shouldSkipDiagonal && !didJump) {
            event.jump = true;
            didJump = true;

            if (isGodBridgeMode) {
                jumpPlaced = -1;
            }
        } else {
            event.jump = false;
        }

        if (mc.player.onGround() && !mc.options.keyJump.isDown()) {
            didJump = false;
        }
    }

    private float lastYaw, secondYaw, lastPitch;

    private float[] calculateRotations() {
        float pitch = 81.5f, yaw = MoveUtil.getMovementYaw(mc.player) + 180;

        if ((shouldPlace(offGroundTicks) || !MoveUtil.isMoving() && modernWatchdog.getProperty()) || !modernWatchdog.getProperty()) {
            switch (rotationMode.getProperty()) {
                case "Snap" -> {
                    if (mc.options.keyJump.isDown())
                        return RotationUtil.getDirectionToBlock(blockCache.blockPos, blockCache.direction);

                    if (ScaffoldWalkUtil.isAirBlock(mc.player.blockPosition())) {
                        if (blockCache != null) {
                            return RotationUtil.getDirectionToBlock(blockCache.blockPos, blockCache.direction);
                        }
                    } else {
                        return new float[]{mc.player.getYRot(), pitch};
                    }
                }

                case "Brute Force" -> {
                    if (blockCache != null) {
                        float[] rots = RotationUtil.getDirectionToBlock(blockCache.blockPos, blockCache.direction);
                        pitch = rots[1];
                        yaw = rots[0];
                    }

                    return new float[]{ yaw, pitch};
                }

                case "Modern Watchdog" -> {

                    if (blockCache != null) {
                        float[] rots = RotationUtil.getDirectionToBlock(blockCache.blockPos,
                                blockCache.direction);
                        pitch = lastPitch = rots[1];
                        yaw = lastYaw = rots[0];
                    }

                    return new float[]{ yaw, pitch };
                }

                case "Basic" -> {
                    return new float[]{yaw, pitch};
                }
            }
        }

        return new float[]{ RotationUtil.forwardYaw(), pitch };
    }

    private boolean shouldPlace(int offGroundTicks) {
        return switch (bypassMode.getProperty()) {
            case "Telly" -> TIME_SINCE_GROUND.finished((long) RandomUtil.getAdvancedRandom(80, 100));
            case "Telly Slow" -> offGroundTicks > 2;
            case "Astralis" -> offGroundTicks > 0;
            default -> false;
        };
    }

    private boolean shouldJumpForTower() {
        if (!mc.player.onGround()) {
            return false;
        }

        int timing = towerTiming.getProperty().intValue();

        BlockPos belowPos = mc.player.blockPosition().below();
        boolean needsBlockBelow = mc.level.getBlockState(belowPos).isAir();

        if (needsBlockBelow && blockCache != null) {
            return onGroundTicks >= timing;
        }

        return false;
    }

    private BlockPos lastBlockPos;

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if (!blockESP.getProperty())
            return;

        Render3DUtil.drawBoxESP(
                event.getMatricies(),
                new AABB(lastBlockPos),
                Astralis.getInstance().getFirstColor(), blockESPAlpha.getProperty().intValue()
        );
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (!blockCounter.getProperty())
            return;

        final Window window = mc.getWindow();

        final float centerX = (float) window.getGuiScaledWidth() / 2,
                centerY = (float) window.getGuiScaledHeight() / 2 + 5;

        final String placedStr = String.valueOf(ScaffoldUtil.getTotalBlocksInInventory());

        switch (blockCounterMode.getProperty()) {
            case "Adjust" -> {
                final float textWidth = tahoma_regular_8.getStringWidth("Blocks"),
                        placedWidth = tahoma_bold_8.getStringWidth(placedStr);

                final float drawX = centerX - (placedWidth + 2 + textWidth) / 2.0f;

                tahoma_bold_8.drawStringWithShadow(
                        placedStr,
                        drawX,
                        centerY,
                        Astralis.getInstance().getFirstColor()
                );

                tahoma_regular_8.drawStringWithShadow(
                        "Blocks",
                        drawX + placedWidth + 2,
                        centerY,
                        Color.white
                );
            }

            case "Astralis" -> {
                final float textWidth = FontManager.getFont("tenacity", 8).getStringWidth("Blocks"),
                        placedWidth = FontManager.getFont("tenacity-bold", 8).getStringWidth(placedStr);
                final float totalWidth = placedWidth + 2.0f + textWidth;

                final float drawX = centerX - totalWidth / 2.0f;
                final float textY = centerY + 2.5f;

                SkijaUtil.roundedRectangle(
                        drawX - 3f,
                        centerY,
                        totalWidth + 3f * 2,
                        15,
                        3,
                        new Color(15, 15, 18, Astralis.getInstance().getModuleManager().getModule(HudModule.class).backgroundAlpha.getProperty().intValue())
                );

                FontManager.getFont("tenacity-bold", 8).drawStringWithShadow(
                        placedStr,
                        drawX,
                        textY,
                        Astralis.getInstance().getFirstColor()
                );

                FontManager.getFont("tenacity", 8).drawStringWithShadow("Blocks",
                        drawX + placedWidth + 2.0f,
                        textY,
                        Color.white
                );
            }
        }
    }

    @EventTarget
    public void onShader(ShaderEvent event) {
        if (!blockCounter.getProperty())
            return;

        Window window = mc.getWindow();

        if (blockCounterMode.is("Astralis")) {
            final float textWidth = FontManager.getFont("tenacity", 8).getStringWidth("Blocks"),
                    placedWidth = FontManager.getFont("tenacity-bold", 8).getStringWidth(String.valueOf(ScaffoldUtil.getTotalBlocksInInventory()));

            final float totalWidth = placedWidth + 2.0f + textWidth;

            SkijaUtil.drawShaderRoundRectangle(
                    (((float) window.getGuiScaledWidth() / 2) - totalWidth / 2.0f) - 3f,
                    (float) window.getGuiScaledHeight() / 2 + 5,
                    totalWidth + 3f * 2,
                    15,
                    3
            );
        }
    }
}