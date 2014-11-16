package ru.spbu.astro.dust.util;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * User: amosov-f
 * Date: 15.11.14
 * Time: 18:13
 */
public class PointsDistribution {
    private static final int DEL = 10;

    @NotNull
    private final List<double[]> points;

    public PointsDistribution(@NotNull List<double[]> points) {
        this.points = points;
    }

    @NotNull
    public double[] next() {
        double[] p = points.get(new Random().nextInt(points.size()));
        p = Arrays.copyOf(p, p.length);
        for (int i = 0; i < p.length; i++) {
            p[i] += (2 * Math.random() - 1) * DEL;
        }
        return p;
    }
}
