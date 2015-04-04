package ru.spbu.astro.healpix.func;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.commons.func.SphericDistribution;
import ru.spbu.astro.util.Value;

import java.util.Arrays;

/**
 * User: amosov-f
 * Date: 04.04.15
 * Time: 20:53
 */
public final class SmoothedDistribution extends HealpixDistribution {
    public SmoothedDistribution(final int nSide, @NotNull final SphericDistribution f, final int iter) {
        super(nSide);
        Arrays.setAll(values, pix -> f.get(healpix.getCenter(pix)));
        for (int i = 0; i < iter; i++) {
            final Value[] copy = Arrays.copyOf(values, values.length);
            Arrays.setAll(
                    copy,
                    pix -> Value.of(Arrays.stream(healpix.getNeighbours(pix))
                            .mapToDouble(neib -> values[neib].getValue())
                            .average()
                            .getAsDouble())
            );
            Arrays.setAll(values, pix -> copy[pix]);
        }
    }
}
