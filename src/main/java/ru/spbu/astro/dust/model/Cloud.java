package ru.spbu.astro.dust.model;

import org.apache.commons.math3.geometry.enclosing.EnclosingBall;
import org.apache.commons.math3.geometry.euclidean.threed.Euclidean3D;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.IntStream;

/**
 * User: amosov-f
 * Date: 16.11.14
 * Time: 14:58
 */
public final class Cloud extends EnclosingBall<Euclidean3D, Vector3D> {
    private final int numPoints;

    public Cloud(@NotNull final List<Vector3D> points) {
        super(
                points.stream().reduce(Vector3D.ZERO, (p1, p2) -> p1.add(p2)).scalarMultiply(1.0 / points.size()),
                IntStream.range(0, 3)
                        .mapToDouble(i -> new StandardDeviation().evaluate(points.stream()
                                .mapToDouble(p -> p.toArray()[i]).toArray()))
                        .average().getAsDouble()
        );
        this.numPoints = points.size();
    }

    public int getNumPoints() {
        return numPoints;
    }

    @NotNull
    @Override
    public String toString() {
        return String.format(
                "Напарвлене %s, расстояние %d пк, радиус %d пк, %d точек",
                new Spheric(getCenter()),
                (int) getCenter().getNorm(),
                (int) getRadius(),
                numPoints
        );
    }
}
