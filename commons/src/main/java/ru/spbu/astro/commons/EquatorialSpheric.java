package ru.spbu.astro.commons;

import org.apache.commons.math3.geometry.spherical.twod.S2Point;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.util.MathTools;

import java.util.Locale;

import static java.lang.Math.toDegrees;

/**
 * User: amosov-f
 * Date: 29.04.15
 * Time: 0:42
 */
public final class EquatorialSpheric extends S2Point {
    public EquatorialSpheric(final double ra, final double dec) {
        super(ra, Math.PI / 2 - dec);
    }

    public double getRa() {
        return getTheta();
    }

    public double getDec() {
        return Math.PI / 2 - getPhi();
    }

    @NotNull
    @Override
    public String toString() {
        return String.format(Locale.US, "(%.2f, %.2f)", MathTools.toHours(getRa()), toDegrees(getDec()));
    }
}
