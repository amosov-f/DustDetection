package ru.spbu.astro.dust.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.concurrent.Immutable;

import static java.lang.Math.log10;
import static java.lang.Math.pow;

@Immutable
public final class Star implements Comparable<Star> {
    private final int id;
    @NotNull
    private final Spheric dir;
    @NotNull
    private final Value parallax;
    private final double vMag;
    @NotNull
    private final SpectralType spectralType;
    @NotNull
    private final Value bvColor;

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

    @NotNull
    public Value getR() {
        return new Value(1000 / parallax.value, 1000 * parallax.error / pow(parallax.value, 2));
    }

    @Nullable
    public Value getExtinction() {
        final Value bv = spectralType.toBV();
        if (bv == null) {
            return null;
        }
        return bvColor.subtract(bv);
    }

    @NotNull
    public Value getAbsoluteMagnitude() {
        return new Value(
                vMag + 5 * log10(parallax.value) - 10,
                2.5 * log10((1 + parallax.getRelativeError()) / (1 - parallax.getRelativeError()))
        );
    }

    public int getId() {
        return id;
    }

    @NotNull
    public Spheric getDir() {
        return dir;
    }

    @NotNull
    public Value getParallax() {
        return parallax;
    }

    public double getVMag() {
        return vMag;
    }

    @NotNull
    public SpectralType getSpectralType() {
        return spectralType;
    }

    @NotNull
    public Value getBVColor() {
        return bvColor;
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
