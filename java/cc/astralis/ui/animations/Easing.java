package cc.astralis.ui.animations;

import java.util.function.Function;

/**
 * This code is part of Liticane's Animation Library.
 *
 * @author Liticane
 * @since 22/03/2024
 */
@SuppressWarnings("unused")
public enum Easing {
    EASE_IN_OUT_SINE(x -> -(Math.cos(Math.PI * x) - 1) / 2),
    EASE_IN_OUT_QUAD(x -> x < 0.5 ? 2 * x * x : 1 - Math.pow(-2 * x + 2, 2) / 2),
    EASE_OUT_BACK(x -> 1 + 2.70158 * Math.pow(x - 1, 3) + 1.70158 * Math.pow(x - 1, 2)),
    EASE_IN_BACK(x -> {
        double s = 1.70158;
        return x * x * ((s + 1) * x - s);
    });

    private final Function<Double, Double> function;

    Easing(Function<Double, Double> function) {
        this.function = function;
    }

    public Function<Double, Double> getFunction() {
        return function;
    }

}