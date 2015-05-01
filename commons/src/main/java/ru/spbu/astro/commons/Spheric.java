package ru.spbu.astro.commons;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.spherical.twod.S2Point;
import org.jetbrains.annotations.NotNull;

import static java.lang.Math.*;

public final class Spheric extends S2Point {
    public static final double POLE_RA = toRadians(192.859508);
    public static final double POLE_DEC = toRadians(27.128336);
    public static final double POS_ANGLE = toRadians(122.932 - 90.0);

    public Spheric(final double l, final double b) throws OutOfRangeException {
        super(l, PI / 2 - b);
    }

    public Spheric(@NotNull final Vector3D vector) throws MathArithmeticException {
        super(vector);
    }

    @NotNull
    public static Spheric valueOf(@NotNull final double[] dir) {
        return new Spheric(dir[1], PI / 2 - dir[0]);
    }

    public double getL() {
        return getTheta();
    }

    public double getB() {
        return PI / 2 - getPhi();
    }

    @NotNull
    public EquatorialSpheric toEquatorial() {
        return new EquatorialSpheric(
                atan2(
                        cos(getB()) * cos(getL() - POS_ANGLE),
                        sin(getB()) * cos(POLE_DEC) - cos(getB()) * sin(POLE_DEC) * sin(getL() - POS_ANGLE)
                ) + POLE_RA,
                asin(cos(getB()) * cos(POLE_DEC) * sin(getL() - POS_ANGLE) + sin(getB()) * sin(POLE_DEC))
        );
    }

    @NotNull
    @Override
    public String toString() {
        return String.format("%.2f° г.ш., %.2f° г.д.", toDegrees(getB()), toDegrees(getL()));
    }
}
