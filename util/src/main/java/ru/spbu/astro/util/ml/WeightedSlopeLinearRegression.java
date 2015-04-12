package ru.spbu.astro.util.ml;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.util.Point;
import ru.spbu.astro.util.Value;

import java.util.Arrays;

/**
 * User: amosov-f
 * Date: 11.01.15
 * Time: 23:01
 */
public final class WeightedSlopeLinearRegression implements SlopeLinearRegression {
    private final double slope;

    public WeightedSlopeLinearRegression(@NotNull final Point... points) {
        final Point.Weight w = new Point.Weight.Sqrt();
        final double numerator = Arrays.stream(points).mapToDouble(p -> w.value(p) * p.x().val() * p.y().val()).sum();
        final double denumenator = Arrays.stream(points).mapToDouble(p -> w.value(p) * Math.pow(p.x().val(), 2)).sum();
        slope = numerator / denumenator;
    }

    @NotNull
    @Override
    public Value getSlope() {
        return Value.of(slope);
    }
}
