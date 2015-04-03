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

    private Value(final double value, final double error) {
        this.value = value;
        this.error = error;
    }

    @NotNull
    public static Value of(final double value) {
        return new Value(value, 0);
    }

    @NotNull
    public static Value of(final double value, final double error) {
        return new Value(value, error);
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
        return Value.of(getValue() + value.getValue(), sqrt(pow(getError(), 2) + pow(value.getError(), 2)));
    }

    @NotNull
    public Value negate() {
        return Value.of(-getValue(), getError());
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
