package astralis.mixin.render;

import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RenderSystem.class)
public class MixinRenderSystem {
   /* @Inject(method = "flipFrame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Tessellator;clear()V", shift = At.Shift.AFTER))
    private static void hookFlipFrame(long window, TracyFrameCapturer capturer, CallbackInfo ci) {
        if (MinecraftClient.getInstance().currentScreen != null && !(MinecraftClient.getInstance().currentScreen instanceof HudEditorScreen)) {
            return;
        }

        final Window window1 = MinecraftClient.getInstance().getWindow();
        SkijaUtil.sco(window1.getScaledWidth(), window1.getScaledHeight());
    }*/
}
