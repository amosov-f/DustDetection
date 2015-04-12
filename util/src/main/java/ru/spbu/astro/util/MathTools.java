package ru.spbu.astro.util;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.IntStream;

import static java.lang.Math.abs;
import static java.lang.Math.max;

/**
 * User: amosov-f
 * Date: 07.02.15
 * Time: 3:12
 */
public final class MathTools {
    private MathTools() {
    }

    public static Value average(@NotNull final double... x) {
        return Value.of(new Mean().evaluate(x),  new StandardDeviation().evaluate(x));
    }

    public static double interpolate(final double x1, final double y1, final double x2, final double y2, final double x) {
        final double dx = x2 - x1;
        final double dy = y2 - y1;
        return dy / dx * (x - x1) + y1;
    }

    public static double shrink(final double x, final double min, final double max) {
        double normalized = x;
        if (normalized > max) {
            normalized = max;
        }
        if (normalized < min) {
            normalized = min;
        }

        final double d = max(abs(min), abs(max));
        return d == 0 ? 0 : normalized / d;
    }

    public static double percentile(@NotNull final double[] v, final double[] w, final int percent) {
        final List<Pair<Double, Double>> vals = sort(v, w);
        final int n = vals.size();
        final double[] s = new double[n];
        double sum = 0;
        for (int i = 0; i < s.length; i++) {
            sum += vals.get(i).getRight();
            s[i] = sum;
        }
        final double[] p = IntStream.range(0, n).mapToDouble(i -> 100 * (s[i] - vals.get(i).getRight() / 2) / s[n - 1]).toArray();
        if (percent < p[0]) {
            return vals.get(0).getLeft();
        }
        if (percent >= p[n - 1]) {
            return vals.get(n - 1).getLeft();
        }
        for (int i = 0; i < n - 1; i++) {
            if (p[i] <= percent && percent < p[i + 1]) {
                return interpolate(p[i], vals.get(i).getLeft(), p[i + 1], vals.get(i + 1).getLeft(), percent);
            }
        }
        throw new RuntimeException();
    }

    public static double median(@NotNull final double[] v) {
        return weightedMedian(v, IntStream.range(0, v.length).mapToDouble(i -> 1.0).toArray());
    }

    public static double weightedMedian(@NotNull final double[] v, @NotNull final double[] w) {
        return percentile(v, w, 50);
    }

    @NotNull
    public static Value weightedMedianValue(@NotNull final double[] v, @NotNull final double[] w) {
        final double median = weightedMedian(v, w);
        return Value.of(median, weightedMedian(Arrays.stream(v).map(x -> abs(x - median)).toArray(), w));
    }

    @NotNull
    private static List<Pair<Double, Double>> sort(@NotNull final double[] v, @NotNull final double[] w) {
        final List<Pair<Double, Double>> sortedValues = new ArrayList<>();
        IntStream.range(0, v.length).forEach(i -> sortedValues.add(Pair.of(v[i], w[i])));
        Collections.sort(sortedValues, Comparator.comparingDouble(Pair::getLeft));
        return sortedValues;
    }
}
