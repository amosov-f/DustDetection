package ru.spbu.astro.healpix.func;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.commons.StarFilter;
import ru.spbu.astro.util.Filter;
import ru.spbu.astro.util.Value;

/**
 * User: amosov-f
 * Date: 04.04.15
 * Time: 0:31
 */
public class PredicateDistribution extends HealpixDistribution {
    public PredicateDistribution(final int nSide, @NotNull final Star[] stars, @NotNull final Filter<Star> filter) {
        super(nSide);
        final Star[][] rings = healpix.split(stars);
        for (int pix = 0; pix < values.length; pix++) {
            if (rings[pix].length != 0) {
                values[pix] = Value.of(1.0 * StarFilter.of(rings[pix]).apply(filter).stars().length / rings[pix].length);
            }
        }
    }
}
