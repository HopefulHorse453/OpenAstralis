package cc.astralis.module.impl.player.nofall;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.PostMotionEvent;
import astralis.mixin.accessor.entity.EntityAccessor;
import cc.astralis.module.Module;
import cc.astralis.module.SubModule;
import cc.astralis.util.network.PacketUtil;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class UniversalNofall extends SubModule {

    public UniversalNofall(Module parentClass){
        super(parentClass,"Universal");
    }

    @EventTarget
    public void onPostMotion(PostMotionEvent event) {
            double motionY = mc.player.getDeltaMovement().y;
            if (mc.player.fallDistance > 3 && motionY < 0 && ((EntityAccessor) mc.player).callCollide(mc.player.getDeltaMovement()).y > motionY) {
                PacketUtil.send(new ServerboundMovePlayerPacket.PosRot(mc.player.getX(), mc.player.getY() + 1E-14, mc.player.getZ(), mc.player.getYRot(), mc.player.getXRot(),
                        false,
                        false
                ));
            }
        }
    }
