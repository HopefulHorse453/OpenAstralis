package cc.astralis.ui.animations;

import cc.astralis.util.math.TimeUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * This code is part of Liticane's Animation Library.
 *
 * @author Liticane
 * @since 22/03/2024
 */
@Getter
@Setter
public class Animation {

    private final TimeUtil timeUtil = new TimeUtil();
    private Easing easing;

    private long duration;
    private double startPoint, endPoint, value;

    private boolean finished = true;

    public Animation(final Easing easing, final long duration) {
        this.easing = easing;
        this.duration = duration;
    }

    public void run(final double endPoint) {
        if (this.endPoint != endPoint) {
            this.endPoint = endPoint;
            this.reset();
        } else {
            this.finished = timeUtil.finished(duration);

            if (this.finished) {
                this.value = endPoint;
                return;
            }
        }

        final double newValue = this.easing.getFunction().apply(this.getProgress());

        if (this.value > endPoint) {
            this.value = this.startPoint - (this.startPoint - endPoint) * newValue;
        } else {
            this.value = this.startPoint + (endPoint - this.startPoint) * newValue;
        }
    }

    public double getProgress() {
        return (double) (System.currentTimeMillis() - this.timeUtil.getLastMS()) / (double) this.duration;
    }

    public void reset() {
        this.timeUtil.reset();
        this.startPoint = value;
        this.finished = false;
    }
}
