package ru.spbu.astro.dust.model;

import jdk.nashorn.internal.ir.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

@Immutable
public final class Value {
    public final double value;
    public final double error;

    public Value(final double value, final double error) {
        this.value = value;
        this.error = error;
    }

    public double getRelativeError() {
        return Math.abs(error / value);
    }

    @NotNull
    public Value add(@NotNull final Value value) {
        return new Value(this.value + value.value, Math.sqrt(Math.pow(error, 2) + Math.pow(value.error, 2)));
    }

    @NotNull
    public Value negate() {
        return new Value(-value, error);
    }

    @NotNull
    public Value subtract(@NotNull final Value other) {
        return add(other.negate());
    }

    @NotNull
    @Override
    public String toString() {
        return String.format("%.3f ± %d%%", value, (int) Math.abs(100 * error / value));
    }
}
