package cc.astralis.component.impl.network;

import cc.astralis.component.Component;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.UpdateEvent;
import cc.astralis.event.events.impl.network.PacketEvent;
import cc.astralis.event.events.impl.render.Render2DEvent;
import cc.astralis.event.types.EventModes;
import com.mojang.blaze3d.platform.Window;
import java.util.LinkedList;
import java.util.Queue;

public class PacketLossDetector extends Component {
    private final Queue<Long> packetTimings = new LinkedList<>();
    private boolean packetLossDetected;
    private double packetLossPercentage;
    private long lastPacketReceivedTime;

    private static final int SAMPLE_SIZE = 300;
    private static final double LOSS_THRESHOLD = 0.05;
    private static final long MINIMUM_LOSS_TIME_MS = 300;
    private static final long TIMEOUT_MS = 2500;

    private long totalLostTimeMs = 0;

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (mc.player.tickCount < 200 || mc.isLocalServer()) {
            packetTimings.clear();
            packetLossPercentage = 0;
            packetLossDetected = false;
            totalLostTimeMs = 0;
            lastPacketReceivedTime = 0;
            return;
        }

        if (event.getEventMode() == EventModes.RECEIVE) {
            long currentTime = System.currentTimeMillis();
            packetTimings.add(currentTime);

            if (packetTimings.size() > SAMPLE_SIZE) {
                packetTimings.poll();
            }

            lastPacketReceivedTime = currentTime;

            calculatePacketLoss();
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (!packetTimings.isEmpty() &&
                System.currentTimeMillis() - lastPacketReceivedTime > TIMEOUT_MS) {
            packetLossDetected = true;
            packetLossPercentage = 1.0;
            totalLostTimeMs = TIMEOUT_MS;
        } else if (packetLossPercentage > LOSS_THRESHOLD && totalLostTimeMs >= MINIMUM_LOSS_TIME_MS) {
            packetLossDetected = true;
        } else {
            packetLossDetected = false;
        }
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (!packetLossDetected) return;
        Window window = mc.getWindow();

        final int iconSize = 40;
        /*render.drawTexture(
                IdentifierAccessor.createIdentifier("astralis", "lag/lag2.png"),
                (window.getScaledWidth() - iconSize) / 2,
                (window.getScaledHeight() / 2) - iconSize - 5,
                iconSize, iconSize, 0, 0
        );*/
    }

    private void calculatePacketLoss() {
        if (packetTimings.size() < 2) {
            packetLossPercentage = 0;
            totalLostTimeMs = 0;
            return;
        }

        Long[] timings = packetTimings.toArray(new Long[0]);

        int lostPackets = 0;
        int intervals = 0;
        long expectedInterval = 50;
        totalLostTimeMs = 0;

        for (int i = 1; i < timings.length; i++) {
            long delta = timings[i] - timings[i - 1];
            int missing = (int) (delta / expectedInterval) - 1;

            if (missing > 0) {
                lostPackets += missing;
                totalLostTimeMs += missing * expectedInterval;
            }

            intervals++;
        }

        int totalPackets = intervals + lostPackets;

        if (totalPackets <= 0) {
            packetLossPercentage = 0;
        } else {
            packetLossPercentage = (double) lostPackets / totalPackets;
        }
    }
}
