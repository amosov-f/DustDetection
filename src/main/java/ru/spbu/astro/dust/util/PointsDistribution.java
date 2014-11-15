package ru.spbu.astro.dust.util;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

/**
 * User: amosov-f
 * Date: 15.11.14
 * Time: 18:13
 */
public class PointsDistribution {
    @NotNull
    private final List<double[]> points;

    public PointsDistribution(@NotNull List<double[]> points) {
        this.points = points;
    }

    @NotNull
    public double[] next() {
        return points.get(new Random().nextInt(points.size()));
    }
}
