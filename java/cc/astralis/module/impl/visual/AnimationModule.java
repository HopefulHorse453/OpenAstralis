package cc.astralis.module.impl.visual;

import cc.astralis.event.EventTarget;
import cc.astralis.event.events.impl.game.UpdateEvent;
import cc.astralis.module.Category;
import cc.astralis.module.Module;
import cc.astralis.property.properties.BooleanProperty;
import cc.astralis.property.properties.ModeProperty;
import cc.astralis.property.properties.NumberProperty;
import cc.astralis.util.render.BlockUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

public class AnimationModule extends Module {
    public static ModeProperty animationMode = new ModeProperty("Animation Mode", "1.8", "1.8", "1.7", "Swang", "Lunar", "Bouncy", "Slide", "Slide 2", "Slide 3", "Astralis", "Swank");
    public NumberProperty speed = new NumberProperty("Speed", 1, 0, 1, 0.1f);
    public static NumberProperty blockX = new NumberProperty("Block X", 0, -2, 2, 0.01f),
            blockY = new NumberProperty("Block Y", 0, -2, 2, 0.01f),
            blockZ = new NumberProperty("Block Z", 0, -2, 2, 0.01f);
    public static BooleanProperty disableItemSwingOffset = new BooleanProperty("Disable Item Swing Offset", false);

    public AnimationModule() {
        super(Category.VISUAL);
        registerProperties(animationMode, speed, blockX, blockY, blockZ, disableItemSwingOffset);
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        this.setSuffix(animationMode.getProperty());
    }

    public static void runBlockHitAnimation(final PoseStack matrices, final float swingProgress, HumanoidArm arm) {
        final float convertedProgress = Mth.sin(Mth.sqrt(swingProgress) * (float) Math.PI);
        final float f = Mth.sin(swingProgress * swingProgress * (float) Math.PI);

        float x = blockX.getProperty().floatValue();
        float y = blockY.getProperty().floatValue();
        float z = blockZ.getProperty().floatValue();

        matrices.translate(arm == HumanoidArm.RIGHT ? -x : x, y, z);

        switch (animationMode.getProperty()) {
            case "1.8" -> {
                BlockUtil.applyBlockTransformation(matrices);
            }

            case "1.7" -> {
                BlockUtil.applySwingTransformation(matrices, swingProgress, convertedProgress);
                BlockUtil.applyBlockTransformation(matrices);
            }

            case "Swang" -> {
                BlockUtil.applyBlockTransformation(matrices);
                matrices.mulPose(Axis.YP.rotationDegrees(f * -30.0F));
                matrices.mulPose(Axis.ZP.rotationDegrees(convertedProgress * -30.0F));
            }

            case "Lunar" -> {
                BlockUtil.applySwingTransformation(matrices, swingProgress, convertedProgress);
                matrices.translate(-0.15F, 0.16F, 0.15F);
                matrices.mulPose(Axis.YP.rotationDegrees(-24.0F));
                matrices.mulPose(Axis.ZP.rotationDegrees(75.0F));
                matrices.mulPose(Axis.YP.rotationDegrees(90.0F));
            }

            case "Bouncy" -> {
                BlockUtil.applyBlockTransformation(matrices);
                matrices.mulPose(Axis.XP.rotationDegrees(0.0F));
                matrices.mulPose(Axis.YP.rotationDegrees(convertedProgress * 42.0F));
                matrices.mulPose(Axis.ZP.rotationDegrees(-convertedProgress * 22.0F));
            }

            case "Slide" -> {
                BlockUtil.applyBlockTransformation(matrices);
                matrices.mulPose(Axis.XP.rotationDegrees(5.0F - (convertedProgress * 32.0F)));
                matrices.mulPose(Axis.YP.rotationDegrees(0.0F));
                matrices.mulPose(Axis.ZP.rotationDegrees(0.0F));
            }

            case "Swank" -> {
                matrices.mulPose(Axis.YP.rotationDegrees(45.0F + f * -5.0F));
                matrices.mulPose(Axis.ZP.rotationDegrees(convertedProgress * -20.0F));
                matrices.mulPose(Axis.XP.rotationDegrees(convertedProgress * -40.0F));
                matrices.mulPose(Axis.YP.rotationDegrees(-45.0F));
                BlockUtil.applyBlockTransformation(matrices);
            }

            case "Astralis" -> {
                matrices.mulPose(Axis.XP.rotationDegrees(-275.5f));

                matrices.mulPose((arm == HumanoidArm.RIGHT ? Axis.YP : Axis.YN)
                        .rotationDegrees(-40.0f));

                matrices.mulPose((arm == HumanoidArm.RIGHT ? Axis.ZP : Axis.ZN)
                        .rotationDegrees(140));

                matrices.mulPose(Axis.XP.rotationDegrees(convertedProgress * 30.0f));
                matrices.mulPose(Axis.ZP.rotationDegrees(convertedProgress * 45.0f));
            }

            case "Slide 3" -> {
                matrices.mulPose(Axis.XP.rotationDegrees(-275.5f));
                matrices.mulPose((arm == HumanoidArm.RIGHT ? Axis.YP : Axis.YN)
                        .rotationDegrees(-51.635f));
                matrices.mulPose((arm == HumanoidArm.RIGHT ? Axis.ZP : Axis.ZN)
                        .rotationDegrees(120));

                matrices.mulPose(Axis.XP.rotationDegrees(convertedProgress * 20 * 1.5f));
            }

            case "Slide 2" -> {
                matrices.mulPose(Axis.XP.rotationDegrees(-280.5f));
                matrices.mulPose((arm == HumanoidArm.RIGHT ? Axis.YP : Axis.YN)
                        .rotationDegrees(-60.635f));
                matrices.mulPose((arm == HumanoidArm.RIGHT ? Axis.ZP : Axis.ZN)
                        .rotationDegrees(110));

                matrices.mulPose(Axis.XP.rotationDegrees(convertedProgress * 30 * 1.5f));
            }
        }
    }
}
