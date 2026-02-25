package cc.astralis.util;

import cc.astralis.util.math.TimeUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.culling.Frustum;

public abstract class Data {
    public static int offGroundTicks = 0, onGroundTicks = 0, offGroundMotionTicks = 0, onGroundMotionTicks = 0;
    public static float timer = 1f;
    public static final TimeUtil TIME_SINCE_GROUND = new TimeUtil();

    public static GuiGraphics drawContext;
    public static PoseStack matrices;
    public static Frustum frustum;
}
