package ru.spbu.astro.dust.graph;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.commons.StarFilter;
import ru.spbu.astro.commons.func.SphericDistribution;
import ru.spbu.astro.commons.graph.HammerProjection;
import ru.spbu.astro.dust.DustStars;
import ru.spbu.astro.healpix.func.HealpixCounter;

/**
 * User: amosov-f
 * Date: 12.10.14
 * Time: 17:51
 */
@Deprecated
public final class MissObserver {
    public static void main(@NotNull final String[] args) {
        final SphericDistribution f = new HealpixCounter(
                StarFilter.of(DustStars.ALL).negativeExtinction().stars(),
                18
        );
        new HammerProjection(f).setVisible(true);
    }
}
