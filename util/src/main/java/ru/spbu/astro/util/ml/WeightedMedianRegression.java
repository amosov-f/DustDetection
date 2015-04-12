package ru.spbu.astro.util.ml;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.util.MathTools;
import ru.spbu.astro.util.Point;
import ru.spbu.astro.util.Value;

import java.util.Arrays;

/**
 * User: amosov-f
 * Date: 09.04.15
 * Time: 3:40
 */
public class WeightedMedianRegression implements SlopeLinearRegression {
    @NotNull
    private final Value slope;

    public WeightedMedianRegression(@NotNull final Point... points) {
        final double[] v = Arrays.stream(points).mapToDouble(p -> p.y().val() / p.x().val()).toArray();
        final double[] w = Arrays.stream(points).mapToDouble(p -> new Point.Weight.Quad().value(p)).toArray();
        slope = MathTools.weightedMedianValue(v, w);
    }

    @NotNull
    @Override
    public Value getSlope() {
        return slope;
    }
}
