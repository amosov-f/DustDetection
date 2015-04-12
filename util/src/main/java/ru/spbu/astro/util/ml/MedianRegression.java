package ru.spbu.astro.util.ml;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.util.Point;
import ru.spbu.astro.util.Value;

import java.util.Arrays;

/**
 * User: amosov-f
 * Date: 09.04.15
 * Time: 3:08
 */
public class MedianRegression implements SlopeLinearRegression {
    private final double slope;

    public MedianRegression(@NotNull final Point... points) {
        final double[] slopes = Arrays.stream(points).mapToDouble(p -> p.y().val() / p.x().val()).toArray();
        Arrays.sort(slopes);
        slope = slopes[slopes.length / 2];
    }

    @NotNull
    @Override
    public Value getSlope() {
        return Value.of(slope);
    }
}
