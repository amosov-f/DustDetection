package ru.spbu.astro.commons;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.commons.spect.SpectType;
import ru.spbu.astro.util.Value;

import static java.lang.Math.log10;
import static java.lang.Math.pow;

public final class Star {
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

    public Star(@NotNull final Builder builder) {
        id = builder.id;
        dir = builder.dir;
        parallax = builder.parallax;
        vMag = builder.vMag;
        spectType = builder.spectType;
        bvColor = builder.bvColor;
    }

    @NotNull
    public Vector3D getCartesian() {
        return dir.getVector().scalarMultiply(getR().getValue());
    }

    @NotNull
    public Value getR() {
        return new Value(1000 / parallax.getValue(), 1000 * parallax.getError() / pow(parallax.getValue(), 2));
    }

    @NotNull
    public Value getExtinction() {
        final Value bv = spectType.toBV();
        if (bv == null) {
            throw new RuntimeException("Star #" + id + " hasn't B-V_int");
        }
        return bvColor.subtract(bv);
    }

    @NotNull
    @SuppressWarnings("MagicNumber")
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

    @NotNull
    @Override
    public String toString() {
        return String.format(
                "(#%d: l = %.3f, b = %.3f, pi = %.3f, dpi = %.3f)",
                id, dir.getL(), dir.getB(), parallax.getValue(), parallax.getError()
        );
    }

    public static final class Builder {
        private final int id;
        private Spheric dir;
        private Value parallax;
        private double vMag;
        private SpectType spectType;
        private Value bvColor;

        public Builder(final int id) {
            this.id = id;
        }

        public Builder(@NotNull final Star star) {
            this(star.getId());
            setDir(star.dir);
            setParallax(star.parallax);
            setVMag(star.vMag);
            setSpectType(star.spectType);
            setBVColor(star.bvColor);
        }

        @NotNull
        public Builder setDir(@NotNull final Spheric dir) {
            this.dir = dir;
            return this;
        }

        @NotNull
        public Builder setParallax(@NotNull final Value parallax) {
            this.parallax = parallax;
            return this;
        }

        @NotNull
        public Builder setVMag(final double vMag) {
            this.vMag = vMag;
            return this;
        }

        @NotNull
        public Builder setSpectType(@NotNull final SpectType spectType) {
            this.spectType = spectType;
            return this;
        }

        @NotNull
        public Builder setBVColor(@NotNull final Value bvColor) {
            this.bvColor = bvColor;
            return this;
        }

        @NotNull
        public Star build() {
            return new Star(this);
        }
    }
}
