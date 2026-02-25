package cc.astralis.module.impl.visual;

import astralis.mixin.accessor.render.BufferBuilderStorageAccessor;
import cc.astralis.Astralis;
import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.MotionEvent;
import cc.astralis.event.events.impl.render.Render3DEvent;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.property.properties.NumberProperty;
import cc.astralis.util.render.ColorUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;

/**
 * @author Kawase
 * @since 20.09.2025
 */
public class TrailsModule extends Module {
    private final NumberProperty minDistance = new NumberProperty("Min Distance", 0.1f, 0.1f, 2f, 0.1f),
            curveResolution = new NumberProperty("Curve Resolution", 10, 5, 50, 1);

    private final ArrayList<PositionHistoryEntry> positionHistoryEntries = new ArrayList<>();

    public TrailsModule() {
        super(Category.VISUAL);
        this.registerProperties(minDistance, curveResolution);
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        Vec3 currentPos = mc.player.position();

        if (positionHistoryEntries.isEmpty()) {
            positionHistoryEntries.add(
                    new PositionHistoryEntry(currentPos, mc.player.getPose(), System.currentTimeMillis()));
        } else {
            PositionHistoryEntry lastEntry = positionHistoryEntries.get(positionHistoryEntries.size() - 1);
            if (lastEntry.pos.distanceTo(currentPos) >= minDistance.getProperty().floatValue()) {
                positionHistoryEntries.add(
                        new PositionHistoryEntry(currentPos, mc.player.getPose(), System.currentTimeMillis()));
            }
        }
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if (positionHistoryEntries.size() < 2)
            return;

        PoseStack matrices = event.getMatricies();
        matrices.pushPose();

        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        Matrix4f matrix = matrices.last().pose();
        MultiBufferSource.BufferSource vertexConsumers =
                ((BufferBuilderStorageAccessor) mc.renderBuffers()).getEntityVertexConsumers();

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderType.debugLineStrip(2));
        positionHistoryEntries.removeIf(entry -> (System.currentTimeMillis() - entry.addedTime) > 3000);

        if (positionHistoryEntries.size() < 2)
            return;

        List<Vec3> curvePoints = generateCurvePoints();

        drawSmoothTrail(matrix, vertexConsumer, curvePoints);

        vertexConsumers.endLastBatch();
        matrices.popPose();
    }

    private List<Vec3> generateCurvePoints() {
        List<Vec3> curvePoints = new ArrayList<>();

        if (positionHistoryEntries.size() < 2) {
            return curvePoints;
        }

        curvePoints.add(positionHistoryEntries.get(0).pos.add(0, 0.02, 0));

        for (int i = 0; i < positionHistoryEntries.size() - 1; i++) {
            Vec3 current = positionHistoryEntries.get(i).pos.add(0, 0.02, 0);
            Vec3 next = positionHistoryEntries.get(i + 1).pos.add(0, 0.02, 0);

            Vec3 controlPoint1 = current;
            Vec3 controlPoint2 = next;

            if (i > 0) {
                Vec3 prev = positionHistoryEntries.get(i - 1).pos.add(0, 0.02, 0);
                controlPoint1 = current.add(next.subtract(prev).scale(0.3));
            }

            if (i < positionHistoryEntries.size() - 2) {
                Vec3 nextNext = positionHistoryEntries.get(i + 2).pos.add(0, 0.02, 0);
                controlPoint2 = next.add(current.subtract(nextNext).scale(0.3));
            }

            int resolution = curveResolution.getProperty().intValue();
            for (int j = 1; j <= resolution; j++) {
                float t = (float) j / resolution;
                Vec3 curvePoint = cubicBezier(current, controlPoint1, controlPoint2, next, t);
                curvePoints.add(curvePoint);
            }
        }

        return curvePoints;
    }

    private Vec3 cubicBezier(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, float t) {
        float u = 1 - t;
        float tt = t * t;
        float uu = u * u;
        float uuu = uu * u;
        float ttt = tt * t;

        Vec3 result = p0.scale(uuu);
        result = result.add(p1.scale(3 * uu * t));
        result = result.add(p2.scale(3 * u * tt));
        result = result.add(p3.scale(ttt));

        return result;
    }

    private void drawSmoothTrail(Matrix4f matrix, VertexConsumer vertexConsumer, List<Vec3> curvePoints) {
        if (curvePoints.size() < 2) return;

        HudModule hud = Astralis.getInstance().getModuleManager().getModule(HudModule.class);

        for (int i = 0; i < curvePoints.size() - 1; i++) {
            Vec3 point1 = curvePoints.get(i);
            Vec3 point2 = curvePoints.get(i + 1);

            float progress = (float) i / (curvePoints.size() - 1);
            float alpha = Math.max(0.1f, 1.0f - progress);

            final Color color = switch (hud.colorMode.getProperty()) {
                case "Rainbow" -> HudModule.getRainbow(3000, (int)(progress * 100), 0.7f, 1);
                default -> ColorUtil.getAccentColor(new UVPair(0, 6), hud.firstColor.getProperty(), hud.secondColor.getProperty());
            };

            Color fadedColor = ColorUtil.withAlpha(color, (int) (alpha * 255));

            drawLine(matrix, vertexConsumer, point1, point2, fadedColor);
        }
    }

    private void drawLine(Matrix4f matrix, VertexConsumer vertexConsumer, Vec3 start, Vec3 end, Color color) {
        float r = color.getRed() / 255.0f;
        float g = color.getGreen() / 255.0f;
        float b = color.getBlue() / 255.0f;
        float a = color.getAlpha() / 255.0f;

        addVertex(matrix, vertexConsumer, start, r, g, b, a);
        addVertex(matrix, vertexConsumer, end, r, g, b, a);
    }

    private void addVertex(Matrix4f matrix, VertexConsumer vertexConsumer,
                           Vec3 pos, float r, float g, float b, float a) {
        vertexConsumer.addVertex(matrix, (float)pos.x, (float)pos.y, (float)pos.z)
                .setColor(r, g, b, a)
                .setNormal(0.0f, 1.0f, 0.0f);
    }

    private record PositionHistoryEntry(Vec3 pos, Pose entityDimensions, long addedTime) {
        /* w */
    }
}