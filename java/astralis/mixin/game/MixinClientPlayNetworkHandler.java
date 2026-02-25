package astralis.mixin.game;

import cc.astralis.Astralis;
import cc.astralis.event.events.impl.input.ChatInputEvent;
import cc.astralis.util.player.StorageUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class MixinClientPlayNetworkHandler {
    @Inject(method = "sendChat", at = @At("HEAD"), cancellable = true)
    private void sendChatMessageHook(String content, CallbackInfo ci) {
        ChatInputEvent chatInputEvent = new ChatInputEvent(content);
        Astralis.getInstance().getEventManager().call(chatInputEvent);

        if (chatInputEvent.isCancelled())
            ci.cancel();
    }

    @Shadow private ClientLevel level;

    @Inject(method = "handleLogin", at = @At("HEAD"))
    private void astr$onJoin(ClientboundLoginPacket packet, CallbackInfo ci) {
        StorageUtil.clearAll();
    }

   /* @Inject(method = "onDisconnected", at = @At("HEAD"))
    private void astr$onDisconnected(Text reason, CallbackInfo ci) {
        StorageUtil.clearAll();
    }*/

    @Inject(method = "handleLevelChunkWithLight", at = @At("TAIL"))
    private void astr$afterChunkData(ClientboundLevelChunkWithLightPacket packet, CallbackInfo ci) {
        if (!StorageUtil.isCollectData()) return;
        if (level == null) return;

        ChunkPos pos = new ChunkPos(packet.getX(), packet.getZ());
        LevelChunk chunk = level.getChunkSource().getChunkNow(pos.x, pos.z);
        if (chunk != null) StorageUtil.onChunkLoaded(chunk);
    }

    @Inject(method = "handleForgetLevelChunk", at = @At("TAIL"))
    private void astr$afterUnload(ClientboundForgetLevelChunkPacket packet, CallbackInfo ci) {
        if (!StorageUtil.isCollectData()) return;

        StorageUtil.onChunkUnloaded(new ChunkPos(packet.pos().x, packet.pos().z));
    }
}
