package ru.spbu.astro.dust.util;

import org.jetbrains.annotations.NotNull;

import static java.lang.Math.sqrt;

public final class Geom {
    public static double dotProduct(@NotNull final double[] p1, @NotNull final double[] p2) {
        if (p1.length != p2.length) {
            throw new IllegalArgumentException("Arrays size may be equal!");
        }
        double dotProduct = 0.0;
        for (int i = 0; i < p1.length; ++i) {
            dotProduct += p1[i] * p2[i];
        }
        return dotProduct;
    }

    public static double abs(@NotNull final double[] p) {
        double abs = 0.0;
        for (double coordinate : p) {
            abs += Math.pow(coordinate, 2);
        }
        abs = sqrt(abs);
        return abs;
    }
}
