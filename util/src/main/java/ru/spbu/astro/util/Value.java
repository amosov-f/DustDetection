package ru.spbu.astro.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public final class Value implements Comparable<Value> {
    public static final Value ZERO = new Value(0);

    private final double value;
    private final double error;

    public Value(final double value) {
        this(value, 0);
    }

    public Value(final double value, final double error) {
        this.value = value;
        this.error = error;
    }

    public double getValue() {
        return value;
    }

    public double getError() {
        return error;
    }

    public double getRelativeError() {
        return Math.abs(getError() / getValue());
    }

    public double getNSigma(final int n) {
        return value + n * error;
    }

    @NotNull
    public Value add(@NotNull final Value value) {
        return new Value(getValue() + value.getValue(), sqrt(pow(getError(), 2) + pow(value.getError(), 2)));
    }

    @NotNull
    public Value negate() {
        return new Value(-getValue(), getError());
    }

    @NotNull
    public Value subtract(@NotNull final Value other) {
        return add(other.negate());
    }

    @Override
    public int compareTo(@NotNull final Value value) {
        return new Double(getValue()).compareTo(value.getValue());
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
        return String.format("%.3f ± %d%%", getValue(), (int) Math.abs(100 * getError() / getValue()));
    }
}
