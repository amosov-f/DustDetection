package ru.spbu.astro.dust.util;

import org.jetbrains.annotations.NotNull;

import java.util.stream.IntStream;

import static java.lang.Math.sqrt;
import static java.util.Arrays.stream;

public final class Geom {
    public static double dotProduct(@NotNull final double[] p1, @NotNull final double[] p2) {
        if (p1.length != p2.length) {
            throw new IllegalArgumentException("Arrays size may be equal!");
        }
        return IntStream.range(0, p1.length).mapToDouble(i -> p1[i] * p2[i]).sum();
    }

    @NotNull
    public static double[] sum(@NotNull final double[]... points) {
        return IntStream.range(0, points[0].length).mapToDouble(i -> stream(points).mapToDouble(p -> p[i]).sum()).toArray();
    }

    @NotNull
    public static double[] minus(@NotNull final double[] p) {
        return stream(p).map(x -> -x).toArray();
    }

    @NotNull
    public static double[] subtract(@NotNull final double[] p1, @NotNull final double[] p2) {
        return sum(p1, minus(p2));
    }

    @NotNull
    public static double[] multiply(@NotNull final double[] p, final double alpha) {
        return stream(p).map(x -> x * alpha).toArray();
    }

    public static double abs(@NotNull final double[] p) {
        return sqrt(stream(p).map(x -> Math.pow(x, 2)).sum());
    }
}
