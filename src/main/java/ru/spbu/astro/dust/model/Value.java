package ru.spbu.astro.dust.model;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

public final class Value implements Comparable<Value> {
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

    public double getMax() {
        return value + 3 * error;
    }

    @NotNull
    public Value add(@NotNull final Value value) {
        return new Value(getValue() + value.getValue(), Math.sqrt(Math.pow(getError(), 2) + Math.pow(value.getError(), 2)));
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

    @NotNull
    @Override
    public String toString() {
        return String.format("%.3f ± %d%%", getValue(), (int) Math.abs(100 * getError() / getValue()));
    }
}
