package astralis.mixin.render;

import cc.astralis.Astralis;
import cc.astralis.module.impl.visual.NoRenderModule;
import cc.astralis.util.Data;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import net.minecraft.client.Camera;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class MixinWorldRenderer {
    /*@Inject(at = @At("HEAD"), method = "renderMain", cancellable = false)
    private void onRenderMain(FrameGraphBuilder frameGraphBuilder, Frustum frustum, Camera camera,
                              Matrix4f positionMatrix, GpuBufferSlice fog, boolean renderBlockOutline,
                              boolean renderEntityOutline, RenderTickCounter tickCounter, Profiler profiler,
                              CallbackInfo ci) {

        if (matrices == null)
            return;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        // very smart code.
        matrices.push();
        GlStates.push();

        Vec3d camPos = MinecraftClient.getInstance().getBlockEntityRenderDispatcher().camera.getPos();
        matrices.translate(-camPos.x, -camPos.y, -camPos.z);
        Astralis.getInstance().getEventManager().call(new RenderBox3DEvent(matrices));

        GlStates.pop();
        matrices.pop();

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }*/

    @Inject(method = "addWeatherPass", at = @At("HEAD"), cancellable = true)
    public void renderoWeather(FrameGraphBuilder frameGraphBuilder, Vec3 cameraPos, GpuBufferSlice fogBuffer, CallbackInfo ci) {
        NoRenderModule noRenderModule = Astralis.getInstance().getModuleManager().getModule(NoRenderModule.class);
        if (noRenderModule.isToggled() && noRenderModule.noWeather.getProperty()) {
            ci.cancel();
        }
    }

    @Inject(
            method = "cullTerrain",
            at = @At("HEAD")
    )
    private void captureFrustum(Camera camera, Frustum frustum, boolean bl, CallbackInfo ci) {
        Data.frustum = frustum;
    }

    @Inject(method = "addCloudsPass", at = @At(value = "HEAD"), cancellable = true)
    private void noClouds(FrameGraphBuilder frameGraphBuilder, CloudStatus mode, Vec3 cameraPos, float cloudPhase, int color, float cloudHeight, CallbackInfo ci) {
        NoRenderModule noRenderModule = Astralis.getInstance().getModuleManager().getModule(NoRenderModule.class);
        if (noRenderModule.isToggled() && noRenderModule.noClouds.getProperty()) {
            ci.cancel();
        }
    }
}

