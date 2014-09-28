package ru.spbu.astro.dust.func;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.dust.model.Value;

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
        for (int i = 0; i < values.length; i++) {
            if (values[i] != null) {
                binaryValues[i] = values[i].getValue() >= threshold ? new Value(1) : new Value(0);
            }
        }
        return binaryValues;
    }
}
