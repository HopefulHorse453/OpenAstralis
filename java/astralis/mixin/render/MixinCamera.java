package astralis.mixin.render;

import cc.astralis.Astralis;
import cc.astralis.module.impl.visual.CameraModule;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public class MixinCamera {

    @Inject(method = "getMaxZoom", at = @At("HEAD"), cancellable = true)
    public void hookCameraNoClip(float f, CallbackInfoReturnable<Float> cir) {
        CameraModule cameraModule = Astralis.getInstance().getModuleManager().getModule(CameraModule.class);
        if (cameraModule.isToggled() && cameraModule.cameraNoClip.getProperty() && cameraModule.modifierCamera.getProperty()) {
            cir.setReturnValue(f);
        }
    }
}