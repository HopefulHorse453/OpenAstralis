package astralis.mixin.render;

import cc.astralis.Astralis;
import cc.astralis.event.events.impl.render.GammaEvent;
import cc.astralis.module.impl.visual.AmbienceModule;
import cc.astralis.module.impl.visual.FullbrightModule;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(LightTexture.class)
public class MixinLightmapTextureManager {

    /*@ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/NativeImage;setColor(III)V"))
    public void hookGammaEvent(Args args) {
        GammaEvent gammaEvent = new GammaEvent(args.get(0), args.get(1), args.get(2));
        Astralis.getInstance().getEventManager().call(gammaEvent);
        args.set(2, gammaEvent.color);
    }*/

    @Redirect(method = "updateLightTexture", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;", ordinal = 1))
    private Object update(OptionInstance option) {
        FullbrightModule fullbright = Astralis.getInstance().getModuleManager().getModule(FullbrightModule.class);
        if (fullbright.isToggled())
            return 8.0;
        return option.get();
    }
}
