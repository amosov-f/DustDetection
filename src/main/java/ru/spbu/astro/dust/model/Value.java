package ru.spbu.astro.dust.model;

public class Value {

    public final double value;
    public final double error;

    public Value(double value, double error) {
        this.value = value;
        this.error = error;
    }

    @Override
    public String toString() {
        return String.format("%.3f ± %d%%", value, (int) Math.abs(100 * error / value));
    }
}
