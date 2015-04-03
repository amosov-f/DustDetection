package ru.spbu.astro.healpix.func;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.commons.StarFilter;
import ru.spbu.astro.util.Value;

import java.util.function.Predicate;

/**
 * User: amosov-f
 * Date: 04.04.15
 * Time: 0:31
 */
public class PredicateDistribution extends HealpixDistribution {
    public PredicateDistribution(final int nSide, @NotNull final Star[] stars, @NotNull final Predicate<Star> predicate) {
        super(nSide);
        final Star[][] rings = healpix.split(stars);
        for (int i = 0; i < values.length; i++) {
            if (rings[i].length != 0) {
                values[i] = Value.of(1.0 * StarFilter.of(rings[i]).filter("filter", predicate).stars().length / rings[i].length);
            }
        }
    }
}
