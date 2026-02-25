package astralis.mixin.render;

import cc.astralis.Astralis;
import cc.astralis.module.impl.visual.NoRenderModule;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public class MixinInGameOverlayRenderer {

    @Inject(method = "renderFire", at = @At("HEAD"), cancellable = true)
    private static void renderNofire(PoseStack matrices, MultiBufferSource vertexConsumers, TextureAtlasSprite sprite, CallbackInfo ci) {
        NoRenderModule noRenderModule = Astralis.getInstance().getModuleManager().getModule(NoRenderModule.class);
        if (noRenderModule.isToggled() && noRenderModule.nofire.getProperty()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderWater", at = @At("HEAD"), cancellable = true)
    private static void renderNowater(Minecraft client, PoseStack matrices, MultiBufferSource vertexConsumers, CallbackInfo ci) {
        NoRenderModule noRenderModule = Astralis.getInstance().getModuleManager().getModule(NoRenderModule.class);
        if (noRenderModule.isToggled() && noRenderModule.nowater.getProperty()) {
            ci.cancel();
        }
    }
}
