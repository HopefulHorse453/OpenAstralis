package astralis.mixin.player;

import cc.astralis.Astralis;
import cc.astralis.event.events.impl.game.PlayerJoinEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayer player;

    @Inject(method = "handleAcceptPlayerLoad", at = @At("TAIL"))
    private void onPlayerJoin(net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket packet, CallbackInfo ci) {
        Astralis.getInstance().getEventManager().call(new PlayerJoinEvent(System.currentTimeMillis()));
    }
}