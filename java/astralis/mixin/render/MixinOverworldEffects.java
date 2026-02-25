package astralis.mixin.render;

import cc.astralis.Astralis;
import cc.astralis.module.impl.visual.AmbienceModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.Color;
import net.minecraft.client.renderer.DimensionSpecialEffects.OverworldEffects;
import net.minecraft.world.phys.Vec3;

@Mixin(OverworldEffects.class)
public class MixinOverworldEffects {

    @Inject(
            method = "getBrightnessDependentFogColor",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onAdjustFogColor(Vec3 color, float sunHeight, CallbackInfoReturnable<Vec3> cir) {
        AmbienceModule ambience = Astralis.getInstance().getModuleManager().getModule(AmbienceModule.class);
        if (ambience != null && ambience.isToggled() && ambience.sky.getProperty()) {
            Color skyColor = ambience.skycolor.getProperty();
            Vec3 customColor = new Vec3(
                    skyColor.getRed() / 255.0,
                    skyColor.getGreen() / 255.0,
                    skyColor.getBlue() / 255.0
            );
            cir.setReturnValue(customColor);
        }
    }
}
