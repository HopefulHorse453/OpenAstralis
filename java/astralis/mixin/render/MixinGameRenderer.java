package astralis.mixin.render;

import cc.astralis.module.impl.visual.CameraModule;
import cc.astralis.module.impl.visual.ZoomModule;
import cc.astralis.skija.SkijaManager;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.util.Mth;
import cc.astralis.Astralis;
import cc.astralis.event.events.impl.game.HurtCamEvent;
import cc.astralis.event.events.impl.render.Render3DEvent;
import cc.astralis.interfaces.IAccess;
import cc.astralis.util.Data;
import cc.astralis.util.render.W2SUtil;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class MixinGameRenderer implements IAccess {

    @Shadow
    private float renderDistance;

    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    void onBobViewWhenHurt(PoseStack matrices, float tickDelta, CallbackInfo ci) {
        HurtCamEvent hurtCamEvent = new HurtCamEvent();
        Astralis.getInstance().getEventManager().call(hurtCamEvent);

        if (hurtCamEvent.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;setProjectionMatrix(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lcom/mojang/blaze3d/ProjectionType;)V",
                    ordinal = 1,
                    shift = At.Shift.BEFORE
            )
    )
    private void hookLevelRenderEndEvent(DeltaTracker tickCounter, CallbackInfo ci,
                                         @Local(ordinal = 0) Matrix4f matrix4f,
                                         @Local(ordinal = 1) Matrix4f matrix4f2) {
        PoseStack poseStack = new PoseStack();
        Data.matrices = poseStack;
        poseStack.mulPose(matrix4f2);
        Astralis.getInstance().getEventManager().call(new Render3DEvent(poseStack));

        W2SUtil.matrixWorldSpace = new Matrix4f(poseStack.last().pose());
        W2SUtil.projectionMatrix = matrix4f;
    }

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void modifyFov(Camera camera, float tickProgress, boolean changingFov, CallbackInfoReturnable<Float> cir) {
        double baseFov = cir.getReturnValue();

        ZoomModule zoom = Astralis.getInstance().getModuleManager().getModule(ZoomModule.class);
        float modifiedFOV = zoom.getModifiedFov((float) baseFov);
        if (!zoom.isToggled() || modifiedFOV == baseFov) return;

        cir.setReturnValue(modifiedFOV);
    }

    @Shadow
    @Final
    Minecraft minecraft;

    @Shadow
    @Final
    private RenderBuffers renderBuffers;

   /* @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;draw()V", shift = At.Shift.AFTER), method = "render")
    private void renderHud(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        final DrawContext drawContext = new DrawContext(this.client, this.buffers.getEntityVertexConsumers());

        Data.matrices = drawContext.getMatrices();
        Data.drawContext = drawContext;

         final Render2DMCEvent eventRender2D = new Render2DMCEvent(drawContext);
        if (mc.currentScreen == null || mc.currentScreen instanceof HudEditorScreen)
            Astralis.getInstance().getEventManager().call(eventRender2D);
    }*/

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/render/GuiRenderer;incrementFrameNumber()V",
                    shift = At.Shift.AFTER
            )
    )
    private void onRender(DeltaTracker tickCounter, boolean tick, CallbackInfo ci) {
        SkijaManager.runCallbacks();
    }

    @Inject(method = "getProjectionMatrix", at = @At("TAIL"), cancellable = true)
    public void getBasicProjectionMatrixHook(float fovDegrees, CallbackInfoReturnable<Matrix4f> cir) {
        CameraModule cameraModule = Astralis.getInstance().getModuleManager().getModule(CameraModule.class);
        if (cameraModule.isToggled() && cameraModule.changeAspectRatio.getProperty()) {
            float zoom = cameraModule.aspectRatio.getProperty().floatValue();
            if (zoom != 1.0f) {
                float modifiedFov = fovDegrees / zoom;

                float defaultAspectRatio = (float) minecraft.getWindow().getWidth() /
                        (float) minecraft.getWindow().getHeight();
                float aspectRatio = defaultAspectRatio * (zoom <= 1.0f ? zoom : 1.0f / zoom);
                aspectRatio = Mth.clamp(aspectRatio, 0.5f, 2.0f);

                Matrix4f projectionMatrix = new Matrix4f().perspective(
                        modifiedFov * ((float) Math.PI / 180F),
                        aspectRatio,
                        0.05f,
                        renderDistance * 4.0f
                );

                cir.setReturnValue(projectionMatrix);
            }
        }
    }
}
