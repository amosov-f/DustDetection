package ru.spbu.astro.commons.hist;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.util.Split;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Function;

/**
 * User: amosov-f
 * Date: 25.10.14
 * Time: 18:49
 */
public abstract class DoubleHist<Y extends Number> extends StarHist<Double, Y> {
    @NotNull
    private final Function<Star, Double> f;
    @NotNull
    private final Split split;

    protected DoubleHist(@NotNull final String name, @NotNull final Function<Star, Double> f, @NotNull final Split split) {
        super(name);
        this.f = f;
        this.split = split;
    }

    private static double removeUnnecessaryDigits(final double x) {
        return Double.valueOf(String.format(Locale.US, "%.2f", x));
    }

    @Nullable
    @Override
    protected Double getX(@NotNull final Star star) {
        final Double x = f.apply(star);
        if (x == null) {
            return null;
        }
        if (x < split.getMin() || split.getMax() < x) {
            return null;
        }
        final double[] centers = Arrays.stream(split.getCenters()).map(DoubleHist::removeUnnecessaryDigits).toArray();
        double best = centers[0];
        for (final double center : centers) {
            if (Math.abs(x - center) < Math.abs(x - best)) {
                best = center;
            }
        }
        return best;
    }
}
