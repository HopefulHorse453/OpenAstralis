package cc.astralis.util.math;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MathUtil {
    public static double interpolate(double oldValue, double newValue, double interpolationValue){
        return (oldValue + (newValue - oldValue) * interpolationValue);
    }

    public static double roundToDecimalPlaces(double value, int places) {
        if (places < 0) throw new IllegalArgumentException("Decimal places must be non-negative");

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static boolean isSimilar(float num1, float num2, int maxDifference) {
        return Math.abs(num1 - num2) <= maxDifference;
    }

    public static double wrapAngleTo180_double(double value) {
        value = value % 360.0D;

        if (value >= 180.0D) {
            value -= 360.0D;
        }

        if (value < -180.0D) {
            value += 360.0D;
        }

        return value;
    }
}
