package ru.spbu.astro.healpix.func;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.util.Value;

import java.util.Arrays;

/**
 * User: amosov-f
 * Date: 28.09.14
 * Time: 12:29
 */
public final class HealpixBinaryDistribution extends HealpixDistribution {
    public HealpixBinaryDistribution(@NotNull final Value[] values, final double threshold) {
        super(binaryValues(values, threshold));
    }

    @NotNull
    private static Value[] binaryValues(@NotNull final Value[] values, final double threshold) {
        final Value[] binaryValues = new Value[values.length];
        Arrays.setAll(binaryValues, i -> values[i] != null ? values[i].val() >= threshold ? Value.ONE : Value.ZERO : null);
        return binaryValues;
    }
}
