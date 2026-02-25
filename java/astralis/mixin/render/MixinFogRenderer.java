package astralis.mixin.render;

import cc.astralis.Astralis;
import cc.astralis.module.impl.visual.AmbienceModule;
import cc.astralis.module.impl.visual.NoRenderModule;
import com.mojang.blaze3d.buffers.Std140Builder;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;
import java.nio.ByteBuffer;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;

@Mixin(FogRenderer.class)
public abstract class MixinFogRenderer {
    @ModifyVariable(
            method = "setupFog(Lnet/minecraft/client/Camera;IZLnet/minecraft/client/DeltaTracker;FLnet/minecraft/client/multiplayer/ClientLevel;)Lorg/joml/Vector4f;",
            at = @At("STORE"),
            ordinal = 0
    )
    private Vector4f modifyFogColor(Vector4f original, Camera camera, int viewDistance, boolean thick, DeltaTracker tickCounter, float skyDarkness, ClientLevel world) {
        AmbienceModule ambienceModule = Astralis.getInstance().getModuleManager().getModule(AmbienceModule.class);
        if (ambienceModule != null && ambienceModule.isToggled() && ambienceModule.fogshi.getProperty()) {
            Color c = ambienceModule.fogcolor.getProperty();
            return new Vector4f(
                    c.getRed() / 255.0f,
                    c.getGreen() / 255.0f,
                    c.getBlue() / 255.0f,
                    1.0f
            );
        }
        return original;
    }

    @Inject(
            method = "setupFog(Lnet/minecraft/client/Camera;IZLnet/minecraft/client/DeltaTracker;FLnet/minecraft/client/multiplayer/ClientLevel;)Lorg/joml/Vector4f;",
            at = @At("TAIL")
    )
    private void modifyFogDistance(Camera camera, int viewDistance, boolean thick, DeltaTracker tickCounter, float skyDarkness, ClientLevel world, CallbackInfoReturnable<Vector4f> cir) {
        AmbienceModule ambienceModule = Astralis.getInstance().getModuleManager().getModule(AmbienceModule.class);
        NoRenderModule noRenderModule = Astralis.getInstance().getModuleManager().getModule(NoRenderModule.class);

        FogData fogData = new FogData();

        if (noRenderModule != null && noRenderModule.isToggled() && noRenderModule.noFog.getProperty()) {
            fogData.renderDistanceStart = 10000.0f;
            fogData.renderDistanceEnd = 10000.0f;
            fogData.environmentalStart = 10000.0f;
            fogData.environmentalEnd = 10000.0f;
        } else if (ambienceModule != null && ambienceModule.isToggled() && ambienceModule.fogshi.getProperty()) {
            fogData.renderDistanceStart = ambienceModule.fogstart.getProperty().floatValue();
            fogData.renderDistanceEnd = ambienceModule.fogend.getProperty().floatValue();
            fogData.environmentalStart = ambienceModule.environmentalStart.getProperty().floatValue();
            fogData.environmentalEnd = ambienceModule.environmentalEnd.getProperty().floatValue();
        } else {
            fogData.renderDistanceStart = viewDistance * 0.75f;
            fogData.renderDistanceEnd = viewDistance * 1.0f;
            fogData.environmentalStart = viewDistance * 0.75f;
            fogData.environmentalEnd = viewDistance * 1.0f;
        }
    }

    @Redirect(
            method = "setupFog(Lnet/minecraft/client/Camera;IZLnet/minecraft/client/DeltaTracker;FLnet/minecraft/client/multiplayer/ClientLevel;)Lorg/joml/Vector4f;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/fog/FogRenderer;updateBuffer(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V"
            )
    )
    private void redirectApplyFog(FogRenderer instance, ByteBuffer buffer, int bufPos, Vector4f fogColor,
                                  float environmentalStart, float environmentalEnd,
                                  float renderDistanceStart, float renderDistanceEnd,
                                  float skyEnd, float cloudEnd) {
        AmbienceModule ambienceModule = Astralis.getInstance().getModuleManager().getModule(AmbienceModule.class);
        NoRenderModule noRenderModule = Astralis.getInstance().getModuleManager().getModule(NoRenderModule.class);

        float newEnvironmentalStart = environmentalStart;
        float newEnvironmentalEnd = environmentalEnd;
        float newRenderStart = renderDistanceStart;
        float newRenderEnd = renderDistanceEnd;

        if (noRenderModule != null && noRenderModule.isToggled() && noRenderModule.noFog.getProperty()) {
            newEnvironmentalStart = 10000.0f;
            newEnvironmentalEnd = 10000.0f;
            newRenderStart = 10000.0f;
            newRenderEnd = 10000.0f;
        } else if (ambienceModule != null && ambienceModule.isToggled() && ambienceModule.fogshi.getProperty()) {
            newEnvironmentalStart = ambienceModule.environmentalStart.getProperty().floatValue();
            newEnvironmentalEnd = ambienceModule.environmentalEnd.getProperty().floatValue();
            newRenderStart = ambienceModule.fogstart.getProperty().floatValue();
            newRenderEnd = ambienceModule.fogend.getProperty().floatValue();
        }

        buffer.position(bufPos);
        Std140Builder.intoBuffer(buffer)
                .putVec4(fogColor)
                .putFloat(newEnvironmentalStart)
                .putFloat(newEnvironmentalEnd)
                .putFloat(newRenderStart)
                .putFloat(newRenderEnd)
                .putFloat(skyEnd)
                .putFloat(cloudEnd);
    }
}