package ru.spbu.astro.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public final class Value implements Comparable<Value> {
    public static final Value ZERO = of(0);
    public static final Value ONE = of(1);

    private final double value;
    private final double error;

    public static final Filter<Value> TWO_SIGMA = nSigmaFilter(2);
    public static final Filter<Value> THREE_SIGMA = nSigmaFilter(3);
    public static final Filter<Value> POS_TWO_SIGMA = nSigmaPosFilter(2);
    public static final Filter<Value> POS_THREE_SIGMA = nSigmaPosFilter(3);

    private Value(final double value, final double error) {
        if (error < 0) {
            throw new IllegalArgumentException("Error " + error + " is below zero!");
        }
        this.value = value;
        this.error = error;
    }

    @NotNull
    private static Filter<Value> nSigmaFilter(final int n) {
        return Filter.by("|k| / sigma_k > " + n, x -> Math.abs(x.val()) > n * x.err());
    }

    @NotNull
    private static Filter<Value> nSigmaPosFilter(final int n) {
        return Filter.by("k / sigma_k > " + n, x -> x.val() > n * x.err());
    }

    @NotNull
    public static Filter<Value> filterByErr(final double limit) {
        return Filter.by("dk < " + limit, x -> x.err() < limit);
    }

    @NotNull
    public static Value of(final double value) {
        return new Value(value, 0);
    }

    @NotNull
    public static Value of(final double value, final double error) {
        return new Value(value, error);
    }

    public double val() {
        return value;
    }

    public double err() {
        return error;
    }

    public double relErr() {
        return Math.abs(err() / val());
    }

    public double snr() {
        return val() / err();
    }

    public double plusNSigma(final int n) {
        return value + n * error;
    }

    @NotNull
    public Value add(@NotNull final Value value) {
        return of(val() + value.val(), sqrt(pow(err(), 2) + pow(value.err(), 2)));
    }

    @NotNull
    public Value negate() {
        return of(-val(), err());
    }

    @NotNull
    public Value subtract(@NotNull final Value other) {
        return add(other.negate());
    }

    @Override
    public int compareTo(@NotNull final Value value) {
        return new Double(val()).compareTo(value.val());
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Value other = (Value) o;
        return Double.compare(other.error, error) == 0 && Double.compare(other.value, this.value) == 0;
    }

    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(value);
        int result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(error);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @NotNull
    @Override
    public String toString() {
        return String.format("%.3f ± %d%%", val(), (int) Math.abs(100 * err() / val()));
    }
}
