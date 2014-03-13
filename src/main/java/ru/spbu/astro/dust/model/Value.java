package ru.spbu.astro.dust.model;

public class Value {

    public final double value;
    public final double error;

    public Value(double value, double error) {
        this.value = value;
        this.error = error;
    }

    public Value add(final Value other) {
        return new Value(value + other.value, Math.sqrt(Math.pow(error, 2) + Math.pow(other.error, 2)));
    }

    public Value negate() {
        return new Value(-value, error);
    }

    public Value subtract(final Value other) {
        return add(other.negate());
    }

    @Override
    public String toString() {
        return String.format("%.3f ± %d%%", value, (int) Math.abs(100 * error / value));
    }
}
