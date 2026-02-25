package cc.astralis.module.impl.combat.velocity;

import cc.astralis.Astralis;
import cc.astralis.component.impl.network.BlinkComponent;
import cc.astralis.component.impl.player.RotationComponent;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.*;
import cc.astralis.event.events.impl.input.InputTickEvent;
import cc.astralis.event.events.impl.network.PacketEvent;
import cc.astralis.event.types.EventModes;
import cc.astralis.module.Module;
import cc.astralis.module.SubModule;
import cc.astralis.module.impl.combat.KillauraModule;
import cc.astralis.property.properties.BooleanProperty;
import cc.astralis.property.properties.NumberProperty;
import cc.astralis.util.math.TimeUtil;
import cc.astralis.util.player.RotationUtil;
import cc.astralis.util.render.ChatUtil;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.common.ClientboundTransferPacket;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ClientboundDebugSamplePacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatEndPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatEnterPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerLookAtPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerRotationPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundSetSimulationDistancePacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ClientboundTickingStatePacket;
import net.minecraft.network.protocol.game.ClientboundTickingStepPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket;
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.concurrent.LinkedBlockingQueue;

// this shi is pasted
public class ModernWatchdogVelocity extends SubModule {
    public NumberProperty delayTicks = new NumberProperty("Delay Ticks", 1, 0, 100, 1.0f);
    private final NumberProperty worldChangeTimeout = new NumberProperty("World Change Timeout", 5000, 0, 10000, 1);
    public BooleanProperty delayAllPackets = new BooleanProperty("Delay All Packets", true);
    public BooleanProperty cancelS27 = new BooleanProperty("Cancel Explosion", false);
    public BooleanProperty skipS27 = new BooleanProperty("Ignore Explosion", false);
    public BooleanProperty autoJumpReset = new BooleanProperty("Jump Reset", false);
    public BooleanProperty fuckU = new BooleanProperty("Blink", false);
    public BooleanProperty attackReduce = new BooleanProperty("Attack Reduce (experimental)", false);
    public BooleanProperty swapBeforeAttack = new BooleanProperty("Switch Attack (experimental)", false);
    private final BooleanProperty disableVelocityOnFlag = new BooleanProperty("Disable Velocity on Flag", false);

    private final TimeUtil switchedWorldsTime = new TimeUtil();

    private int count, attackCount, stuckTicks;
    private boolean strict = false, isDelaying = false;
    private boolean s27 = false, jump = false;
    private boolean actualServerSprint = false, actuallyAttacked = false;;

    private final LinkedBlockingQueue<Packet<ClientGamePacketListener>> packets = new LinkedBlockingQueue<>();

    public ModernWatchdogVelocity(Module parent) {
        super(parent, "Modern Watchdog");
        registerPropertiesToParentClass(
                delayTicks,
                worldChangeTimeout,
                delayAllPackets,
                cancelS27,
                skipS27,
                autoJumpReset,
                fuckU,
                attackReduce,
                swapBeforeAttack,
                disableVelocityOnFlag
        );
    }

    @EventTarget
    public void onInputTick(InputTickEvent event) {
        if (shouldReturn()) {
            return;
        }

        /*if (mc.player.hurtTime > 0) {
            if (Math.random() * 100 < chance.getProperty().intValue()) {
                event.up = true;
                if (mc.player.hurtTime == 9) {
                    event.jump = true;
                }
            }
        }*/

        if (this.jump && this.stuckTicks == 0) {
            event.jump = this.jump;
            this.jump = false;
        }

        if (this.strict) {
            event.up = true;
            event.left = false;
            event.right = false;
        }
    }

    private void release() {
        while (!this.packets.isEmpty()) {
            try {
                Packet<ClientGamePacketListener> packet = this.packets.poll();
                packet.handle(mc.getConnection());
            } catch (Exception var2) {
                var2.printStackTrace();
            }
        }

        if (this.fuckU.getProperty()) {
            Astralis.getInstance().getComponentManager().getComponent(BlinkComponent.class).stopBlinking();
        }

        this.isDelaying = false;
    }

 /*   @EventTarget
    public void onMovementModify(ModifyMovementEvent event) {
        if (shouldReturn()) {
            return;
        }

        if (this.strict) {
          *//*  event.setMovementForward(1.0f);
            event.setMovementSideways(0.0f);*//*
        }
    }*/

    @EventTarget
    public void onPostMotion(PostMotionEvent event) {
        if (shouldReturn()) {
            return;
        }

        if (this.isDelaying ) {
            this.count++;
            if (this.autoJumpReset.getProperty() && mc.player.onGround() && this.count > 1) {
                this.jump = true;
                this.isDelaying = false;
                this.count = 0;
                this.release();
                return;
            }

            if (this.count > this.delayTicks.getProperty().intValue()) {
                this.isDelaying = false;
                this.count = 0;
                this.release();
            }
        }
    }

    @EventTarget
    public void onSwitchWorld(WorldChangeEvent event) {
        switchedWorldsTime.reset();
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (shouldReturn()) {
            return;
        }

        if (this.attackCount > 0) {
            RotationComponent.setFixKeyBinds(false);

            boolean serverSprint = actualServerSprint;
            if (!actuallyAttacked && serverSprint) {
                Entity result = RotationUtil.rayCastEntity(3.0);
                if (result == null) {
                    result = KillauraModule.target;
                    if (result != null) {
                        BlockHitResult hitResult = RotationUtil.calculateIntercept(
                                result.getBoundingBox(), mc.player.getEyePosition(1.0F), RotationUtil.getCurrentHitVec(3.0)
                        );
                        boolean rayCast = hitResult != null && hitResult.getType() != HitResult.Type.MISS;
                        if (!rayCast) {
                            result = null;
                        }
                    }
                }

                if (result instanceof AbstractClientPlayer) {
                    if (this.swapBeforeAttack.getProperty()) {
                        mc.getConnection().send(new ServerboundSetCarriedItemPacket((mc.player.getInventory().getSelectedSlot() + 1) % 9));
                        mc.getConnection().send(new ServerboundSetCarriedItemPacket(mc.player.getInventory().getSelectedSlot()));
                    }

                    mc.player.setSprinting(false);
                    mc.player.setDeltaMovement(mc.player.getDeltaMovement().multiply(0.6, 1.0, 0.6));
                    mc.getConnection().send(ServerboundInteractPacket.createAttackPacket(result, mc.player.isShiftKeyDown()));
                    mc.player.swing(InteractionHand.MAIN_HAND);
                }
            }

            this.attackCount--;
        } else if (this.strict) {
            RotationComponent.setFixKeyBinds(true);
            this.strict = false;
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (shouldReturn()) {
            return;
        }

        if (event.getPacket() instanceof ClientboundPlayerPositionPacket playerPositionLookS2CPacket && disableVelocityOnFlag.getProperty()) {
            switchedWorldsTime.reset();
            ChatUtil.printDebug("reset");
        }

        Packet<?> packet = event.getPacket();
        if (packet instanceof ServerboundInteractPacket c02) {
            c02.dispatch(new ServerboundInteractPacket.Handler() {
                @Override
                public void onInteraction(InteractionHand hand) {
                }

                @Override
                public void onInteraction(InteractionHand hand, Vec3 pos) {
                }

                @Override
                public void onAttack() {
                    actuallyAttacked = true;
                }
            });
        }

        ServerboundPlayerCommandPacket c0b;

        if (event.getPacket() instanceof ServerboundPlayerCommandPacket && (c0b = (ServerboundPlayerCommandPacket)event.getPacket()).getId() == mc.player.getId()) {
            if (c0b.getAction() == ServerboundPlayerCommandPacket.Action.START_SPRINTING) {
                this.actualServerSprint = true;
            } else if (c0b.getAction() == ServerboundPlayerCommandPacket.Action.STOP_SPRINTING) {
                this.actualServerSprint = false;
            }
        }

        if (event.getEventMode() != EventModes.RECEIVE) {
            return;
        }

        if (!event.isCancelled()) {
            if (this.isDelaying) {
                if (this.delayAllPackets.getProperty()
                        || !(event.getPacket() instanceof ClientboundMoveEntityPacket)
                        && !(event.getPacket() instanceof ClientboundEntityPositionSyncPacket)
                        && !(event.getPacket() instanceof ClientboundSetEntityMotionPacket s12 && s12.getId() != mc.player.getId())
                        && !(event.getPacket() instanceof ClientboundEntityEventPacket)
                        && !(event.getPacket() instanceof ClientboundPongResponsePacket)
                        && !(event.getPacket() instanceof ClientboundSetEquipmentPacket)
                        && !(event.getPacket() instanceof ClientboundUpdateMobEffectPacket)
                        && !(event.getPacket() instanceof ClientboundSetEntityDataPacket)
                        && !(event.getPacket() instanceof ClientboundDamageEventPacket)
                        && !(event.getPacket() instanceof ClientboundSetEntityLinkPacket)
                        && !(event.getPacket() instanceof ClientboundSetPassengersPacket)
                        && !(event.getPacket() instanceof ClientboundUpdateAttributesPacket)
                        && !(event.getPacket() instanceof ClientboundAnimatePacket)
                        && !(event.getPacket() instanceof ClientboundKeepAlivePacket)
                        && !(event.getPacket() instanceof ClientboundTransferPacket)
                        && !(event.getPacket() instanceof ClientboundBlockUpdatePacket)
                        && !(event.getPacket() instanceof ClientboundBlockEntityDataPacket)
                        && !(event.getPacket() instanceof ClientboundTeleportEntityPacket)
                        && !(event.getPacket() instanceof ClientboundStatusResponsePacket)
                        && !(event.getPacket() instanceof ClientboundTickingStatePacket)
                        && !(event.getPacket() instanceof ClientboundSetSimulationDistancePacket)
                        && !(event.getPacket() instanceof ClientboundTickingStepPacket)
                        && !(event.getPacket() instanceof ClientboundLevelEventPacket)
                        && !(event.getPacket() instanceof ClientboundMoveVehiclePacket)
                        && !(event.getPacket() instanceof ClientboundUpdateRecipesPacket)
                        && !(event.getPacket() instanceof ClientboundSetCameraPacket)
                        && !(event.getPacket() instanceof ClientboundPlayerRotationPacket)
                        && !(event.getPacket() instanceof ClientboundPlayerLookAtPacket)
                        && !(event.getPacket() instanceof ClientboundSetHealthPacket)
                        && !(event.getPacket() instanceof ClientboundExplodePacket)
                        && !(event.getPacket() instanceof ClientboundRotateHeadPacket)
                        && !(event.getPacket() instanceof ClientboundDebugSamplePacket)
                        && !(event.getPacket() instanceof ClientboundPlayerCombatEnterPacket)
                        && !(event.getPacket() instanceof ClientboundPlayerCombatEndPacket)
                        && !(event.getPacket() instanceof ClientboundSectionBlocksUpdatePacket)) {
                    if (this.delayAllPackets.getProperty()
                            || event.getPacket() instanceof ClientboundPingPacket
                            || event.getPacket() instanceof ClientboundExplodePacket
                            || event.getPacket() instanceof ClientboundSetEntityMotionPacket s12x && s12x.getId() == mc.player.getId()) {
                        event.setCancelled(true);
                        // unchecked
                        this.packets.add((Packet<ClientGamePacketListener>) event.getPacket());
                    }

                    if (event.getPacket() instanceof ClientboundPlayerPositionPacket) {
                        this.release();
                        this.stuckTicks = 0;
                        this.count = 0;
                        this.isDelaying = false;
                        this.jump = false;
                    }
                }
            } else {
                if (event.getPacket() instanceof ClientboundSetEntityMotionPacket s2CPacket && s2CPacket.getId() == mc.player.getId()) {
                    if (this.s27) {
                        this.s27 = false;
                        if (this.cancelS27.getProperty()) {
                            event.setCancelled(true);
                            return;
                        }

                        if (this.skipS27.getProperty()) {
                            return;
                        }
                    }

                    if (this.attackReduce.getProperty()) {
                        this.strict = true;
                        this.attackCount = 2;
                    }

                    if (this.delayTicks.getProperty().intValue() == 0.0) {
                        if (mc.player.onGround()) {
                            this.jump = true;
                        }
                    } else if (this.stuckTicks <= 0) {
                        if (this.fuckU.getProperty()) {
                            Astralis.getInstance().getComponentManager().getComponent(BlinkComponent.class).startBlinking();
                        }

                        this.count = 0;
                        this.isDelaying = true;
                        this.packets.add(s2CPacket);
                        event.setCancelled(true);
                    }
                }
            }

            if (event.getPacket() instanceof ClientboundExplodePacket) {
                if (this.cancelS27.getProperty()) {
                    event.setCancelled(true);
                }

                this.s27 = true;
            }
        }
    }

    private boolean shouldReturn() {
        return !switchedWorldsTime.finished(worldChangeTimeout.getProperty().longValue());
    }
}