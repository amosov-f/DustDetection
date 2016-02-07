package ru.spbu.astro.commons;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.commons.spect.SpectType;
import ru.spbu.astro.util.Value;

import java.util.Objects;

import static java.lang.Math.*;

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
    @NotNull
    private final Value raProperMotion;
    @NotNull
    private final Value decProperMotion;

    public Star(@NotNull final Builder builder) {
        id = builder.id;
        dir = builder.dir;
        parallax = builder.parallax;
        vMag = builder.vMag;
        spectType = builder.spectType;
        bvColor = builder.bvColor;
        raProperMotion = Objects.requireNonNull(builder.raProperMotion, "No RA proper motion!");
        decProperMotion = Objects.requireNonNull(builder.decProperMotion, "No DEC proper motion!");
    }

    @NotNull
    public Vector3D getCartesian() {
        return dir.getVector().scalarMultiply(getR().val());
    }

    @NotNull
    public Value getR() {
        final double p = parallax.val();
        final double dp = parallax.err();
        final double r = 1000 / (p * (1 + 1.2 * Math.pow(dp / p, 2)));
        return Value.of(r, dp / p * r);
    }

    @NotNull
    public Value getExtinction() {
        return bvColor.subtract(Objects.requireNonNull(spectType.toBV(), "Star #" + id + " hasn't B-V_int"));
    }

    @NotNull
    @SuppressWarnings("MagicNumber")
    public Value getAbsMag() {
        return Value.of(
                vMag + 5 * log10(parallax.val()) - 10,
                2.5 * log10((1 + parallax.relErr()) / (1 - parallax.relErr()))
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
    public Value getRaProperMotion() {
        return raProperMotion;
    }

    @NotNull
    public Value getDecProperMotion() {
        return decProperMotion;
    }

    public double getProperMotion() {
        final double muDec = getDecProperMotion().val();
        final double muRa = getRaProperMotion().val();
        return sqrt(pow(muDec, 2) + pow(muRa, 2));
    }

    @NotNull
    @Override
    public String toString() {
        return String.format(
                "(#%d: l = %.3f, b = %.3f, pi = %.3f, dpi = %.3f)",
                id, dir.getL(), dir.getB(), parallax.val(), parallax.err()
        );
    }

    public static final class Builder {
        private final int id;
        private Spheric dir;
        private Value parallax;
        private double vMag;
        private SpectType spectType;
        private Value bvColor;
        @Nullable
        private Value raProperMotion;
        @Nullable
        private Value decProperMotion;

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
            setRaProperMotion(star.raProperMotion);
            setDecProperMotion(star.decProperMotion);
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
        public Builder setRaProperMotion(@NotNull final Value raProperMotion) {
            this.raProperMotion = raProperMotion;
            return this;
        }

        @NotNull
        public Builder setDecProperMotion(@NotNull final Value decProperMotion) {
            this.decProperMotion = decProperMotion;
            return this;
        }

        @NotNull
        public Star build() {
            return new Star(this);
        }
    }
}
