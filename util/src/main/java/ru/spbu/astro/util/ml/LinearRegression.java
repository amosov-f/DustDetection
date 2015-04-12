package ru.spbu.astro.util.ml;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.util.Value;

/**
 * User: amosov-f
 * Date: 11.01.15
 * Time: 23:31
 */
public interface LinearRegression extends SlopeLinearRegression {
    @NotNull
    Value getIntercept();

    @Override
    default double predict(final double x) {
        return SlopeLinearRegression.super.predict(x) + getIntercept().val();
    }
}
