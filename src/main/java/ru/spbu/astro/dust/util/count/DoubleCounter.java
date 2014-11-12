package ru.spbu.astro.dust.util.count;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.dust.model.Star;

import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * User: amosov-f
 * Date: 25.10.14
 * Time: 18:49
 */
public class DoubleCounter extends Counter<Double> {
    @NotNull
    private final String name;
    @NotNull
    private final Function<Star, Double> f;
    private final double min;
    private final double max;
    private final double dx;

    public DoubleCounter(@NotNull final String name, @NotNull final Function<Star, Double> f, double min, double max, double dx) {
        this.name = name;
        this.f = f;
        this.min = min;
        this.max = max;
        this.dx = dx;
    }

    @NotNull
    @Override
    public Map<Double, Integer> count(@NotNull List<Star> stars) {
        final NavigableMap<Double, Integer> counts = new TreeMap<>();
        for (double x = min + dx / 2; x <= max; x += dx) {
            counts.put(x, 0);
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
