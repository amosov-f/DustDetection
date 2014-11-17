package ru.spbu.astro.dust.model;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.dust.model.spect.SpectType;

import static java.lang.Math.log10;
import static java.lang.Math.pow;

public final class Star implements Comparable<Star> {
    private final int id;
    @NotNull
    private final Spheric dir;
    @NotNull
    private final Value parallax;
    private final double vMag;
    @NotNull
    private final SpectType spectType;
    @NotNull
    private final Value bvColor;

    public Star(final int id, @NotNull final Spheric dir, @NotNull final Value parallax, final double vMag,
                @NotNull final SpectType spectType, @NotNull final Value bvColor) {
        this.id = id;
        this.dir = dir;
        this.parallax = parallax;
        this.vMag = vMag;
        this.spectType = spectType;
        this.bvColor = bvColor;
    }

    @NotNull
    public Vector3D getCartesian() {
        return dir.getVector().scalarMultiply(getR().getValue());
    }

    @Override
    public int compareTo(@NotNull Star star) {
        return star.getParallax().compareTo(getParallax());
    }

    @NotNull
    public Value getR() {
        return new Value(1000 / parallax.getValue(), 1000 * parallax.getError() / pow(parallax.getValue(), 2));
    }

    @NotNull
    public Value getExtinction() {
        final Value bv = spectType.toBV();
        assert bv != null;
        return bvColor.subtract(bv);
    }

    @NotNull
    public Value getAbsoluteMagnitude() {
        return new Value(
                vMag + 5 * log10(parallax.getValue()) - 10,
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
    public SpectType getSpectType() {
        return spectType;
    }

    @NotNull
    public Value getBVColor() {
        return bvColor;
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Star)) {
            return false;
        }

        Star star = (Star) o;

        return id == star.id;
    }

    @NotNull
    @Override
    public String toString() {
        return String.format(
                "(%d: l = %.3f, b = %.3f, pi = %.3f, dpi = %.3f)",
                id, dir.getL(), dir.getB(), parallax.getValue(), parallax.getError()
        );
    }
}
