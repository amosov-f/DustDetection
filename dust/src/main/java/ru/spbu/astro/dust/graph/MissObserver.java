package ru.spbu.astro.dust.graph;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.core.func.HealpixCounter;
import ru.spbu.astro.core.func.SphericDistribution;
import ru.spbu.astro.core.graph.HammerProjection;
import ru.spbu.astro.dust.DustCatalogues;
import ru.spbu.astro.core.StarFilter;

/**
 * User: amosov-f
 * Date: 12.10.14
 * Time: 17:51
 */
public final class MissObserver {
    public static void main(@NotNull final String[] args) {
        final SphericDistribution f = new HealpixCounter(
                new StarFilter(DustCatalogues.HIPPARCOS_UPDATED.getStars()).negativeExtinction().getStars(),
                18
        );
        new HammerProjection(f).setVisible(true);
    }
}
