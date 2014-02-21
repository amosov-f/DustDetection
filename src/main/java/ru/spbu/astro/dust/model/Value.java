package ru.spbu.astro.dust.model;

public class Value {

    public final double value;
    public final double err;

    public Value(double value, double err) {
        this.value = value;
        this.err = err;
    }

    @Override
    public String toString() {
        return String.format("%.3f ± %d%%", value, (int) (100 * err));
    }
}
