package astralis.mixin.player;

import cc.astralis.Astralis;
import cc.astralis.event.events.impl.game.VelocityUpdateEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPlayNetworkHandlerMixin extends ClientCommonPacketListenerImpl {

    @Unique
    private Minecraft mc = Minecraft.getInstance();

    public ClientPlayNetworkHandlerMixin(Minecraft client, Connection connection, CommonListenerCookie connectionState) {
        super(client, connection, connectionState);
    }

    @Inject(
            method = "handleSetEntityMotion",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;lerpMotion(Lnet/minecraft/world/phys/Vec3;)V"),
            cancellable = true
    )
    private void onEntityVelocityUpdate(ClientboundSetEntityMotionPacket packet, CallbackInfo ci) {
        if (mc == null)
            return;

        if (mc.player == null || packet.getId() != mc.player.getId()) return;

        Vec3 originalVelocity = packet.getMovement();
        VelocityUpdateEvent event = new VelocityUpdateEvent(originalVelocity.x, originalVelocity.y, originalVelocity.z, false);
        Astralis.getInstance().getEventManager().call(event);

        if (!event.isCancelled()) {
            Vec3 updatedVelocity = new Vec3(event.getVelocityX(), event.getVelocityY(), event.getVelocityZ());
            mc.player.lerpMotion(updatedVelocity);
        }

        ci.cancel();
    }

    @Inject(
            method = "handleExplosion",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundExplodePacket;playerKnockback()Ljava/util/Optional;"),
            cancellable = true
    )
    private void modifyExplosionVelocity(ClientboundExplodePacket packet, CallbackInfo ci) {
        if (mc.player == null) return;

        packet.playerKnockback().ifPresent(knockback -> {
            Vec3 combinedVelocity = mc.player.getDeltaMovement().add(knockback);

            VelocityUpdateEvent event = new VelocityUpdateEvent(
                    combinedVelocity.x, combinedVelocity.y, combinedVelocity.z, true
            );
            Astralis.getInstance().getEventManager().call(event);

            if (!event.isCancelled()) {
                Vec3 result = new Vec3(event.getVelocityX(), event.getVelocityY(), event.getVelocityZ());
                mc.player.push(result.x, result.y, result.z);
            }

            ci.cancel();
        });
    }
}