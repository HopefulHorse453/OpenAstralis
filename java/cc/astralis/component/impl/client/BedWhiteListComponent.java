package cc.astralis.component.impl.client;

import cc.astralis.component.Component;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.UpdateEvent;
import cc.astralis.event.events.impl.network.PacketEvent;
import cc.astralis.util.render.ChatUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.world.level.block.BedBlock;

public class BedWhiteListComponent extends Component {
    private static final List<BlockPos> WHITELISTED = new ArrayList<>();

    private boolean check = true;

    public static boolean isWhitelisted(BlockPos pos) {
        if (pos == null) return false;
        for (BlockPos bp : WHITELISTED) {
            if (bp.distToLowCornerSqr(pos.getX(), pos.getY(), pos.getZ()) < 1.5D) return true;
        }
        return false;
    }

    public static void clear() {
        WHITELISTED.clear();
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (!check || mc.player == null || mc.level == null) return;

        BlockPos bed = findBed();
        if (bed == null) return;

        WHITELISTED.clear();
        WHITELISTED.add(bed);
        for (Direction direction : Direction.values()) WHITELISTED.add(bed.relative(direction));
        check = false;
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (!(event.getPacket() instanceof ClientboundSystemChatPacket gameMessage)) return;

        String text = gameMessage.content().getString();
        if (text != null && text.contains("Protect your bed")) {
            WHITELISTED.clear();
            check = true;
        }
    }

    private BlockPos findBed() {
        for (BlockPos pos : BlockPos.betweenClosed(mc.player.blockPosition().offset(-16, -16, -16), mc.player.blockPosition().offset(16, 16, 16))) {
            if (mc.level.getBlockState(pos).getBlock() instanceof BedBlock) {
                return pos;
            }
        }
        return null;
    }
}