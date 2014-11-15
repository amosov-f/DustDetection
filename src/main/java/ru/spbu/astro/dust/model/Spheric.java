package ru.spbu.astro.dust.model;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.dust.util.Geom;

import static java.lang.Math.*;

public final class Spheric implements Comparable<Spheric> {
    private final double l;
    private final double b;

    public Spheric(final double l, final double b) {
        this.l = l;
        this.b = b;
    }

    public Spheric(@NotNull final double[] p) {
        l = Math.atan2(p[1], p[0]);
        b = Math.asin(p[2] / Geom.abs(p));
    }

    public double getL() {
        return l;
    }

    public double getB() {
        return b;
    }

    public double getTheta() {
        return Math.PI / 2 - b;
    }

    public double getPhi() {
        return l;
    }

    public double distanceTo(@NotNull final Spheric dir) {
        final double theta1 = getTheta();
        final double theta2 = dir.getTheta();
        final double phi1 = getPhi();
        final double phi2 = dir.getPhi();

        return acos(cos(theta1) * cos(theta2) + sin(theta1) * sin(theta2) * cos(phi2 - phi1));
    }

    @Override
    public int compareTo(@NotNull Spheric dir) {
        if (l < dir.l) {
            return -1;
        }
        if (l > dir.l) {
            return 1;
        }
        return 0;
    }

    @NotNull
    @Override
    public String toString() {
        return String.format("%.2f° г.ш., %.2f° г.д.", Math.toDegrees(b), Math.toDegrees(l));
    }

    @NotNull
    public static Spheric valueOf(@NotNull final double[] dir) {
        return new Spheric(dir[1], Math.PI / 2 - dir[0]);
    }
}
