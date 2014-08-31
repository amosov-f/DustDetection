package ru.spbu.astro.dust.model;

import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.Immutable;

@Immutable
public final class Star implements Comparable<Star> {
    public final int id;
    @NotNull
    public final Spheric dir;
    @NotNull
    public final Value parallax;
    public final double vMag;
    @NotNull
    public final SpectralType spectralType;
    @NotNull
    public final Value bvColor;

    public Star(int id, @NotNull Spheric dir, @NotNull Value parallax, double vMag, @NotNull SpectralType spectralType, @NotNull Value bvColor) {
        this.id = id;
        this.dir = dir;
        this.parallax = parallax;
        this.vMag = vMag;
        this.spectralType = spectralType;
        this.bvColor = bvColor;
    }

    @NotNull
    public double[] getCartesian() {
        double r = getR().value;
        double theta = dir.getTheta();
        double phi = dir.getPhi();

        return new double[]{
                r * Math.sin(theta) * Math.cos(phi),
                r * Math.sin(theta) * Math.sin(phi),
                r * Math.cos(theta)
        };
    }

    @Override
    public int compareTo(@NotNull Star s) {
        if (parallax.value > s.parallax.value) {
            return -1;
        }
        if (parallax.value < s.parallax.value) {
            return 1;
        }
        return 0;
    }

    public Value getR() {
        return new Value(1000 / parallax.value, 1000 * parallax.error / Math.pow(parallax.value, 2));
    }

    public Value getExtinction() {
        return bvColor.subtract(spectralType.toBV());
    }

    public Value getAbsoluteMagnitude() {
        return new Value(
                vMag + 5 * Math.log10(parallax.value) - 10,
                2.5 * Math.log10((1 + parallax.getRelativeError()) / (1 - parallax.getRelativeError()))
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Star)) {
            return false;
        }

        Star star = (Star) o;

        return id == star.id;
    }

    @Override
    public String toString() {
        return String.format(
                "(%d: l = %.3f, b = %.3f, pi = %.3f, dpi = %.3f)",
                id, dir.l, dir.b, parallax.value, parallax.error
        );
    }
}
