package ru.spbu.astro.commons.hist;

import com.google.common.primitives.Ints;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.util.Split;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
    public Integer getY(@NotNull final Stream<Star> stars) {
        return (int) stars.count();
    }

    @NotNull
    public Map<Double, Double> histShares(@NotNull final Star... stars) {
        final Map<Double, Integer> counts = hist(stars);
        final int count = IntStream.of(Ints.toArray(counts.values())).sum();
        final Map<Double, Double> fracts = new TreeMap<>();
        counts.forEach((x, y) -> fracts.put(x, (double) y / count));
        return fracts;
    }
}
