package ru.spbu.astro.util;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * User: amosov-f
 * Date: 15.11.14
 * Time: 18:13
 */
public final class PointsDistribution {
    private static final int DEL = 10;

    @NotNull
    private final List<Vector3D> points;

    public PointsDistribution(@NotNull List<Vector3D> points) {
        this.points = points;
    }

    @NotNull
    public Vector3D next() {
        return points.get(new Random().nextInt(points.size())).add(DEL, new Vector3D(IntStream.range(0, 3).mapToDouble(
                i -> (2 * Math.random() - 1)
        ).toArray()));
    }
}
