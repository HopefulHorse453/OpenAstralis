package astralis.mixin.render;

import cc.astralis.Astralis;
import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public class MixinWindow {
    @Inject(method = "onFramebufferResize", at = @At("RETURN"))
    private void onFramebufferSizeChanged(final long window, final int width, final int height, final CallbackInfo callback) {
        Astralis.getInstance().getSkija().init(width > 0 ? width : 1, height > 0 ? height : 1);

       /* FontManager.disposeAllFonts();
        FontManager.reinitializeAllFonts();*/
    }

    @Inject(method = "updateDisplay", at = @At("HEAD"))
    private void onSwapBuffers(CallbackInfo ci) {

    }
}
