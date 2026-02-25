package astralis.mixin.accessor.player;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.prediction.PredictiveAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MultiPlayerGameMode.class)
public interface AccessorClientPlayerInteractionManager {
    @Invoker("startPrediction")
    void invokeSendSequencedPacket(final ClientLevel world, final PredictiveAction packetCreator);

    @Accessor("destroyProgress")
    float getCurrentBreakingProgress();

    @Accessor("destroyProgress")
    void setCurrentBreakingProgress(float value);

    @Accessor("destroyDelay")
    void setBreakingCooldown(int value);
}

