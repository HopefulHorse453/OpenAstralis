package cc.astralis.util.render;

import astralis.mixin.accessor.render.BufferBuilderStorageAccessor;
import cc.astralis.Astralis;
import cc.astralis.interfaces.IAccess;
import cc.astralis.module.impl.visual.HudModule;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class Render3DUtil implements IAccess {
    public static final RenderPipeline RENDER_PIPELINE = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation("pipeline/debug_filled_box")
            .withCull(false)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_STRIP)
            .build()
    );

    public static final RenderPipeline QUAD_RENDER_PIPELINE = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation("pipeline/debug_filled_box")
            .withCull(false)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
            .build()
    );

    public static final RenderType RENDER_TYPE = RenderType.create("test", 1536, false, true, RENDER_PIPELINE, RenderType.CompositeState.builder().setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING).createCompositeState(false));
    public static final RenderType QUAD_RENDER_TYPE = RenderType.create("test2", 1536, false, true, QUAD_RENDER_PIPELINE, RenderType.CompositeState.builder().setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING).createCompositeState(false));

    public static void drawBoxESP(PoseStack stack, AABB box, Color color,int alpha) {
        stack.pushPose();

        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        stack.translate(box.minX - cam.x, box.minY - cam.y, box.minZ - cam.z);

        Matrix4f matrix = stack.last().pose();
        MultiBufferSource.BufferSource vcp = ((BufferBuilderStorageAccessor) mc.renderBuffers()).getEntityVertexConsumers();
        VertexConsumer vc = vcp.getBuffer(QUAD_RENDER_TYPE);

        int rgb = ColorUtil.withAlpha(color, alpha).getRGB();

        float dx = (float) (box.maxX - box.minX);
        float dy = (float) (box.maxY - box.minY);
        float dz = (float) (box.maxZ - box.minZ);

        // Bottom
        drawQuad(matrix, vc, rgb, 0, 0, dz, dx, 0, dz, dx, 0, 0, 0, 0, 0);

        // Top
        drawQuad(matrix, vc, rgb, 0, dy, 0, dx, dy, 0, dx, dy, dz, 0, dy, dz);

        // West
        drawQuad(matrix, vc, rgb, 0, 0, 0, 0, 0, dz, 0, dy, dz, 0, dy, 0);

        // East
        drawQuad(matrix, vc, rgb, dx, 0, dz, dx, 0, 0, dx, dy, 0, dx, dy, dz);

        // North
        drawQuad(matrix, vc, rgb, dx, 0, 0, 0, 0, 0, 0, dy, 0, dx, dy, 0);

        // South
        drawQuad(matrix, vc, rgb, 0, 0, dz, dx, 0, dz, dx, dy, dz, 0, dy, dz);

        vcp.endLastBatch();
        stack.popPose();
    }

    private static void drawQuad(Matrix4f matrix, VertexConsumer consumer, int color,
                                 float x1, float y1, float z1,
                                 float x2, float y2, float z2,
                                 float x3, float y3, float z3,
                                 float x4, float y4, float z4) {
        consumer.addVertex(matrix, x1, y1, z1).setColor(color);
        consumer.addVertex(matrix, x2, y2, z2).setColor(color);
        consumer.addVertex(matrix, x3, y3, z3).setColor(color);
        consumer.addVertex(matrix, x4, y4, z4).setColor(color);
    }

    private static void drawLine(PoseStack matrixStack, VertexConsumer vertexConsumer, float x1, float y1, float z1, float x2, float y2, float z2, Color color) {
        PoseStack.Pose entry = matrixStack.last();
        Matrix4f matrix4f = entry.pose();

        Vec3 normal = getNormal(x1, y1, z1, x2, y2, z1);

        float r = (float) color.getRed() / 255;
        float g = (float) color.getGreen() / 255;
        float b = (float) color.getBlue() / 255;

        vertexConsumer.addVertex(matrix4f, x1, y1, z1).setColor(r, g, b, 1.0f).setNormal(entry, (float) normal.x, (float) normal.y, (float) normal.z);
        vertexConsumer.addVertex(matrix4f, x2, y2, z2).setColor(r, g, b, 1.0f).setNormal(entry, (float) normal.x, (float) normal.y, (float) normal.z);
    }

    public static Vec3 getNormal(float x1, float y1, float z1, float x2, float y2, float z2) {
        float deltaX = x2 - x1;
        float deltaY = y2 - y1;
        float deltaZ = z2 - z1;
        float normalSqrt = Mth.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

        return new Vec3(deltaX / normalSqrt, deltaY / normalSqrt, deltaZ / normalSqrt).normalize();
    }

    public static void drawCircleESP(PoseStack stack, Entity target) {
        stack.pushPose();

        final HudModule hud = Astralis.getInstance().getModuleManager().getModule(HudModule.class);

        final Color baseFirst = switch (hud.colorMode.getProperty()) {
            case "Rainbow" -> HudModule.getRainbow(3000, 0, 0.7f, 1);
            default -> ColorUtil.getAccentColor(new UVPair(0, 6),
                    hud.firstColor.getProperty(), hud.secondColor.getProperty());
        };
        final Color baseSecond = switch (hud.colorMode.getProperty()) {
            case "Rainbow" -> HudModule.getRainbow(4000, 0, 1f, 1);
            default -> ColorUtil.getAccentColor(new UVPair(0, 6),
                    hud.firstColor.getProperty(), hud.secondColor.getProperty());
        };

        Color firstColor = ColorUtil.withAlpha(baseFirst, 200);
        Color secondColor = ColorUtil.withAlpha(baseSecond, 200);

        stack.pushPose();

        float f = System.currentTimeMillis() % 1800 / 900F;
        boolean b = f >= 1;
        if (b) f = 2 - f;
        float f1 = (float) Math.sin(f * Math.PI);
        stack.translate(target.getPosition(mc.getDeltaTracker().getGameTimeDeltaPartialTick(true)).add(0, (target.getBbHeight() + 0.125F) * f, 0).subtract(mc.gameRenderer.getMainCamera().getPosition()));
        Matrix4f matrix4f = stack.last().pose();
        float size = target.getBbWidth();

        MultiBufferSource.BufferSource vertexConsumerProvider = ((BufferBuilderStorageAccessor) mc.renderBuffers()).getEntityVertexConsumers();

        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RENDER_TYPE);

        final float y = 0.25F * f1;
        for (int i = 0; i < 361; i++) {
            double x = Math.sin(Math.toRadians(i)) * size, z = Math.cos(Math.toRadians(i)) * size;
            vertexConsumer.addVertex(matrix4f, (float) x, 0, (float) z).setColor((b ? secondColor : firstColor).getRGB());
            vertexConsumer.addVertex(matrix4f, (float) x, y, (float) z).setColor((b ? secondColor : firstColor).getRGB());
        }

        vertexConsumerProvider.endLastBatch();

        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        vertexConsumer = vertexConsumerProvider.getBuffer(RenderType.debugLineStrip(1));
        for (int i = 0; i < 361; i++) {
            vertexConsumer.addVertex(matrix4f, (float) Math.sin(Math.toRadians(i)) * size, b ? 0 : y, (float) Math.cos(Math.toRadians(i)) * size).setColor((b ? secondColor : firstColor).getRGB());
        }
        vertexConsumerProvider.endLastBatch();

        GL11.glDisable(GL11.GL_LINE_SMOOTH);

        stack.popPose();
    }

    public static void drawCircleAt(PoseStack stack, Vec3 pos, float size, int alpha) {
        stack.pushPose();

        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();

        stack.translate(pos.x - cameraPos.x, pos.y - cameraPos.y, pos.z - cameraPos.z);

        Matrix4f matrix4f = stack.last().pose();

        MultiBufferSource.BufferSource vertexConsumers = ((BufferBuilderStorageAccessor) mc.renderBuffers())
                .getEntityVertexConsumers();
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderType.debugLineStrip(1));

        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        for (int i = 0; i <= 360; i++) {
            double angleRad = Math.toRadians(i);
            float x = (float) Math.sin(angleRad) * size;
            float z = (float) Math.cos(angleRad) * size;

            HudModule hud = Astralis.getInstance().getModuleManager().getModule(HudModule.class);
            final Color color = switch (hud.colorMode.getProperty()) {
                case "Rainbow" -> HudModule.getRainbow(3000, 0, 0.7f, 1);
                default -> ColorUtil.getAccentColor(new UVPair(0, 6), hud.firstColor.getProperty(), hud.secondColor.getProperty());
            };
            Color c = ColorUtil.withAlpha(color, alpha);
            vertexConsumer.addVertex(matrix4f, x, 0.01F, z)
                    .setColor(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
        }

        vertexConsumers.endLastBatch();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);

        stack.popPose();
    }

    public static void drawCircle(PoseStack stack, Entity target, float size) {
        stack.pushPose();

        Vec3 interpolatedPos = target.getPosition(mc.getDeltaTracker().getGameTimeDeltaPartialTick(true));
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();

        stack.translate(interpolatedPos.x - cameraPos.x, interpolatedPos.y - cameraPos.y, interpolatedPos.z - cameraPos.z);

        Matrix4f matrix4f = stack.last().pose();

        MultiBufferSource.BufferSource vertexConsumers = ((BufferBuilderStorageAccessor) mc.renderBuffers()).getEntityVertexConsumers();
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderType.debugLineStrip(1));

        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        for (int i = 0; i <= 360; i++) {
            double angleRad = Math.toRadians(i);
            float x = (float) Math.sin(angleRad) * size;
            float z = (float) Math.cos(angleRad) * size;
            vertexConsumer.addVertex(matrix4f, x, 0.01F, z)
                    .setColor(Astralis.getInstance().getFirstColor().getRGB());
        }

        vertexConsumers.endLastBatch();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);

        stack.popPose();
    }

    public static float getTickDelta() {
        return mc.getDeltaTracker().getRealtimeDeltaTicks();
    }

    public static double animate(double input) {
        return Math.abs(1 + Math.sin(input)) / 2;
    }

}
