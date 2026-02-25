package cc.astralis.module.impl.combat.backtrack;

import cc.astralis.Astralis;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.EntityInteractEvent;
import cc.astralis.event.events.impl.game.TickEvent;
import cc.astralis.event.events.impl.network.PacketEvent;
import cc.astralis.event.events.impl.render.Render3DEvent;
import cc.astralis.event.events.impl.game.WorldChangeEvent;
import cc.astralis.event.types.EventModes;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.property.properties.*;
import cc.astralis.util.network.PacketUtil;
import cc.astralis.util.player.PlayerUtil;
import cc.astralis.util.render.ChatUtil;
import cc.astralis.util.render.Render3DUtil;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.VecDeltaCodec;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

// holy skid.
public class BackTrackModule extends Module {

    private final NumberProperty range = new NumberProperty("Range", 3f, 1f, 10f, 0.1f);
    private final NumberProperty delayMin = new NumberProperty("MS Min", 100, 0, 1000, 1);
    private final NumberProperty delayMax = new NumberProperty("MS Max", 150, 0, 1000, 1);
    private final NumberProperty trackingBuffer = new NumberProperty("Tracking Timing", 500, 0, 2000, 1);
    private final NumberProperty chance = new NumberProperty("Chance", 50, 0, 100, 1);

    private final BooleanProperty pauseOnHurtTime = new BooleanProperty("Pause On HurtTime", false);
    private final NumberProperty hurtTime = new NumberProperty("Hurt Time", 3, 0, 10, 1)
            .setVisible(pauseOnHurtTime::getProperty);

    private final ModeProperty targetMode = new ModeProperty("Target Mode", "Attack", "Attack", "Range");
    private final NumberProperty lastAttackTimeToWork = new NumberProperty("Last Attack Time To Work", 1000, 0, 5000, 1);

    private final ConcurrentLinkedQueue<DelayData> delayedPacketQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<Packet<?>> packetProcessQueue = new ConcurrentLinkedQueue<>();

    private Entity target;
    private VecDeltaCodec position = null;

    private long currentDelay;
    private long trackingBufferTime;
    private long lastAttackTime;
    private boolean shouldPause = false;
    private int currentChance;
    private final Random random = new Random();

    public BackTrackModule() {
        super(Category.COMBAT);
        registerProperties(
                range, delayMin, delayMax, trackingBuffer,
                chance, pauseOnHurtTime, hurtTime,
                targetMode, lastAttackTimeToWork
        );
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (event.getEventMode() != EventModes.RECEIVE || event.isCancelled()) return;
        Packet<?> packet = event.getPacket();

        if (packet instanceof ServerboundChatPacket || packet instanceof ServerboundChatCommandPacket || packet instanceof ClientboundSystemChatPacket)
            return;

        if (packet instanceof ClientboundPlayerPositionPacket || packet instanceof ClientboundDisconnectPacket) {
            clear(true);
            return;
        }

        if (packet instanceof ClientboundSetHealthPacket && ((ClientboundSetHealthPacket) packet).getHealth() <= 0) {
            ChatUtil.print("release");
            clear(true);
            return;
        }

        if (packet instanceof ClientboundSoundPacket soundPacket && soundPacket.getSound().value() == SoundEvents.PLAYER_HURT) {
            return;
        }

        if (target != null) {
            boolean isEntityPacket = packet instanceof ClientboundMoveEntityPacket && ((ClientboundMoveEntityPacket) packet).getEntity(mc.level) == target;
            boolean isPositionPacket = packet instanceof ClientboundTeleportEntityPacket && ((ClientboundTeleportEntityPacket) packet).id() == target.getId();

            if (isEntityPacket || isPositionPacket) {
                Vec3 newPos = null;
                if (packet instanceof ClientboundMoveEntityPacket entityPkt && position != null) {
                    newPos = position.decode(entityPkt.getXa(), entityPkt.getYa(), entityPkt.getZa());
                } else if (packet instanceof ClientboundTeleportEntityPacket posPkt) {
                    newPos = new Vec3(posPkt.change().position().x, posPkt.change().position().y, posPkt.change().position().z);
                }

                if (newPos != null) {
                    if (position == null) position = new VecDeltaCodec();
                    position.setBase(newPos);

                    double trackedDist = newPos.distanceToSqr(mc.player.position());
                    double actualDist = target.distanceToSqr(mc.player);

                    if (trackedDist < actualDist) {
                        flushDelayedPackets();
                        return;
                    }
                }
            }
        }

        event.setCancelled(true);
        delayedPacketQueue.add(new DelayData(packet, System.currentTimeMillis()));
    }

    @EventTarget
    public void onAttack(EntityInteractEvent event) {
        Entity enemy = event.getTarget();
        if (enemy == null) return;

        lastAttackTime = System.currentTimeMillis();
        currentChance = random.nextInt(101);

        if (targetMode.is("Attack")) {
            processTarget(enemy);
        }
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (event.getEventMode() == EventModes.POST) return;
        this.setSuffix(currentDelay + "ms");

        if (targetMode.is("Range")) {
            LivingEntity rangeTarget = PlayerUtil.getTargets(false, false, false, true, false, range.getProperty().floatValue()).getFirst();
            if (rangeTarget != null /*&& (rangeTarget.isAlive() && !rangeTarget.isDead() && rangeTarget.getHealth() > 0)*/)
                processTarget(rangeTarget);
            else
                clear();
        }

        if (shouldCancelPackets()) processPackets(false);
        else clear();

        packetProcessQueue.removeIf(pkt -> {
            PacketUtil.handlePacket(pkt);
            return true;
        });

        if (arePacketQueuesEmpty()) currentDelay = getRandomDelay();
    }

    @EventTarget
    public void onRender(Render3DEvent event) {
        if (position != null && target != null) {
            EntityDimensions dims = target.getDimensions(target.getPose());
            double d = dims.width() / 2.0;
            AABB box = new AABB(-d, 0, -d, d, dims.height(), d).inflate(0.05);
            Vec3 pos = position.getBase();
            if (pos != null)
                Render3DUtil.drawBoxESP(event.getMatricies(), box.move(pos), Astralis.getInstance().getFirstColor(), 200);
        }
    }

    @EventTarget
    public void onWorldChange(WorldChangeEvent event) {
        clear(true);
    }

    private void processTarget(Entity enemy) {
        shouldPause = enemy instanceof LivingEntity living && pauseOnHurtTime.getProperty() && living.hurtTime >= hurtTime.getProperty().intValue();

        if (!shouldBacktrack(enemy)) return;

        if (enemy != target) {
            clear(false);
            position = new VecDeltaCodec();
            position.setBase(enemy.position());
        }

        target = enemy;
    }

    private boolean shouldBacktrack(Entity target) {
        double dist = mc.player.distanceTo(target);
        boolean inRange = dist <= range.getProperty().floatValue();

        if (inRange) trackingBufferTime = System.currentTimeMillis();

        boolean bufferValid = (System.currentTimeMillis() - trackingBufferTime) <= trackingBuffer.getProperty().longValue();
        boolean chanceCheck = currentChance < chance.getProperty().intValue();
        boolean notPaused = !shouldPause;
        boolean recentAttack = (System.currentTimeMillis() - lastAttackTime) <= lastAttackTimeToWork.getProperty().longValue();

        return (inRange || bufferValid) && target.isAlive() && mc.player.tickCount > 10 && chanceCheck && notPaused && recentAttack;
    }

    private void processPackets(boolean flush) {
        long now = System.currentTimeMillis();
        delayedPacketQueue.removeIf(data -> {
            if (flush || data.getTimestamp() <= now - currentDelay) {
                PacketUtil.handlePacket(data.getPacket());
                return true;
            }
            return false;
        });
    }

    private void clear(boolean flush) {
        if (flush) {
            flushDelayedPackets();
        } else {
            delayedPacketQueue.clear();
        }
        packetProcessQueue.clear();
        target = null;
        position = null;
        shouldPause = false;
    }

    public void flushDelayedPackets() {
        while (!delayedPacketQueue.isEmpty()) {
            DelayData data = delayedPacketQueue.poll();
            if (data != null) {
                PacketUtil.handlePacket(data.getPacket());
            }
        }
    }

    private void clear() { clear(true); }

    private boolean shouldCancelPackets() {
        return target != null && target.isAlive() && shouldBacktrack(target);
    }

    private boolean arePacketQueuesEmpty() {
        return delayedPacketQueue.isEmpty() && packetProcessQueue.isEmpty();
    }

    private long getRandomDelay() {
        int min = delayMin.getProperty().intValue();
        int max = delayMax.getProperty().intValue();
        return min + random.nextInt(max - min + 1);
    }

    @Override
    public void onEnable() {
        currentDelay = getRandomDelay();
        currentChance = random.nextInt(101);
        clear(false);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        flushDelayedPackets();
        packetProcessQueue.clear();
        target = null;
        position = null;
        shouldPause = false;
        super.onDisable();
    }

    private static class DelayData {
        private final Packet<?> packet;
        private final long timestamp;

        public DelayData(Packet<?> packet, long timestamp) {
            this.packet = packet;
            this.timestamp = timestamp;
        }

        public Packet<?> getPacket() { return packet; }
        public long getTimestamp() { return timestamp; }
    }
}
