package ru.spbu.astro.dust.model;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.geometry.Point;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.spherical.twod.S2Point;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.dust.util.Geom;

import static java.lang.Math.*;

public final class Spheric extends S2Point implements Comparable<Spheric> {

    public Spheric(final double l, final double b) throws OutOfRangeException {
        super(l, Math.PI / 2 - b);
    }

    public Spheric(@NotNull final Vector3D vector) throws MathArithmeticException {
        super(vector);
    }

    public double getL() {
        return getTheta();
    }

    public double getB() {
        return Math.PI / 2 - getPhi();
    }

    @Override
    public int compareTo(@NotNull Spheric dir) {
        if (getL() < dir.getL()) {
            return -1;
        }
        if (getL() > dir.getL()) {
            return 1;
        }
        return 0;
    }

    @NotNull
    @Override
    public String toString() {
        return String.format("%.2f° г.ш., %.2f° г.д.", Math.toDegrees(getB()), Math.toDegrees(getL()));
    }

    @NotNull
    public static Spheric valueOf(@NotNull final double[] dir) {
        return new Spheric(dir[1], Math.PI / 2 - dir[0]);
    }
}
