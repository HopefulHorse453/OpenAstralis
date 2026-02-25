package cc.astralis.module.impl.client.cheaterfinder.checks;

import cc.astralis.event.events.impl.game.MotionEvent;
import cc.astralis.module.impl.client.cheaterfinder.Check;
import cc.astralis.module.impl.client.cheaterfinder.MotionPredictor;
import cc.astralis.util.render.ChatUtil;
import net.minecraft.world.phys.Vec3;

public class SimulationCheck extends Check {
    private final MotionPredictor motionPredictor;

    private static final double HORIZONTAL_THRESHOLD = 1;
    private static final double VERTICAL_THRESHOLD = 1;
    private static final double TOTAL_THRESHOLD = 1;

    private int suspiciousMovements = 0;
    private static final int MAX_SUSPICIOUS_BEFORE_FLAG = 3;

    public SimulationCheck() {
        super("Prediction");
        this.motionPredictor = new MotionPredictor();
    }

    @Override
    public void onMotion(MotionEvent event) {
     /*   PlayerEntity player = getPlayerData().player;
        if (player == null )  {
            return;
        }

        Vec3d predictedPos = motionPredictor.predictNextPosition(player, getPlayerData());
        Vec3d actualPos = new Vec3d(player.getX(), player.getY(), player.getZ());

        double horizontalDev = motionPredictor.getHorizontalDeviation(actualPos, predictedPos);
        double verticalDev = motionPredictor.getVerticalDeviation(actualPos, predictedPos);
        double totalDev = motionPredictor.calculateDeviation(actualPos, predictedPos);

        boolean suspiciousHorizontal = horizontalDev > HORIZONTAL_THRESHOLD;
        boolean suspiciousVertical = verticalDev > VERTICAL_THRESHOLD;
        boolean suspiciousTotal = totalDev > TOTAL_THRESHOLD;

        if ((suspiciousHorizontal || suspiciousVertical || suspiciousTotal)) {
            suspiciousMovements++;

            if (suspiciousMovements >= MAX_SUSPICIOUS_BEFORE_FLAG) {
                ChatUtil.print("§cMotion Prediction Debug:");
                ChatUtil.print("§7Horizontal Dev: §f" + String.format("%.3f", horizontalDev));
                ChatUtil.print("§7Vertical Dev: §f" + String.format("%.3f", verticalDev));
                ChatUtil.print("§7Total Dev: §f" + String.format("%.3f", totalDev));
                ChatUtil.print("§7Predicted: §f" + formatVec3d(predictedPos));
                ChatUtil.print("§7Actual: §f" + formatVec3d(actualPos));

                warn();
                suspiciousMovements = 0;
            }
        } else {
            if (suspiciousMovements > 0) {
                suspiciousMovements--;
            }
        }*/
    }

    private String formatVec3d(Vec3 vec) {
        return String.format("(%.3f, %.3f, %.3f)", vec.x, vec.y, vec.z);
    }

    public void resetPredictor() {
        motionPredictor.reset();
        suspiciousMovements = 0;
    }
}
