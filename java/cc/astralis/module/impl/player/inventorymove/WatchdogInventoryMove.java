package cc.astralis.module.impl.player.inventorymove;

import cc.astralis.Astralis;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.UpdateEvent;
import cc.astralis.event.events.impl.network.PacketEvent;
import cc.astralis.event.types.EventModes;
import cc.astralis.module.Module;
import cc.astralis.module.SubModule;
import cc.astralis.module.impl.combat.KillauraModule;
import cc.astralis.module.impl.player.InventoryMoveModule;
import cc.astralis.util.network.PacketUtil;
import java.util.ArrayDeque;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.inventory.ClickType;

import static org.apache.commons.lang3.ArrayUtils.contains;

public class WatchdogInventoryMove extends SubModule {
    private final InventoryMoveModule parentClass;

    private final ClickType[] allowedActions = new ClickType[]{
            ClickType.QUICK_MOVE,
            ClickType.SWAP,
            ClickType.THROW
    };

    private boolean shouldBlink = false;
    private final ArrayDeque<Packet<?>> packetDeque = new ArrayDeque<>();

    public WatchdogInventoryMove(Module parentClass) {
        super(parentClass, "Modern Watchdog");
        this.parentClass = (InventoryMoveModule) parentClass;
    }

    @EventTarget
    public void onUpdate(UpdateEvent e) {
        KillauraModule killauraModule = Astralis.getInstance().getModuleManager().getModule(KillauraModule.class);
        if (mc.player == null || mc.player.isUsingItem() || mc.player.isBlocking() || !killauraModule.validTargets.isEmpty()) return;
        Screen screen = mc.screen;
        if (screen == null || !parentClass.ScreenCheck()) return;
        KeyMapping.setAll();
    }

    // yes else if else if else trol made that else if spam thx
    @EventTarget
    public void onPacket(PacketEvent e) {
        KillauraModule killauraModule = Astralis.getInstance().getModuleManager().getModule(KillauraModule.class);
        if (mc.player == null || mc.level == null || mc.player.isUsingItem() || mc.player.isBlocking() || !killauraModule.validTargets.isEmpty()) return;

        if (e.getPacket() instanceof ServerboundContainerClickPacket packet) {
            if (packet.containerId() == mc.player.inventoryMenu.containerId && contains(allowedActions, packet.clickType())) {
                PacketUtil.send(new ServerboundContainerClosePacket(packet.containerId()));
            } else {
                shouldBlink = true;
            }
        } else if (e.getPacket() instanceof ServerboundContainerClosePacket) {
            shouldBlink = false;
        } else if (e.getPacket() instanceof ClientboundLoginPacket || e.getPacket() instanceof ClientboundPlayerPositionPacket) {
            packetDeque.clear();
            shouldBlink = false;
        } else if (shouldBlink && e.getEventMode() == EventModes.SEND && !(e.getPacket() instanceof ServerboundKeepAlivePacket) && !(e.getPacket() instanceof ServerboundPongPacket)) {
            packetDeque.add(e.getPacket());
            e.setCancelled(true);
        }

        if (mc.screen == null) {
            shouldBlink = false;
            dequeue();
        }
    }

    private void dequeue() {
        if (shouldBlink) return;

        if (mc.getConnection() != null) {
            while (!packetDeque.isEmpty()) {
                Packet<?> packet = packetDeque.pollFirst();
                if (packet != null) {
                    PacketUtil.sendNoEvent(packet);
                }
            }
        }
    }
}
