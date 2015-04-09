package ru.spbu.astro.util;

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

    public static double percentile(final double[] v, final double[] w, final int percent) {
        final int n = v.length;
        final double[] s = new double[n];
        double sum = 0;
        for (int i = 0; i < s.length; i++) {
            sum += w[i];
            s[i] = sum;
        }
        final double[] p = IntStream.range(0, n).mapToDouble(i -> 100 * (s[i] - w[i] / 2) / s[n - 1]).toArray();
        if (percent < p[0]) {
            return v[0];
        }
        if (percent >= p[n - 1]) {
            return v[n - 1];
        }
        for (int i = 0; i < n - 1; i++) {
            if (p[i] <= percent && percent < p[i + 1]) {
                return interpolate(p[i], v[i], p[i + 1], v[i + 1], percent);
            }
        }
        throw new RuntimeException();
    }

    public static double weightedMedian(final double[] v, final double[] w) {
        return percentile(v, w, 50);
    }
}
