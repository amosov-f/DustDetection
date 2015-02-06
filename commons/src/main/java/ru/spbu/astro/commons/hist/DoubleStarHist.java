package ru.spbu.astro.commons.hist;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.commons.Star;

import java.util.*;
import java.util.function.Function;

/**
 * User: amosov-f
 * Date: 25.10.14
 * Time: 18:49
 */
public final class DoubleStarHist implements StarHist<Double> {
    @NotNull
    private final String name;
    @NotNull
    private final Function<Star, Double> f;
    private final double min;
    private final double max;
    private final double dx;

    public DoubleStarHist(@NotNull final String name, @NotNull final Function<Star, Double> f, final double dx) {
        this(name, f, 0, 1, dx);
    }

    public DoubleStarHist(@NotNull final String name, @NotNull final Function<Star, Double> f, final double min, final double max, final double dx) {
        this.name = name;
        this.f = f;
        this.min = min;
        this.max = max;
        this.dx = dx;
    }

    private static double removeUnnecessaryDigits(final double x) {
        return Double.valueOf(String.format(Locale.US, "%.2f", x));
    }

    @NotNull
    @Override
    public Map<Double, Integer> hist(@NotNull final List<Star> stars) {
        final NavigableMap<Double, Integer> counts = new TreeMap<>();
        for (int i = 0; min + dx * (i + 0.5) <= max; i++) {
            counts.put(removeUnnecessaryDigits(min + dx * (i + 0.5)), 0);
        }
        for (final Star star : stars) {
            final double value = f.apply(star);
            if (min <= value && value <= max) {
                Double x = counts.ceilingKey(value - dx / 2);
                if (x == null) {
                    x = counts.lastKey();
                }
                counts.put(x, counts.get(x) + 1);
            }

        }
        return counts;
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }
}
