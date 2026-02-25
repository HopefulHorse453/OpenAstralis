package astralis.mixin.network;

import cc.astralis.Astralis;
import cc.astralis.interfaces.IAccess;
import cc.astralis.module.impl.combat.VelocityModule;
import cc.astralis.util.render.ChatUtil;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientboundSetEntityMotionPacket.class)
public class MixinEntityVelocityUpdateS2CPacket implements IAccess {

   /* @Inject(method = "apply", at = @At("HEAD"), cancellable = true)
    private void onApply(ClientPlayPacketListener listener, CallbackInfo ci) {
        if (mc.player == null)
            return;

        EntityVelocityUpdateS2CPacket packet = (EntityVelocityUpdateS2CPacket)(Object)this;

        if (packet.getEntityId() != mc.player.getId()) {
            return;
        }

        VelocityModule velocityModule = Astralis.getInstance().getModuleManager().getModule(VelocityModule.class);

        if (velocityModule == null || !velocityModule.isToggled() || !velocityModule.mode.is("Club"))
            return;

        ChatUtil.print("velo");

        ci.cancel();

        if (mc.player.isOnGround()) {
            mc.player.setSprinting(true);
            mc.player.jump();
        } else {
            double x = packet.getVelocityX() * 0.6;
            double y = packet.getVelocityY();
            double z = packet.getVelocityZ() * 0.6;

            mc.player.setVelocity(x, y, z);
        }
    }*/
}