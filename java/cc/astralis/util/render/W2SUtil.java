package cc.astralis.util.render;

import cc.astralis.interfaces.IAccess;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4d;
import org.joml.Vector4f;

public class W2SUtil implements IAccess {
    public static Matrix4f matrixWorldSpace;
    public static Matrix4f projectionMatrix;

    public static Vec3 perform(Vec3 vector) {
        Camera camera = mc.gameRenderer.getMainCamera();

        Vec3 cameraPos = camera.getPosition();
        Vec3 relativePos = vector.subtract(cameraPos);

        Vector4f pos = new Vector4f(
                (float) relativePos.x,
                (float) relativePos.y,
                (float) relativePos.z,
                1.0f
        );

        matrixWorldSpace.transform(pos);
        projectionMatrix.transform(pos);

       // ChatUtil.print(pos.toString());

        pos.div(pos.w);

        float screenX = (pos.x + 1.0f) * mc.getWindow().getScreenWidth() * 0.5f;
        float screenY = (1.0f - pos.y) * mc.getWindow().getScreenHeight() * 0.5f;
        float scaleFactor = (float) mc.getWindow().getGuiScale();

        return new Vec3(
                screenX / scaleFactor,
                screenY / scaleFactor,
                pos.z
        );
    }

    public static Vector4d calculateScreenPosition(Entity entity) {
        final AABB box = entity.getBoundingBox();

        final double x = entity.xo + (entity.getX() - entity.xo) * mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        final double y = entity.yo + (entity.getY() - entity.yo) * mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        final double z = entity.zo + (entity.getZ() - entity.zo) * mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);

        final AABB expandedBox = new AABB(
                box.minX - entity.getX() + x - 0.1,
                box.minY - entity.getY() + y,
                box.minZ - entity.getZ() + z - 0.1,
                box.maxX - entity.getX() + x + 0.1,
                box.maxY - entity.getY() + y + 0.12,
                box.maxZ - entity.getZ() + z + 0.1
        );

        final Vec3[] vectors = new Vec3[]{
                new Vec3(expandedBox.minX, expandedBox.minY, expandedBox.minZ),
                new Vec3(expandedBox.minX, expandedBox.maxY, expandedBox.minZ),
                new Vec3(expandedBox.maxX, expandedBox.minY, expandedBox.minZ),
                new Vec3(expandedBox.maxX, expandedBox.maxY, expandedBox.minZ),
                new Vec3(expandedBox.minX, expandedBox.minY, expandedBox.maxZ),
                new Vec3(expandedBox.minX, expandedBox.maxY, expandedBox.maxZ),
                new Vec3(expandedBox.maxX, expandedBox.minY, expandedBox.maxZ),
                new Vec3(expandedBox.maxX, expandedBox.maxY, expandedBox.maxZ),
        };

        Vector4d position = null;

        for (final Vec3 vector : vectors) {
            final Vec3 vectorToScreen = W2SUtil.perform(vector);

            if (vectorToScreen.z > 0 && vectorToScreen.z < 1) {
                if (position == null) {
                    position = new Vector4d(vectorToScreen.x, vectorToScreen.y, vectorToScreen.z, 0);
                }

                position.x = Math.min(vectorToScreen.x, position.x);
                position.y = Math.min(vectorToScreen.y, position.y);
                position.z = Math.max(vectorToScreen.x, position.z);
                position.w = Math.max(vectorToScreen.y, position.w);
            }
        }

        return position;
    }
}