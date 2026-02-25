package cc.astralis.module.impl.combat;

import astralis.mixin.accessor.mc.KeyBindingAccessor;
import cc.astralis.Astralis;
import cc.astralis.component.impl.network.BlinkComponent;
import cc.astralis.component.impl.player.RotationComponent;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.PostMotionEvent;
import cc.astralis.event.events.impl.game.SlowDownEvent;
import cc.astralis.event.events.impl.game.TickEvent;
import cc.astralis.event.events.impl.game.UpdateEvent;
import cc.astralis.event.events.impl.input.SwordInputEvent;
import cc.astralis.event.events.impl.network.PacketEvent;
import cc.astralis.event.events.impl.render.Render3DEvent;
import cc.astralis.event.types.EventModes;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.module.impl.movement.ScaffoldRecodeModule;
import cc.astralis.property.properties.BooleanProperty;
import cc.astralis.property.properties.ModeProperty;
import cc.astralis.property.properties.NumberProperty;
import cc.astralis.property.properties.body.BodyPart;
import cc.astralis.property.properties.body.BodyProperty;
import cc.astralis.util.math.RandomUtil;
import cc.astralis.util.math.TimeUtil;
import cc.astralis.util.network.PacketUtil;
import cc.astralis.util.player.PlayerUtil;
import cc.astralis.util.player.RayTraceUtil;
import cc.astralis.util.player.RotationUtil;
import cc.astralis.util.render.ChatUtil;
import cc.astralis.util.render.Render3DUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundClientTickEndPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class KillauraModule extends Module {
    private final ModeProperty auraMode = new ModeProperty("Aura Mode", "Single", "Single", "Switch");
    private final BooleanProperty raycast = new BooleanProperty("Ray Cast", false);
    private final NumberProperty switchDelay = new NumberProperty("Switch Delay", 200, 0, 5000, 1);
    private final NumberProperty scoutRange = new NumberProperty("Scout Range", 3, 0, 6, 0.1f);
    public final NumberProperty attackRange = new NumberProperty("Attack Range", 3, 0, 6, 0.1f);

    private final BooleanProperty watchdogKeepSprint = new BooleanProperty("Watchdog Keep Sprint", true),
            disableKeepSprintOnKB = new BooleanProperty("Disable Keep Sprint on KB", true);

    public BooleanProperty autoBlock = new BooleanProperty("Auto Block", false);
    public final NumberProperty blockRange = new NumberProperty("Block Range", 3, 0, 6, 0.1f);
    public ModeProperty autoBlockMode = new ModeProperty("Auto Block Mode", "Vanilla", "Vanilla", "None", "Blink", "Modern Watchdog", "Modern Watchdog Post", "Old Watchdog");
    private final BooleanProperty blink = new BooleanProperty("Blink", false);
    private final BooleanProperty packetAutoBlock = new BooleanProperty("Packet Auto Block", false),
            interactWhenBlocking = new BooleanProperty("Interact When Blocking", false),
            forceBlockHitAnimation = new BooleanProperty("Force Block Hit Animation", false);

    private final BooleanProperty rotation = new BooleanProperty("Rotations", true);
    private final ModeProperty legitRandomization = new ModeProperty("Noise Mode", "Off", "Off", "Snap", "MouseSim")
            .setVisible(rotation::getProperty);

    // This is a part of Array client code (created by Kawase).
    private final NumberProperty noiseYawMultiplier = new NumberProperty("Noise Yaw Multiplier", 0.08f, 0.01f, 0.5f, 0.01f)
            .setVisible(() -> legitRandomization.is("Snap") || legitRandomization.is("MouseSim"));
    private final NumberProperty noisePitchMultiplier = new NumberProperty("Noise Pitch Multiplier", 0.02f, 0.01f, 0.3f, 0.01f)
            .setVisible(() -> legitRandomization.is("Snap") || legitRandomization.is("MouseSim"));

    private final NumberProperty maxYawNoise = new NumberProperty("Max Yaw Noise", 1.2f, 0.1f, 5f, 0.1f)
            .setVisible(() -> legitRandomization.is("Snap") || legitRandomization.is("MouseSim"));
    private final NumberProperty maxPitchNoise = new NumberProperty("Max Pitch Noise", 0.75f, 0.1f, 5f, 0.1f)
            .setVisible(() -> legitRandomization.is("Snap") || legitRandomization.is("MouseSim"));

    private final BooleanProperty gcdFix = new BooleanProperty("GCD Fix", false);
    private final BodyProperty bodyPart = new BodyProperty("Rotation Target", BodyPart.HEAD);
    public NumberProperty rotationRange = new NumberProperty("Rotation Range", 3, 0, 6, 0.1f);

    private final NumberProperty
            minYawSpeed = new NumberProperty("Min Yaw Speed", 120, 1, 180, 1),
            maxYawSpeed = new NumberProperty("Max Yaw Speed", 120, 1, 180, 1),
            minPitchSpeed = new NumberProperty("Min Pitch Speed", 120, 1, 180, 1),
            maxPitchSpeed = new NumberProperty("Max Pitch Speed", 120, 1, 180, 1);

    private final BooleanProperty latestCPS = new BooleanProperty("1.9+ CPS", false);
    private final BooleanProperty onedotEight = new BooleanProperty("1.8 Swing Order", false);
    public NumberProperty minCPS = new NumberProperty("Min CPS", 10, 0, 20, 1),
            maxCPS = new NumberProperty("Max CPS", 20, 0, 20, 1);
    private final ModeProperty critMode = new ModeProperty("Wait for Crit", "Off", "Off", "Normal", "Force");
    private final BooleanProperty criticalSprint = new BooleanProperty("Critical Sprint", false);
    private final BooleanProperty throughWalls = new BooleanProperty("Through Walls", true),
            ignoreTeamMates = new BooleanProperty("Ignore Teammates", true);
    private final BooleanProperty monsters = new BooleanProperty("Monsters", true),
            animals = new BooleanProperty("Animals", false),
            invisible = new BooleanProperty("Invisible", false);
    private final BooleanProperty esp = new BooleanProperty("ESP", false);
    private final NumberProperty espWidth = new NumberProperty("Esp Width", 0.8f, 0, 10, 0.1f);

    public static LivingEntity target;

    public List<LivingEntity> validTargets = new ArrayList<>();
    private final TimeUtil cpsTime = new TimeUtil(), switchTime = new TimeUtil();
    private float[] rotations;

    public boolean renderBlockHit, isBlocking;

    private boolean isBlinking = false;
    private int autoBlockTicks = 0, killAuraTicks = 0;
    private int targetIndex = 0;

    private final TimeUtil rotationRandomizationTimer = new TimeUtil(), rotationNoiseTimer = new TimeUtil();

    private double currentYawSpeed = 0f, currentPitchSpeed = 0f;
    private float randomYawOffset = 0f, randomPitchOffset = 0f;

    private float smoothYawNoise = 0f, smoothPitchNoise = 0f;
    private float yawNoiseTarget = 0f, pitchNoiseTarget = 0f;

    public KillauraModule() {
        super(Category.COMBAT);
        registerProperties(auraMode, raycast,
                switchDelay.setVisible(() -> auraMode.is("Switch")),
                scoutRange, attackRange, watchdogKeepSprint,
                disableKeepSprintOnKB.setVisible(watchdogKeepSprint::getProperty),
                autoBlock,
                blockRange.setVisible(autoBlock::getProperty),
                autoBlockMode.setVisible(autoBlock::getProperty),
                blink.setVisible(() -> autoBlockMode.is("Watchdog 2") && autoBlock.getProperty()),
                packetAutoBlock.setVisible(autoBlock::getProperty),
                interactWhenBlocking.setVisible(autoBlock::getProperty),
                forceBlockHitAnimation.setVisible(autoBlock::getProperty),

                rotation,
                gcdFix.setVisible(rotation::getProperty),
                bodyPart.setVisible(rotation::getProperty),
                rotationRange.setVisible(rotation::getProperty),

                legitRandomization,
                noiseYawMultiplier.setVisible(() -> legitRandomization.is("Snap") || legitRandomization.is("MouseSim")),
                noisePitchMultiplier.setVisible(() -> legitRandomization.is("Snap") || legitRandomization.is("MouseSim")),
                maxYawNoise.setVisible(() -> legitRandomization.is("Snap") || legitRandomization.is("MouseSim")),
                maxPitchNoise.setVisible(() -> legitRandomization.is("Snap") || legitRandomization.is("MouseSim")),

                minYawSpeed.setVisible(rotation::getProperty),
                maxYawSpeed.setVisible(rotation::getProperty),
                minPitchSpeed.setVisible(rotation::getProperty),
                maxPitchSpeed.setVisible(rotation::getProperty),
                latestCPS, onedotEight, critMode, criticalSprint,
                minCPS.setVisible(() -> !latestCPS.getProperty()),
                maxCPS.setVisible(() -> !latestCPS.getProperty()),
                throughWalls, monsters, ignoreTeamMates, animals, invisible,
                esp, espWidth.setVisible(esp::getProperty)
        );
    }

    @Override
    public void onEnable() {
        this.isBlocking = false;

        // this.killAuraTicks = 0;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        target = null;
        setBlockState(false);
        super.onDisable();
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (event.getEventMode() == EventModes.POST ||
                Astralis.getInstance().getModuleManager().getModule(ScaffoldRecodeModule.class).isToggled() ||
                (Astralis.getInstance().getModuleManager().getModule(AntiFireBallModule.class).isToggled() &&
                        Astralis.getInstance().getModuleManager().getModule(AntiFireBallModule.class).target != null)
        ) {
            return;
        }

        killAuraTicks++;
        this.setSuffix(auraMode.getProperty());

        this.validTargets = PlayerUtil.getTargets(
                animals.getProperty(), monsters.getProperty(),
                ignoreTeamMates.getProperty(), invisible.getProperty(),
                throughWalls.getProperty(), scoutRange.getProperty().floatValue()
        );

        // shift code inc.
        List<LivingEntity> distanceSortedTargets = validTargets.stream()
                .sorted(Comparator.comparingDouble(RotationUtil::getDistanceToEntityBox))
                .toList();

        if (auraMode.is("Switch")) {
            if (validTargets.size() == 1) {
                this.validTargets = distanceSortedTargets;
            } else {
                this.validTargets = validTargets.stream()
                        .filter(entity -> RotationUtil.getDistanceToEntityBox(entity) <= attackRange.getProperty().floatValue())
                        .sorted(Comparator.comparingDouble(RotationUtil::getDistanceToEntityBox))
                        .collect(Collectors.toList());
            }
        } else {
            this.validTargets = distanceSortedTargets;
        }

        if (validTargets.isEmpty()) {
            target = null;
            targetIndex = 0;
            setBlockState(false);
            return;
        }

        if (auraMode.is("Single")) {
            target = validTargets.getFirst();
            targetIndex = 0;
        } else {
            if (targetIndex >= validTargets.size()) {
                targetIndex = 0;
            }

            if (switchTime.finished(switchDelay.getProperty().longValue())) {
                targetIndex = (targetIndex + 1) % validTargets.size();
                switchTime.reset();
            }

            target = validTargets.get(targetIndex);
        }

        if (target == null) {
            setBlockState(false);
            return;
        }

     /*   if (Astralis.getInstance().getModuleManager().getModule(BreakerModule.class).isToggled() &&
                Astralis.getInstance().getModuleManager().getModule(BreakerModule.class).rotate) {
            setBlockState(false);
            return;
        }*/

        handleCombat();
    }

    private void handleCombat() {
        float distance = (float) RotationUtil.getDistanceToEntityBox(target);

        if (distance <= rotationRange.getProperty().floatValue() && rotation.getProperty()/* &&
                !(Astralis.getInstance().getModuleManager().getModule(BreakerModule.class).isToggled() &&
                        Astralis.getInstance().getModuleManager().getModule(BreakerModule.class).rotate)*/) {

            double minYaw = this.minYawSpeed.getProperty().doubleValue() / 2.0;
            double maxYaw = this.maxYawSpeed.getProperty().doubleValue() / 2.0;
            double minPitch = this.minPitchSpeed.getProperty().doubleValue() / 2.0;
            double maxPitch = this.maxPitchSpeed.getProperty().doubleValue() / 2.0;

            if (minYaw > maxYaw) minYaw = maxYaw;
            if (maxYaw < minYaw) maxYaw = minYaw;
            if (minPitch > maxPitch) minPitch = maxPitch;
            if (maxPitch < minPitch) maxPitch = minPitch;

            if (this.rotationRandomizationTimer.finished(150)) {
                this.currentYawSpeed = minYaw + new Random().nextDouble() * (maxYaw - minYaw);
                this.currentPitchSpeed = minPitch + new Random().nextDouble() * (maxPitch - minPitch);
            }

            this.currentYawSpeed = Mth.clamp(this.currentYawSpeed, minYaw, maxYaw);
            this.currentPitchSpeed = Mth.clamp(this.currentPitchSpeed, minPitch, maxPitch);

            float[] rotsMode;

            switch (legitRandomization.getProperty()) {
                case "Snap" -> {
                    if (rotationNoiseTimer.finished((long) RandomUtil.getAdvancedRandom(100, 200))) {
                        randomYawOffset = generateYawNoise();
                        randomPitchOffset = generatePitchNoise();
                        rotationNoiseTimer.reset();
                    }

                    Vec3 noisyVec = RotationUtil.getHitVec3(target).add(
                            randomYawOffset * noiseYawMultiplier.getProperty().floatValue(),
                            randomPitchOffset * noisePitchMultiplier.getProperty().floatValue(),
                            randomYawOffset * noiseYawMultiplier.getProperty().floatValue()
                    );

                    float[] noisyRot = RotationUtil.getRotationsToVector(noisyVec);
                    noisyRot[1] = Mth.clamp(noisyRot[1], -89.9f, 89.9f);
                    rotsMode = noisyRot;
                }
                case "MouseSim" -> {
                    if (rotationNoiseTimer.finished(350)) {
                        yawNoiseTarget = generateYawNoise();
                        pitchNoiseTarget = generatePitchNoise();
                        rotationNoiseTimer.reset();
                    }

                    smoothYawNoise += (yawNoiseTarget - smoothYawNoise) * 0.1f;
                    smoothPitchNoise += (pitchNoiseTarget - smoothPitchNoise) * 0.1f;

                    Vec3 noisyVec = RotationUtil.getHitVec3(target).add(
                            smoothYawNoise * noiseYawMultiplier.getProperty().floatValue(),
                            smoothPitchNoise * noisePitchMultiplier.getProperty().floatValue(),
                            smoothYawNoise * noiseYawMultiplier.getProperty().floatValue()
                    );

                    float[] noisyRot = RotationUtil.getRotationsToVector(noisyVec);
                    noisyRot[1] = Mth.clamp(noisyRot[1], -89.9f, 89.9f);
                    rotsMode = noisyRot;
                }
                default -> {
                   rotsMode = RotationUtil.getRotations(target);
                }
            }

            rotations = rotsMode;

            if (gcdFix.getProperty()) {
                rotations[0] += (float) (Math.random() - 0.5f);
                rotations[1] += (float) (Math.random() - 0.5f) * 2;
            }

            RotationComponent.setRotations(rotations,
                    (float) currentYawSpeed,
                    (float) currentPitchSpeed
            );
        }

        boolean canAutoBlock = distance <= blockRange.getProperty().floatValue() && autoBlock.getProperty()
                && mc.player.getInventory().getSelectedItem().is(ItemTags.SWORDS);

        RayTraceUtil.updateCrosshairTarget(throughWalls.getProperty());
        BlockHitResult hitResult;

        boolean rayCast = (hitResult = RotationUtil.calculateIntercept(target.getBoundingBox(), mc.player.getEyePosition(1.0f), RotationUtil.getCurrentHitVec(this.attackRange.getProperty().doubleValue()))) != null && hitResult.getType() != net.minecraft.world.phys.HitResult.Type.MISS && (this.throughWalls.getProperty() || RotationUtil.rayCastEntity(3.0) == target);

        boolean inRange = PlayerUtil.isEntityWithinRange(target, attackRange.getProperty().floatValue());

        boolean canAttack = inRange && (rayCast || !raycast.getProperty()) &&
                (!critMode.getProperty().equals("Normal") || !mc.player.onGround()) &&
                (!(critMode.getProperty().equals("Normal") || critMode.getProperty().equals("Force")) ||
                        !isGoingToPerfromCritical(criticalSprint.getProperty(), true));

        renderBlockHit = forceBlockHitAnimation.getProperty();

        if (canAutoBlock) {
            if (!autoBlockMode.is("Modern Watchdog"))
                autoBlock(canAttack);
        } else {
            setBlockState(false);
        }

        if (canAutoBlock && autoBlockMode.is("Modern Watchdog")) {
            if (mc.player.tickCount % 2 == 0 && rayCast) {
                mc.gameMode.attack(mc.player, target);
                mc.player.swing(InteractionHand.MAIN_HAND);
            }

            autoBlock(canAttack);
        }

        if (!canAttack || autoBlock.getProperty() && autoBlockMode.getProperty().toLowerCase().contains("watchdog") &&
                !autoBlockMode.is("Modern Watchdog Post"))
            return;

        // shit code delux
        boolean shouldAttack;

        if (latestCPS.getProperty()) {
            shouldAttack = mc.player.getAttackStrengthScale(0.0f) >= 0.95;
        } else if (autoBlockMode.is("Modern Watchdog Post") && autoBlock.getProperty()) {
            shouldAttack = mc.player.tickCount % 2 == 0;
        } else {
            shouldAttack = cpsTime.finished(getCPS());
        }

        if (shouldAttack) {
            if (onedotEight.getProperty())
                mc.player.swing(InteractionHand.MAIN_HAND);

            mc.gameMode.attack(mc.player, target);

            if (!onedotEight.getProperty())
                mc.player.swing(InteractionHand.MAIN_HAND);

            cpsTime.reset();
            mc.player.resetAttackStrengthTicker();
        }
    }
    
    // thanks balls.
    @EventTarget
    public void onPostMotion(PostMotionEvent event) {
      /*  if (!autoBlock.getProperty() || !autoBlockMode.is("Modern Watchdog Post") || target == null)
            return;

        if (mc.player.getInventory().getSelectedStack().isIn(ItemTags.SWORDS) &&
                PlayerUtil.isEntityWithinRange(target, blockRange.getProperty().floatValue())) {
            for (Hand hand : Hand.values()) {
                if (mc.crosshairTarget.getType() == HitResult.Type.ENTITY &&
                        mc.crosshairTarget instanceof EntityHitResult hitResult)
                    mc.interactionManager.interactEntity(mc.player, hitResult.getEntity(), hand);

                mc.interactionManager.interactItem(mc.player, hand);
            }
            ChatUtil.printDebug("Block");
        }*/
    }

    private boolean isGoingToPerfromCritical(boolean criticalSprint, boolean fallDistance) {
        if (!mc.player.onGround() &&
                !mc.player.onClimbable() &&
                !mc.player.isInWater() &&
                !mc.player.isUnderWater() &&
                !mc.player.hasEffect(MobEffects.BLINDNESS) &&
                !mc.player.isPassenger()) {
            if (!fallDistance || mc.player.fallDistance > 0F) {
                return !criticalSprint || !mc.player.isSprinting();
            }
        }

        return false;
    }

    private void autoBlock(boolean canHit) {
        switch (autoBlockMode.getProperty()) {
            case "Vanilla":
                setBlockState(true);
                break;
            case "Blink":
                if (mc.player.tickCount % 2 == 0) {
                    Astralis.getInstance().getComponentManager().getComponent(BlinkComponent.class).stopBlinking();
                    isBlinking = false;
                    PacketUtil.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN));
                    isBlocking = false;
                } else {
                    Astralis.getInstance().getComponentManager().getComponent(BlinkComponent.class).startBlinking();
                    isBlinking = true;
                    setBlockState(true);
                }
                break;
            case "Modern Watchdog Post":
                if (mc.player.isUsingItem()) {
                    boolean b1 = false;
                    for (int i = 0; i < 9; i++) {
                        if (((KeyBindingAccessor) mc.options.keyHotbarSlots[i]).getTimesPressed() == 0) continue;
                        b1 = true;
                        break;
                    }

                    if (!b1) {
                        int selected = mc.player.getInventory().getSelectedSlot();

                        PacketUtil.send(new ServerboundSetCarriedItemPacket((selected + 1) % 9));
                        mc.player.releaseUsingItem();
                        PacketUtil.send(new ServerboundSetCarriedItemPacket(selected));
                    }
                }

                isBlocking = true;
                break;
            case "Old Watchdog":
                if (cpsTime.finished(getCPS())) {
                    unBlock(true);
                    if (canHit) {
                        mc.gameMode.attack(mc.player, target);
                        mc.player.swing(InteractionHand.MAIN_HAND);
                    }

                    mc.getConnection().getConnection().send(ServerboundInteractPacket.createInteractionPacket(target, mc.player.isShiftKeyDown(), InteractionHand.MAIN_HAND));
                    block(false, true);
                    cpsTime.reset();
                }
                break;
            case "Modern Watchdog":
                BlockHitResult hitResult;

                boolean rayCast = (hitResult = RotationUtil.calculateIntercept(target.getBoundingBox(), mc.player.getEyePosition(1.0f), RotationUtil.getCurrentHitVec(this.attackRange.getProperty().doubleValue()))) != null && hitResult.getType() != net.minecraft.world.phys.HitResult.Type.MISS && (this.throughWalls.getProperty() || RotationUtil.rayCastEntity(3.0) == target);

                switch (autoBlockTicks++) {
                    case 1 -> {
                        if (rayCast)
                            PacketUtil.send(ServerboundInteractPacket.createInteractionPacket(target, mc.player.isShiftKeyDown(), InteractionHand.MAIN_HAND));
                        /*   if (!isBlocking)*/

                        PacketUtil.sendSequenced(sequence -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, sequence, rotations[0], rotations[1]));

                        isBlocking = true;
                        Astralis.getInstance().getComponentManager().getComponent(BlinkComponent.class).stopBlinking();
                        isBlinking = false;
                    }

                    case 2 -> {
                        Astralis.getInstance().getComponentManager().getComponent(BlinkComponent.class).startBlinking();

                        switchAndUnblock(true);

                        isBlocking = false;
                        autoBlockTicks = 1;
                    }
                }
       /*         final int selectedSlot = mc.player.getInventory().getSelectedSlot();
                switch (autoBlockTicks % 2) {
                    case 0: {
                        Astralis.getInstance().getComponentManager().getComponent(BlinkComponent.class).startBlinking();
                        isBlinking = true;

                        if (isBlocking) {
                            PacketUtil.send(new UpdateSelectedSlotC2SPacket(selectedSlot));
                            isBlocking = false;
                        }

                        mc.interactionManager.attackEntity(mc.player, target);
                        mc.player.swingHand(Hand.MAIN_HAND);

                        mc.getNetworkHandler().getConnection().send(PlayerInteractEntityC2SPacket.interact(target, mc.player.isSneaking(), Hand.MAIN_HAND));
                        ((AccessorClientPlayerInteractionManager) mc.interactionManager).invokeSendSequencedPacket(mc.world, sequence -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, sequence, rotations[0], rotations[1]));
                        isBlocking = true;
                        break;
                    }
                    case 1: {
                        Astralis.getInstance().getComponentManager().getComponent(BlinkComponent.class).stopBlinking();
                        isBlinking = false;
                        if (isBlocking) {
                            PacketUtil.send(new UpdateSelectedSlotC2SPacket(selectedSlot % 8 + 1));
                        }

                        autoBlockTicks = -1;
                        break;
                    }
                }

                autoBlockTicks++;*/
            /*    final int selectedSlot = mc.player.getInventory().getSelectedSlot();
                switch (autoBlockTicks++) {
                    case 1 -> {
                        PacketUtil.send(PlayerInteractEntityC2SPacket.interact(target, mc.player.isSneaking(), Hand.MAIN_HAND));
                       if (!isBlocking) {
                           PacketUtil.send(new UpdateSelectedSlotC2SPacket(selectedSlot));
                           PacketUtil.sendSequencedPacket(sequence -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, sequence, rotations[0], rotations[1]));

                           isBlocking = true;
                       }
                        Astralis.getInstance().getComponentManager().getComponent(BlinkComponent.class).stopBlinking();
                        isBlinking = false;
                    }
                    case 2 -> {
                        Astralis.getInstance().getComponentManager().getComponent(BlinkComponent.class).startBlinking();
                        isBlinking = true;
                        if (!isBlocking) {
                            PacketUtil.send(new UpdateSelectedSlotC2SPacket(selectedSlot % 8 + 1));
                        }
                        PacketUtil.send(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));
                        isBlocking = false;
                        autoBlockTicks = 1;
                    }
                }*/
                break;
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
    }

    @EventTarget
    public void onSwordInputEvent(SwordInputEvent event) {
        event.setBlocking(
                target != null &&
                !autoBlockMode.is("Modern Watchdog Post")
                && !autoBlockMode.is("None") && !autoBlockMode.is("Fake") &&
                        RotationUtil.getDistanceToEntityBox(target) <= blockRange.getProperty().floatValue() && autoBlock.getProperty()
        );
    }

    @EventTarget
    public void onSlowDown(SlowDownEvent event) {
        if (autoBlock.getProperty() && autoBlockMode.is("Modern Watchdog") && target != null && RotationUtil.getDistanceToEntityBox(target) <= blockRange.getProperty().floatValue()) {
            event.setCancelled(true);
            event.setSlowDown(1.0f);
        }
    }

    @EventTarget
    public void onPacket2(PacketEvent event) {
    /*    if (Astralis.getInstance().getModuleManager().getModule(BreakerModule.class).isToggled() &&
                Astralis.getInstance().getModuleManager().getModule(BreakerModule.class).rotate) {
            return;
        }
*/
        if (event.getPacket() instanceof ServerboundClientTickEndPacket clientTickEndC2SPacket) {
            if (watchdogKeepSprint.getProperty() && (!disableKeepSprintOnKB.getProperty() || mc.player.hurtTime == 0)) {
                if (!Astralis.getInstance().getComponentManager().getComponent(BlinkComponent.class).isBlinking()) {
                    event.setCancelled(true);
                    PacketUtil.sendNoEvent(clientTickEndC2SPacket);
                } else {
                    ChatUtil.printDebug("Stopped Keep sprint");
                }
            } else {
                ChatUtil.printDebug("Stopped Keep sprint");
            }

            if (autoBlock.getProperty() && autoBlockMode.is("Modern Watchdog Post") && mc.player.getInventory().getSelectedItem().is(ItemTags.SWORDS) && target != null &&
                    PlayerUtil.isEntityWithinRange(target, blockRange.getProperty().floatValue())) {
                /* Astralis.getInstance().getComponentManager().getComponent(BlinkComponent.class).startBlinking();*/

                EntityHitResult hitResult = RayTraceUtil.getHitResult();

                boolean isRaycastSafe =
                        hitResult != null && hitResult.getEntity() == target;
                mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
                if (isRaycastSafe)
                    mc.gameMode.interact(mc.player, hitResult.getEntity(), InteractionHand.MAIN_HAND);

                /*  ChatUtil.printDebug("block");*/
            }
        }

        if (event.getPacket() instanceof ServerboundPlayerActionPacket packet && autoBlock.getProperty()
                && autoBlockMode.is("Old Watchdog") && killAuraTicks < 3 && packet.getAction().equals(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM)
        ) {
            event.setCancelled(true);
        }
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if (!esp.getProperty() || target == null)
            return;

        Render3DUtil.drawCircleESP(event.getMatricies(), target);
    }

    private void setBlockState(boolean state) {
        if (state) {
            block(interactWhenBlocking.getProperty(), packetAutoBlock.getProperty());
        } else {
            unBlock(packetAutoBlock.getProperty());

            if (isBlinking) {
                Astralis.getInstance().getComponentManager().getComponent(BlinkComponent.class).stopBlinking();
                isBlinking = false;
            }
        }
    }

    private void block(boolean interact, boolean packet) {
        if (interact)
            mc.gameMode.interact(mc.player, target, InteractionHand.MAIN_HAND);

        if (packet) {
            PacketUtil.sendSequenced(sequence -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, sequence, rotations[0], rotations[1]));
        } else {
            mc.options.keyUse.setDown(true);
        }

        renderBlockHit = true;
        isBlocking = true;
    }

    private void unBlock(boolean packet) {
        boolean canUnBlock = isBlocking && mc.player.getInventory().getSelectedItem().is(ItemTags.SWORDS);

        if (autoBlockMode.is("Old Watchdog")) {
            if (isBlocking) {
                final int selectedSlot = mc.player.getInventory().getSelectedSlot();
                PacketUtil.send(new ServerboundSetCarriedItemPacket(selectedSlot % 8 + 1));
                PacketUtil.send(new ServerboundSetCarriedItemPacket(selectedSlot));
                isBlocking = false;
                killAuraTicks = 0;
            }
        }

        if (canUnBlock) {
            if (autoBlockMode.is("Modern Watchdog")) {
                switchAndUnblock(false);
            } else {
                if (packet) {
                    PacketUtil.sendNoEvent(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN));
                } else {
                    mc.options.keyUse.setDown(false);
                }
            }
        }

        renderBlockHit = false;
        isBlocking = false;
    }

    private void switchAndUnblock(boolean isBlinking) {
        int selected = mc.player.getInventory().getSelectedSlot();
        int random = RandomUtil.getAdvancedRandomInt(1, 5);
        int randomNormalized = (selected + random) % 9;
        ChatUtil.printDebug("Switching to " + randomNormalized + " base random: " + random);
        PacketUtil.send(new ServerboundSetCarriedItemPacket(randomNormalized));

        this.isBlinking = isBlinking;

        PacketUtil.send(new ServerboundSetCarriedItemPacket(selected));
        PacketUtil.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN));
    }

    private long getCPS() {
        return (long) (1000 / (
                RandomUtil.getAdvancedRandom(minCPS.getProperty().floatValue(), maxCPS.getProperty().floatValue()))
        );
    }

    public boolean shouldRenderFakeAnim() {
        return target != null && autoBlock.getProperty() && renderBlockHit;
    }

    private float generateYawNoise() {
        return (float) (RandomUtil.getAdvancedRandom(-maxYawNoise.getProperty().floatValue(), maxYawNoise.getProperty().floatValue())
                + Math.sin(System.currentTimeMillis() % 2000 / 200.0) * 0.5f);
    }

    private float generatePitchNoise() {
        return (float) (RandomUtil.getAdvancedRandom(-maxPitchNoise.getProperty().floatValue(), maxPitchNoise.getProperty().floatValue())
                + Math.cos(System.currentTimeMillis() % 1750 / 150.0) * 0.3f);
    }
}