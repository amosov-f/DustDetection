package ru.spbu.astro.dust.util;

import org.jetbrains.annotations.NotNull;

import static java.lang.Math.sqrt;

public class Geom {
    public static double dotProduct(@NotNull final double[] a, @NotNull final double[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Arrays size may be equal!");
        }

        double dotProduct = 0.0;
        for (int i = 0; i < a.length; ++i) {
            dotProduct += a[i] * b[i];
        }
        return dotProduct;
    }

    public static double abs(@NotNull final double[] a) {
        double abs = 0.0;
        for (double coordinate : a) {
            abs += Math.pow(coordinate, 2);
        }
        abs = sqrt(abs);
        return abs;
    }
}
