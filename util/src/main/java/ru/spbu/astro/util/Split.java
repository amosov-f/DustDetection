package ru.spbu.astro.util;

import org.jetbrains.annotations.NotNull;

import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * User: amosov-f
 * Date: 08.03.15
 * Time: 18:36
 */
public final class Split {
    private final double min;
    private final double max;
    private final int size;
    
    public Split(final int size) {
        this(0, 1, size);
    }

    public Split(final double del) {
        this(0, 1, del);
    }

    public Split(final double min, final double max, final double del) {
        this(min, max, (int) ((max - min) / del));
    }

    public Split(final double min, final double max, final int size) {
        this.min = min;
        this.max = max;
        this.size = size;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public int getSize() {
        return size;
    }
    
    public double getDel() {
        return (max - min) / size;
    }
    
    @NotNull
    public DoubleStream getCenters() {
        return IntStream.range(0, size)
                .mapToDouble(i -> min + getDel() * (i + 0.5))
                .map(TextUtils::removeUnnecessaryDigits);
    }
}
