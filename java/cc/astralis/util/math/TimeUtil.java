package cc.astralis.util.math;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TimeUtil {
    private long lastMS;

    public TimeUtil() {
        reset();
    }

    public boolean finished(final long delay) {
        return System.currentTimeMillis() - delay >= lastMS;
    }

    public void reset() {
        this.lastMS = System.currentTimeMillis();
    }

    public long getElapsedTime() {
        return System.currentTimeMillis() - this.lastMS;
    }
}
