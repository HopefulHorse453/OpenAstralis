package astralis.mixin.game;

import cc.astralis.util.Data;
import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DeltaTracker.Timer.class)
public class MixinRenderTickCounter {
    @Shadow private float deltaTicks;
    @Shadow private float deltaTickResidual;
    @Shadow private long lastMs;
    @Shadow @Final private float msPerTick;
    @Shadow @Final private FloatUnaryOperator targetMsptProvider;

    @Inject(method = "advanceGameTime(J)I", at = @At("HEAD"), cancellable = true)
    private void overrideBeginRenderTick(long timeMillis, CallbackInfoReturnable<Integer> cir) {
        this.deltaTicks = (float)(timeMillis - this.lastMs) / this.targetMsptProvider.apply(this.msPerTick);
        this.deltaTicks *= Data.timer;

        this.lastMs = timeMillis;
        this.deltaTickResidual += this.deltaTicks;
        int i = (int)this.deltaTickResidual;
        this.deltaTickResidual -= (float)i;

        cir.setReturnValue(i);
    }
}