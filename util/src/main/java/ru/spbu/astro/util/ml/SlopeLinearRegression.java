package ru.spbu.astro.util.ml;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.util.Value;

/**
 * User: amosov-f
 * Date: 12.04.15
 * Time: 19:24
 */
public interface SlopeLinearRegression extends Regression {
    @NotNull
    Value getSlope();

    @Override
    default double predict(final double x) {
        return getSlope().val() * x;
    }
}
