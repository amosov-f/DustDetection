package ru.spbu.astro.commons.hist;

import com.google.common.primitives.Ints;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.util.Split;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * User: amosov-f
 * Date: 08.03.15
 * Time: 20:51
 */
public class CountHist extends DoubleHist<Integer> {
    public CountHist(@NotNull final String name, @NotNull final Function<Star, Double> f, @NotNull final Split split) {
        super(name, f, split);
    }

    @Nullable
    @Override
    public Integer getY(@NotNull final List<Star> stars) {
        return stars.size();
    }

    @NotNull
    public Map<Double, Double> histShares(@NotNull final List<Star> stars) {
        final Map<Double, Integer> counts = hist(stars);
        final Map<Double, Double> fracts = new TreeMap<>();
        final int count = IntStream.of(Ints.toArray(counts.values())).sum();
        for (final Map.Entry<Double, Integer> entry : counts.entrySet()) {
            fracts.put(entry.getKey(), (double) entry.getValue() / count);
        }
        return fracts;
    }
}
