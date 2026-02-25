package cc.astralis.module.impl.movement;

import cc.astralis.Astralis;
import cc.astralis.component.impl.player.RotationComponent;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.MotionEvent;
import cc.astralis.event.events.impl.game.TickEvent;
import cc.astralis.event.events.impl.game.UpdateEvent;
import cc.astralis.event.events.impl.input.InputTickEvent;
import cc.astralis.event.events.impl.render.Render2DEvent;
import cc.astralis.event.events.impl.render.Render3DEvent;
import cc.astralis.event.types.EventModes;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.module.impl.movement.scaffold.sprints.*;
import cc.astralis.module.impl.movement.scaffold.towers.VanillaTower;
import cc.astralis.module.impl.movement.scaffold.towers.WatchdogVanillaTower;
import cc.astralis.property.properties.BooleanProperty;
import cc.astralis.property.properties.ClassModeProperty;
import cc.astralis.property.properties.ModeProperty;
import cc.astralis.property.properties.NumberProperty;
import cc.astralis.util.math.RandomUtil;
import cc.astralis.util.math.TimeUtil;
import cc.astralis.util.network.PacketUtil;
import cc.astralis.util.player.MoveUtil;
import cc.astralis.util.player.RayTraceUtil;
import cc.astralis.util.player.scaffold.ScaffoldWalkUtil;
import cc.astralis.util.player.scaffold.ScaffoldWalkUtil.BlockSlot;
import cc.astralis.util.render.ChatUtil;
import cc.astralis.util.render.ColorUtil;
import cc.astralis.util.render.Render3DUtil;
import com.mojang.blaze3d.platform.Window;
import cc.astralis.skija.utils.SkijaUtil;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.awt.*;
import java.util.Arrays;

import static cc.astralis.util.player.scaffold.ScaffoldWalkUtil.*;

public class ScaffoldWalkModule extends Module {
    private final ModeProperty scaffoldMode = new ModeProperty("Scaffold Mode", "Normal", "Normal", "Modern Watchdog", "Telly", "Snap"),
            placeMode = new ModeProperty("Place Mode", "Update", "Update", "Click");
    private final NumberProperty offGroundTicksPlace = new NumberProperty("Start Placing After", 5, 0, 9, 1);
    private final BooleanProperty swing = new BooleanProperty("Swing Hand", true);
    private final BooleanProperty alternatedScaffoldOrder = new BooleanProperty("Alternated Scaffold Order", false);
    public final BooleanProperty safeWalk = new BooleanProperty("SafeWalk", true);
    private final BooleanProperty sameY = new BooleanProperty("Same Y", false),
            analBoob = new BooleanProperty("Anal Boob", false);
    private final BooleanProperty dontPlaceInSameTick = new BooleanProperty("Don't place in same tick", true);
    private final BooleanProperty jump = new BooleanProperty("Jump", false);
    private final BooleanProperty dontJumpOnDiag = new BooleanProperty("Don't jump diagonally", false);
    private final NumberProperty jumpEvery = new NumberProperty("Jump Every", 1, 1, 30, 1);
    private final NumberProperty placeDelay = new NumberProperty("Place Delay", 0, 0, 10, 1);
    private final BooleanProperty sprintOnJump = new BooleanProperty("Sprint On Jump", false);
    public final BooleanProperty sprint = new BooleanProperty("Sprint", false);
    private final BooleanProperty changeFov = new BooleanProperty("Change FOV", false);
    private final ClassModeProperty sprintMode = new ClassModeProperty(
            "Sprint Mode",
            new VanillaSprint(this),
            new ModernWatchdogSprint(this),
            /*new WatchdogSprint(this),
            new WatchdogSafeSprint(this),*/
            new IntaveSprint(this)
    );
    public final BooleanProperty tower = new BooleanProperty("Tower", false);
    private final ClassModeProperty towerMode = new ClassModeProperty("Tower Mode",
            new WatchdogVanillaTower(this),
            new VanillaTower(this)
    );
    private final BooleanProperty noOffhandCheck = new BooleanProperty("No Off Hand check", true);
    private final BooleanProperty blockCounter = new BooleanProperty("Block Counter", true);
    private final ModeProperty blockCounterMode = new ModeProperty("Block Counter Mode", "Astralis", "Astralis", "Adjust");
    private final BooleanProperty blockESP = new BooleanProperty("Block ESP", true);
    private final ModeProperty blockESPMode = new ModeProperty("Block ESP Mode", "Accurate", "Accurate", "Prediction");
    private final BooleanProperty sneak = new BooleanProperty("Sneak", false);
    private final NumberProperty sneakEvery = new NumberProperty("Sneak Every", 1, 1, 30, 1);
    private final BooleanProperty funnySlotSwitch = new BooleanProperty("Autoswap to next stack", true);
    private final BooleanProperty rotation = new BooleanProperty("Rotations", true);
    private final BooleanProperty oneEightyOnJumpKey = new BooleanProperty("180 on jump key", false);
    private final ModeProperty rayCastMode = new ModeProperty("Ray Cast Mode", "Off", "Off", "Normal", "Advanced", "MC");

    private final NumberProperty
            minYawSpeed = new NumberProperty("Min Yaw Speed", 120, 1, 180, 1),
            maxYawSpeed = new NumberProperty("Max Yaw Speed", 120, 1, 180, 1),
            minPitchSpeed = new NumberProperty("Min Pitch Speed", 120, 1, 180, 1),
            maxPitchSpeed = new NumberProperty("Max Pitch Speed", 120, 1, 180, 1);

    public final ModeProperty rotationMode = new ModeProperty("Rotation Mode", "Basic", "Basic", "Edge", "Modern Watchdog", "Modern Watchdog 2", "God Bridge", "Intave");
    private final BooleanProperty bruteForceYaw = new BooleanProperty("Brute Force Yaw", true),
            bruteForcePitch = new BooleanProperty("Brute Force Pitch", true);
    private final BooleanProperty spoofRayCast = new BooleanProperty("Spoof Ray Cast", false),
                stable = new BooleanProperty("Stable", false);
    private final NumberProperty yawValue = new NumberProperty("Yaw Value", 180, 0, 360, 1),
            pitchValue = new NumberProperty("Pitch Value", 81.7f, -90, 90, 0.1f);

    private BlockData blockData;
    public int blocksPlaced, ticksOnAir, slot, startSlot;
    public double startY;
    private float[] rotations;
    public boolean firstJump;
    private boolean didBecomeNotNull;
    public int spoofedSlot = -1;
    private int lastPlaceTick;
    private int jumpPlaced;

    // tower vars
    private boolean towering;

    public ScaffoldWalkModule() {
        super(Category.MOVEMENT);
        registerProperties(scaffoldMode, alternatedScaffoldOrder, offGroundTicksPlace.setVisible(() -> scaffoldMode.is("Telly")),
                placeMode, placeDelay, swing, sameY, changeFov,
                dontPlaceInSameTick,
                analBoob.setVisible(sameY::getProperty), jump, dontJumpOnDiag.setVisible(jump::getProperty),
                jumpEvery.setVisible(jump::getProperty), sprint, sprintOnJump,
                sprintMode.setVisible(sprint::getProperty), tower,
                towerMode.setVisible(tower::getProperty),
                noOffhandCheck, blockCounter, blockCounterMode.setVisible(blockCounter::getProperty),
                blockESP, blockESPMode.setVisible(blockESP::getProperty),
                sneak, sneakEvery.setVisible(sneak::getProperty), rayCastMode,
                funnySlotSwitch, rotation, oneEightyOnJumpKey.setVisible(rotation::getProperty),
                rotationMode.setVisible(rotation::getProperty),
                bruteForceYaw.setVisible(() -> !rotationMode.is("Intave")),
                bruteForcePitch.setVisible(() -> !rotationMode.is("Intave")),
                spoofRayCast.setVisible(() -> rotationMode.is("Modern Watchdog") && rotation.getProperty()),
                stable.setVisible(() -> rotationMode.is("Modern Watchdog") && rotation.getProperty()),
                minYawSpeed.setVisible(rotation::getProperty),
                maxYawSpeed.setVisible(rotation::getProperty),
                minPitchSpeed.setVisible(rotation::getProperty),
                maxPitchSpeed.setVisible(rotation::getProperty),
                yawValue.setVisible(rotation::getProperty),
                pitchValue.setVisible(rotation::getProperty),
                safeWalk
        );
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            startSlot = mc.player.getInventory().getSelectedSlot();
            startY = mc.player.getBlockY();
            didReset = true;
            slowdownTime.reset();
            blocksPlaced = 0;
            didBecomeNotNull = false;
            if (!sprint.getProperty()) {
                mc.options.toggleSprint().set(false);
                mc.player.setSprinting(false);
                mc.options.keySprint.setDown(false);
            }
        }

        if (rotation.getProperty() && changeFov.getProperty()) {
            mc.options.fovEffectScale().set(0.0);
        }

        this.jumpPlaced = 0;
        spoofedSlot = startSlot;
        lastPlaceTick = -1;

        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (mc.player != null)
            mc.player.getInventory().setSelectedSlot(startSlot);

        if (analBoob.getProperty()) {
            mc.options.keyUse.setDown(false);
        }

        if (rotation.getProperty() && changeFov.getProperty())  {
            mc.options.fovEffectScale().set(1.0);
        }
        spoofedSlot = -1;
        RotationComponent.setSpoofRotations(false);
        RotationComponent.setRotations(rotations,
                RandomUtil.getAdvancedRandom(minYawSpeed.getProperty().floatValue(), maxYawSpeed.getProperty().floatValue()),
                RandomUtil.getAdvancedRandom(minPitchSpeed.getProperty().floatValue(), maxPitchSpeed.getProperty().floatValue()));

        super.onDisable();
    }

    @EventTarget
    public void onTickInput(InputTickEvent event) {
        if (!sprint.getProperty())
            mc.player.setSprinting(false);

        mc.options.keySprint.setDown(false);
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (event.getEventMode() == EventModes.POST) {
            if (!alternatedScaffoldOrder.getProperty())
                rotations = calculateRotations();
        } else {
            if (placeMode.is("Click"))
                place();
        }

        if (alternatedScaffoldOrder.getProperty()) {
            // calculate block data
            blockData = getBlockData((int) (((sameY.getProperty() || Astralis.getInstance().getModuleManager().getModule(SpeedModule.class).isToggled()) && !mc.options.keyJump.isDown()) || firstJump ? startY : mc.player.getBlockY()));
            rotations = calculateRotations();
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        mc.options.keyAttack.setDown(false);
        mc.options.keyUse.setDown(false);

        boolean canTower = tower.getProperty() && mc.options.keyJump.isDown();
        boolean canSprint = (sprint.getProperty() && MoveUtil.isMoving());

        if (sprintOnJump.getProperty() && mc.options.keyJump.isDown() && sprint.getProperty()) {
            mc.player.setSprinting(MoveUtil.isMoving());
        }

        if (!sprintOnJump.getProperty() && !canTower) {
            mc.options.keySprint.setDown(canSprint);
            mc.player.setSprinting(canSprint);
        }

        float randomPitch = RandomUtil.getAdvancedRandom(minPitchSpeed.getProperty().floatValue(), maxPitchSpeed.getProperty().floatValue()),
                randomYaw = RandomUtil.getAdvancedRandom(minYawSpeed.getProperty().floatValue(), maxYawSpeed.getProperty().floatValue());
        if (rotation.getProperty()) {
            RotationComponent.setRotations(rotations,
                    /*150.000f,
                    160.000f*/
                    rotationMode.is("Modern Watchdog") ? (offGroundTicks < (MoveUtil.isGoingDiagonally() ? 1 : 2) ? 180 : MoveUtil.isGoingDiagonally() ? 60 : 0) : randomYaw,
                    randomPitch
            );
        }

        if (placeMode.is("Update")) {
            place();
        }
    }

    private final TimeUtil wasPressedTime = new TimeUtil();

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (mc.options.keyJump.isDown()) {
            wasPressedTime.reset();
        }

        if (!wasPressedTime.finished(200))
            startY = mc.player.getBlockY();

        if (sameY.getProperty() && analBoob.getProperty() && sprint.getProperty()) {
            if (mc.player.onGround()) {
                //  MoveUtil.strafe(Math.min(0.45f, MoveUtil.getSpeed() * 2) - Math.random() / 100f);
            }
            ChatUtil.printDebug(event.getYaw() + " expected: " + (mc.player.getYRot() - 110));
            mc.player.setSprinting(Math.abs(mc.player.getYRot() - 110 - event.getYaw()) <= 30f && MoveUtil.isMoving());

        }

        if (firstJump && offGroundTicks == 8) {
            firstJump = false;
        }

        sneak();
        tower();
    }

    @EventTarget
    public void onInputTickEvent(InputTickEvent event) {
        jump(event);
    }

    private BlockPos lastBlockPos;

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if (!blockESP.getProperty())
            return;

        if (blockData != null) {
            lastBlockPos = blockData.position();
        }

        Render3DUtil.drawBoxESP(
                event.getMatricies(),
                new AABB(blockESPMode.is("Accurate") ? lastBlockPos :
                        (MoveUtil.isMoving() ? lastBlockPos.relative(mc.player.getMotionDirection()) : lastBlockPos.above())),
                Astralis.getInstance().getFirstColor(),50
        );
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (!blockCounter.getProperty())
            return;

        Window window = mc.getWindow();

        float centerX = (float) window.getGuiScaledWidth() / 2;
        float centerY = (float) window.getGuiScaledHeight() / 2 + 5;

        String placedStr = String.valueOf(getTotalBlocksInInventory());

        switch (blockCounterMode.getProperty()) {
            case "Adjust" -> {
                float textWidth = tahoma_regular_8.getStringWidth("Blocks"),
                        placedWidth = tahoma_bold_8.getStringWidth(placedStr);

                float drawX = centerX - (placedWidth + 2 + textWidth) / 2.0f;

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
                float textWidth = product_regular_8.getStringWidth("Blocks"),
                        placedWidth = product_bold_8.getStringWidth(placedStr);
                float totalWidth = placedWidth + 2.0f + textWidth;

                float drawX = centerX - totalWidth / 2.0f;
                float textY = centerY + 4;

                SkijaUtil.roundedRectangle(
                        drawX - 3f,
                        centerY,
                        totalWidth + 3f * 2,
                        15,
                        3,
                        ColorUtil.withAlpha(Color.black, 150)
                );

                product_bold_8.drawStringWithShadow(
                        placedStr,
                        drawX,
                        textY,
                        Astralis.getInstance().getFirstColor()
                );

                product_regular_8.drawStringWithShadow("Blocks",
                        drawX + placedWidth + 2.0f,
                        textY,
                        Color.white
                );
            }
        }
    }

    private void place() {
        BlockSlot[] slots = getBlockSlots(noOffhandCheck.getProperty());
        if (slots.length == 0) {
            return;
        }

        Arrays.sort(slots, (a, b) -> {
            int countA = getStackCount(a);
            int countB = getStackCount(b);
            return Integer.compare(countB, countA);
        });

        int selectedIndex = funnySlotSwitch.getProperty()
                ? (int) RandomUtil.getAdvancedRandom(0, slots.length)
                : 0;
        BlockSlot selectedSlot = slots[selectedIndex];

        if (selectedSlot.hand() == InteractionHand.MAIN_HAND) {
            mc.player.getInventory().setSelectedSlot(selectedSlot.slot());
        }

        ticksOnAir = getBlockStateRelativeToPlayer(0, -1, 0).isAir() ? ++ticksOnAir : 0;

        if (!alternatedScaffoldOrder.getProperty())
            blockData = getBlockData((int) (((sameY.getProperty() || Astralis.getInstance().getModuleManager().getModule(SpeedModule.class).isToggled()) && !mc.options.keyJump.isDown()) || firstJump ? startY : mc.player.getBlockY()));

        if (blockData != null && ticksOnAir > placeDelay.getProperty().intValue()/* && canPlace()*/ &&
                (lastPlaceTick != mc.player.tickCount || !dontPlaceInSameTick.getProperty())
        ) {
            if (isRayCastSafe()) {

                mc.gameMode.useItemOn(mc.player, selectedSlot.hand(),
                        new BlockHitResult(getVec(), blockData.direction(), blockData.position(), false));

                if (swing.getProperty()) {
                    mc.player.swing(selectedSlot.hand());
                } else {
                    PacketUtil.send(new ServerboundSwingPacket(selectedSlot.hand()));
                }

                if (tower.getProperty() && towerMode.is("Watchdog Vertical") && mc.options.keyJump.isDown()) {
                    mc.gameMode.useItemOn(mc.player, selectedSlot.hand(),
                            new BlockHitResult(getVec(), blockData.direction(),
                                    new BlockPos(blockData.position().getX() - 1, blockData.position().getY(),
                                            blockData.position().getZ()), false));
                }

                blocksPlaced++;
                jumpPlaced++;
                ticksOnAir = 0;
                lastPlaceTick = mc.player.tickCount;
            }
        }
    }

    private float watchdogYawFixRandom;
    private float watchcockYaw;

    private boolean didReset = false;
    public final TimeUtil slowdownTime = new TimeUtil();
    private float[] lastBruteForceRots = null;
    private int bruteForceTicks = 0;

    public static float wrapTo90(float f) {
        f %= 90F;
        if (f >= 45) f -= 90;
        if (f < -45) f += 90;
        return f;
    }

    private BlockData lastBlockData;
    private float[] calculateRotations() {
        float y = mc.player.getYRot() - yawValue.getProperty().floatValue() +
                (mc.options.keyRight.isDown() ? 45 : mc.options.keyLeft.isDown() ? -45 :
                        mc.options.keyDown.isDown() ? 180 : 0);

        if (blockData != null)
            lastBlockData = blockData;

        float baseYaw;
        float basePitch = pitchValue.getProperty().floatValue();

        if (!MoveUtil.isMoving() && rotationMode.is("Modern Watchdog"))
            return new float[] {y, basePitch};

        if (rotationMode.is("Modern Watchdog")) {
      /*      RotationComponent.setSpoofRotations(true);
            RotationComponent.setFakeYaw(mc.player.isOnGround() ? mc.player.getYaw() : mc.player.getYaw() - 180);
            RotationComponent.setFakePitch(basePitch);*/
            boolean leftSide;
            Vec3 position = new Vec3(lastBlockPos.getX() + 0.5,  lastBlockPos.getY(), lastBlockPos.getZ() + 0.5); // center pos

            double yawRad = MoveUtil.getDirection();
            double dirX = -Math.sin(yawRad);
            double dirZ = Math.cos(yawRad);

            double toEntryX = position.x - mc.player.getX();
            double toEntryZ = position.z - mc.player.getZ();

            leftSide = (dirX * toEntryZ - dirZ * toEntryX) > 0;
            float straightValue = 260;
            baseYaw = mc.player.getYRot() + (MoveUtil.isGoingDiagonally() ? -135 : (!leftSide ? straightValue : -straightValue)) /* * Math.signum(f)*/;
           /* ChatUtil.printDebug(MoveUtil.isGoingDiagonally());*/
        } else {
            baseYaw = mc.player.getYRot() - yawValue.getProperty().floatValue() +
                    (mc.options.keyRight.isDown() ? 45 : mc.options.keyLeft.isDown() ? -45 :
                            mc.options.keyDown.isDown() ? 180 : 0);
        }

   /*     if (rotationMode.is("Watchdog Modern")) {
            baseYaw = mc.player.getYaw() - (MoveUtil.isGoingDiagonally() ? 180 : 225) +
                    (mc.options.rightKey.isPressed() ? 45 : mc.options.leftKey.isPressed() ? -45 :
                            mc.options.backKey.isPressed() ? 180 : 0);
        }
*/
        float forwardYaw = mc.player.getYRot() + 45;

        // ChatUtil.print(ticksOnAir);
        if (canPlace()) {
            boolean found = false;
            float finalBruteForceYaw = baseYaw;
            float finalBruteForcePitch = basePitch;
            if ((bruteForceYaw.getProperty() || bruteForcePitch.getProperty()) && !rotationMode.is("Intave")) {
                if (blockData != null) {
                  /*  if (lastBruteForceRots != null && isRayCastSafe()) {
                        return lastBruteForceRots;
                    }*/

                    if (bruteForcePitch.getProperty()) {
                        for (float possiblePitch = 90; possiblePitch > 30 && !found; possiblePitch -=
                                (possiblePitch > (mc.player.hasEffect(MobEffects.SPEED) ? 60 : 80) ? 0.5f : 2f)) {
                            if (RayTraceUtil.getOver(blockData.direction(), blockData.position(), true, 5, baseYaw, possiblePitch)) {
                                finalBruteForceYaw = baseYaw;
                                finalBruteForcePitch = possiblePitch;
                                found = true;
                                lastBruteForceRots = new float[]{finalBruteForceYaw, finalBruteForcePitch};
                                bruteForceTicks = 0;
                            }
                        }
                    }

                    if (bruteForceYaw.getProperty() && !found) {
                        for (float yawOffset = 0; yawOffset <= 180 && !found; yawOffset += 5) {
                            for (float direction = -1; direction <= 1 && !found; direction += 2) {
                                float possibleYaw = baseYaw + (yawOffset * direction);
                                for (float possiblePitch = 85; possiblePitch > 30 && !found; possiblePitch -= 5) {
                                    if (RayTraceUtil.getOver(blockData.direction(), blockData.position(), true, 5, possibleYaw, possiblePitch)) {
                                        finalBruteForceYaw = possibleYaw;
                                        finalBruteForcePitch = possiblePitch;
                                        found = true;
                                        lastBruteForceRots = new float[]{finalBruteForceYaw, finalBruteForcePitch};
                                        bruteForceTicks = 0;
                                    }
                                }
                            }
                        }
                    }

                } else {
                   /* if (bruteForceTicks != 0) {
                        if (scaffoldMode.is("Snap")) {
                            return new float[]{mc.player.getYaw(), basePitch};
                        }
                    }
                    if (lastBruteForceRots != null) {
                        bruteForceTicks++;
                        return lastBruteForceRots;
                    }*/
                }
            }

            baseYaw = bruteForceYaw.getProperty() ? finalBruteForceYaw : baseYaw;
            basePitch = bruteForcePitch.getProperty() ? finalBruteForcePitch : basePitch;

            switch (rotationMode.getProperty()) {
                case "Basic" -> {
                    return new float[]{baseYaw, basePitch};
                }

                case "Modern Watchdog" -> {
                    if (blockData != null) {
                        float[] rots = getDirectionToBlock(blockData.position.getX(), blockData.position.getY(), blockData.position.getZ(),
                                blockData.direction);
                        basePitch = rots[1];
                    }

                    return new float[] {baseYaw, basePitch};
                }

                case "Modern Watchdog 2" -> {
                    boolean up = mc.options.keyUp.isDown();
                    boolean left = mc.options.keyLeft.isDown();
                    float y2 = mc.player.getYRot() - (float) Math.toDegrees(Math.atan2(left == mc.options.keyRight.isDown() ? 0 : left ? 1 : -1, up == mc.options.keyDown.isDown() ? 0 : up ? 1 : -1));
                    float f = wrapTo90(y2);

                    return new float[] {y2 + (onGroundTicks == 1 ? 0 : (Math.abs(f) > 22.5 ? -140 : 90)) * (f == 0.0F ? 1.0F : Math.signum(f)), basePitch};
                }

               /* case "Modern Watchdog" -> {
                    return new float[] { baseYaw + (onGroundTicks == 1 ? 0 : 90), 0, 5};
                }*/
                case "Edge" -> {
                    // badaiim bipass
                    float dirYaw = MoveUtil.getRawDirectionNoKeys();
                    if (oneEightyOnJumpKey.getProperty() && !MoveUtil.isMoving()) {
                        return new float[]{mc.player.getYRot() - 180, basePitch};
                    }

/*
                    if (((!mc.player.isOnGround() && mc.options.jumpKey.isPressed()) || !MoveUtil.isMoving())
*/

                    if (MoveUtil.isPressingForwardAndStrafe())
                        return new float[]{baseYaw, basePitch};

                    if (!didBecomeNotNull) {
                        if (blockData != null)
                            didBecomeNotNull = true;

                        return new float[]{MoveUtil.isGoingDiagonally() ? dirYaw - 140 + watchdogYawFixRandom : dirYaw + 100 + watchdogYawFixRandom, basePitch};
                    } else {
                        if (mc.player.tickCount % 3 == 0) {
                            watchdogYawFixRandom = RandomUtil.getAdvancedRandom(0f, 9f);
                        }

                        boolean leftSide;
                        Vec3 position = new Vec3(blockData.position.getX() + 0.5, blockData.position.getY(), blockData.position.getZ() + 0.5); // center pos

                        double yawRad = Math.toRadians(dirYaw);
                        double dirX = -Math.sin(yawRad);
                        double dirZ = Math.cos(yawRad);

                        double toEntryX = position.x - mc.player.getX();
                        double toEntryZ = position.z - mc.player.getZ();

                        leftSide = (dirX * toEntryZ - dirZ * toEntryX) > 0;

                        if (MoveUtil.isGoingDiagonally()) {
                            watchcockYaw = dirYaw - 140 /* + watchdogYawFixRandom  */;
                        } else if (!MoveUtil.isGoingDiagonally()) {
                            watchcockYaw = dirYaw + (leftSide ? 100 + watchdogYawFixRandom : -100 - watchdogYawFixRandom);
                        }

                        return new float[]{watchcockYaw, 82};
                    }
                }

                case "God Bridge" -> {
                    //return new float[] {mc.thePlayer.rotationYaw - ((MoveUtil.isGoingDiagonally()) ? 180 : 225f), 75.7F};
                    Options options = mc.options;
                    boolean up = options.keyUp.isDown(), down = options.keyDown.isDown(), left = options.keyLeft.isDown(), right = options.keyRight.isDown();
                    float yRot = Mth.wrapDegrees(mc.player.getYRot() - 180 + (up == down ? left == right ? 0 : left ? -90 : 90 : up ? left == right ? 0 : left ? -45 : 45 : left == right ? 180 : left ? -135 : 135));
                    if (yRot >= -22.5F && yRot < 22.5F) {
                        yRot = 45;
                    }
                    if (yRot < -22.5F && yRot >= -67.5F) yRot = -45;
                    if (yRot < -67.5F && yRot >= -112.5F) {
                        yRot = -45;
                    }
                    if (yRot < -112.5F && yRot >= -157.5F) yRot = -135;
                    if (yRot < -157.5F && yRot >= -180 || yRot >= 157.5F && yRot < 180) {
                        yRot = -135;
                    }
                    if (yRot >= 112.5F && yRot < 157.5F) yRot = 135;
                    if (yRot >= 67.5F && yRot < 112.5F) {
                        yRot = 135;
                    }
                    if (yRot >= 22.5F && yRot < 67.5F) yRot = 45;


                    return new float[]{yRot, 75.7F};
                }
                case "Intave" -> {
                    if (mc.options.keyJump.isDown())
                        return getDirectionToBlock(blockData.position.getX(), blockData.position.getY(), blockData.position.getZ(),
                                blockData.direction);

                    if (ScaffoldWalkUtil.isAirBlock(mc.player.blockPosition())) {
                        if (blockData != null) {
                            float[] rots = getDirectionToBlock(blockData.position.getX(), blockData.position.getY(), blockData.position.getZ(),
                                    blockData.direction);

                            return rots;
                        }
                    } else {
                        return new float[] { forwardYaw, basePitch };
                    }
             /*       float targetYaw = baseYaw;
                    float targetPitch = basePitch;

                    if (blockData != null) {
                        // Calculate initial target rotations
                        float[] rots = getDirectionToBlock(blockData.position(), blockData.direction());
                        targetYaw = rots[0];
                        targetPitch = rots[1];

                        // Check if we're already looking at the block
                        MovingObjectPosition rotationRay = rayCast(1, new float[]{targetYaw, targetPitch},
                                mc.interactionManager.getReachDistance(), 2);

                        if (!rotationRay.getBlockPos().equals(blockData.position()) ||
                                rotationRay.getSide() != blockData.direction()) {

                            // If not looking at block, brute force find correct rotation
                            int maxTicks = (int) (Math.abs(MathHelper.wrapDegrees(targetYaw - watchcockYaw) / 4);
                            boolean stop = false;
                            int ticks = 0;

                            while (ticks <= maxTicks && !stop) {
                                targetYaw = updateRotation(watchcockYaw, rots[0], 5);
                                targetPitch = getYawBasedPitch(blockData.position(), blockData.direction(),
                                        targetYaw, basePitch);

                                MovingObjectPosition stopRay = rayCast(1, new float[]{targetYaw, targetPitch},
                                        mc.interactionManager.getReachDistance(), 2);

                                if (stopRay.getBlockPos().equals(blockData.position()) &&
                                        stopRay.getSide() == blockData.direction()) {
                                    stop = true;
                                }
                                ticks++;
                            }
                        }
                    }

                    watchcockYaw = targetYaw;
                    return new float[]{targetYaw, targetPitch};*/
                }
            }
        } else {
            return new float[]{mc.player.getYRot(), basePitch};
        }

        return new float[]{forwardYaw, basePitch};
    }

    public static float[] getDirectionToBlock(double x, double y, double z, Direction direction) {
        ClientLevel world = mc.level;
        if (world == null) return new float[]{0, 0};

        Entity marker = new Entity(EntityType.ITEM, world) {
            @Override
            protected void defineSynchedData(SynchedEntityData.Builder builder) {

            }

            @Override
            public boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
                return false;
            }

            @Override
            protected void readAdditionalSaveData(ValueInput view) {

            }

            @Override
            protected void addAdditionalSaveData(ValueOutput view) {

            }
/*
            @Override
            protected void initDataTracker() {}
            @Override
            protected void readCustomDataFromNbt(NbtCompound nbt) {}
            @Override
            protected void writeCustomDataToNbt(NbtCompound nbt) {}*/
        };

        marker.setPos(
                x + 0.5D + direction.getStepX() * 0.5D,
                y + 0.5D + direction.getStepY() * 0.5D,
                z + 0.5D + direction.getStepZ() * 0.5D
        );

        return getRotationFromPosition(marker.getX(), marker.getY(), marker.getZ());
    }

    public static float[] getRotationFromPosition(double x, double y, double z) {
        LocalPlayer player = mc.player;
        if (player == null) return new float[]{0, 0};

        double xDiff = x - player.getX();
        double zDiff = z - player.getZ();
        double yDiff = y - player.getY() - 1.2D;
        double dist = Math.sqrt(xDiff * xDiff + zDiff * zDiff);

        float yaw = (float) (Math.atan2(zDiff, xDiff) * 180.0F / (float) Math.PI - 90.0F);
        float pitch = (float) -(Math.atan2(yDiff, dist) * 180.0F / (float) Math.PI);

        return new float[]{yaw, pitch};
    }

    private void sneak() {
        if (!sneak.getProperty()) {
            return;
        }

        mc.options.keyShift.setDown(blocksPlaced % sneakEvery.getProperty().floatValue() == 0);
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

        final boolean isGodBridgeMode = rotationMode.is("God Bridge") && rotation.getProperty();
        final boolean shouldSkipDiagonal = MoveUtil.isGoingDiagonally() && dontJumpOnDiag.getProperty();

        boolean shouldJump;

        if (isGodBridgeMode) {
            shouldJump = ticksOnAir > 0 && jumpPlaced > jumpEvery.getProperty().intValue();
        } else {
            shouldJump = blocksPlaced != 0 && blocksPlaced % jumpEvery.getProperty().intValue() == 0 || jumpEvery.getProperty().intValue() == 1;
        }

        if (shouldJump && !shouldSkipDiagonal && !didJump ) {
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

    private void tower() {
        if (!tower.getProperty() || !mc.options.keyJump.isDown()) {
            if (towering) {
                MoveUtil.stop();

            }
            towering = false;
            return;
        }

        mc.options.keyUse.setDown(false);
        towering = true;
    }

    private boolean canPlace() {
        return switch (scaffoldMode.getProperty()) {
            case "Telly" -> offGroundTicks > offGroundTicksPlace.getProperty().intValue();
            case "Modern Watchdog" -> !mc.player.onGround();
            default -> true;
        };
    }

    // todo: white list from obfuscation.
    private boolean isRayCastSafe() {
        // modern watchdog for some reason does not have good raycast checks meaning we can sometimes places with yaws
        // that you shouldn't be able to.

        boolean spoof = rotationMode.is("Modern Watchdog") && spoofRayCast.getProperty();

        float yaw = spoof ?
                mc.player.getYRot() - 180 : RotationComponent.getYaw(), pitch = RotationComponent.getPitch();

        if (blockData != null && MoveUtil.isGoingDiagonally() && spoof) {
            float[] rots = getDirectionToBlock(blockData.position.getX(), blockData.position.getY(), blockData.position.getZ(),
                    blockData.direction);
            yaw = rots[0];
        }

        return /*spoof && MoveUtil.isGoingDiagonally() || */switch (rayCastMode.getProperty()) {
            case "Advanced" -> RayTraceUtil.getOver(blockData.direction(), blockData.position(), true, 5, yaw, pitch);
            case "Normal" -> RayTraceUtil.getOver(blockData.direction(), blockData.position(), false, 5, yaw, pitch);
            case "MC" -> isMCRayCastSafe();
            default -> true;
        };
    }

    private boolean isMCRayCastSafe() {
        if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK
                && mc.hitResult instanceof BlockHitResult blockHitResult) {
            return blockHitResult.getBlockPos().equals(blockData.position())
                    && blockHitResult.getDirection().equals(blockData.direction());
        }
        return false;
    }

    private Vec3 getVec() {
        if (blockData == null)
            return null;

        BlockPos pos = blockData.position();

        double x = pos.getX() + Math.random();
        double y = pos.getY() + Math.random();
        double z = pos.getZ() + Math.random();

        final HitResult movingObjectPosition = mc.hitResult;

        // Fallback vector.
        switch (blockData.direction) {
            case DOWN -> y = pos.getY();
            case UP -> y = pos.getY() + 1;
            case NORTH -> z = pos.getZ();
            case SOUTH -> z = pos.getZ() + 1;
            case WEST -> x = pos.getX();
            case EAST -> x = pos.getX() + 1;
        }

        if (movingObjectPosition instanceof BlockHitResult blockHitResult) {
            if (blockHitResult.getBlockPos().equals(blockData.position()) &&
                    blockHitResult.getDirection() == blockData.direction()) {
                x = blockHitResult.getLocation().x;
                y = blockHitResult.getLocation().y;
                z = blockHitResult.getLocation().z;
            }
        }

        return new Vec3(x, y, z);
    }

    private int getTotalBlocksInInventory() {
        int total = 0;
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.getItem() instanceof BlockItem blockItem) {
                Block block = blockItem.getBlock();
                if (ScaffoldWalkUtil.canPlaceBlock(block)) {
                    total += stack.getCount();
                }
            }
        }

        return total;
    }


    public record BlockData(BlockPos position, Direction direction) {
        /* w */
    }
}