package cc.astralis.util.render;

import cc.astralis.interfaces.IAccess;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.util.Mth;

/**
 * @author Kawase
 * @since 28.10.2025
 */
public class BlockUtil implements IAccess {
    public static void applyBlockTransformation(final PoseStack matrices) {
        matrices.translate(-0.15F, 0.16F, 0.15F);
        matrices.mulPose(Axis.YP.rotationDegrees(-18.0F));
        matrices.mulPose(Axis.ZP.rotationDegrees(82.0F));
        matrices.mulPose(Axis.YP.rotationDegrees(112.0F));
    }

    public static void applySwingTransformation(final PoseStack matrices, final float swingProgress, final float convertedProgress) {
        final float f = Mth.sin(swingProgress * swingProgress * (float) Math.PI);
        matrices.mulPose(Axis.YP.rotationDegrees(45.0F + f * -20.0F));
        matrices.mulPose(Axis.ZP.rotationDegrees(convertedProgress * -20.0F));
        matrices.mulPose(Axis.XP.rotationDegrees(convertedProgress * -80.0F));
        matrices.mulPose(Axis.YP.rotationDegrees(-45.0F));
    }
}
