package astralis.mixin.accessor.player;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.prediction.PredictiveAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MultiPlayerGameMode.class)
public interface ClientPlayerInteractionManagerAccessor {
    @Invoker
    void callStartPrediction(ClientLevel world, PredictiveAction packetCreator);

    @Invoker
    void callEnsureHasSentCarriedItem();

    @Invoker("ensureHasSentCarriedItem")
    void syncSlot();
}
