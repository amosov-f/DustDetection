package ru.spbu.astro.dust.graph;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.dust.func.HealpixCounter;
import ru.spbu.astro.dust.func.SphericDistribution;
import ru.spbu.astro.dust.model.Catalogue;
import ru.spbu.astro.dust.util.StarSelector;

/**
 * User: amosov-f
 * Date: 12.10.14
 * Time: 17:51
 */
public class MissObserver {
    public static void main(@NotNull final String[] args) {
        final SphericDistribution f = new HealpixCounter(
                new StarSelector(Catalogue.HIPPARCOS_UPDATED).selectByNegativeExtinction().getStars(),
                18
        );
        final HammerProjection hammerProjection = new HammerProjection(f);
        hammerProjection.setVisible(true);
    }
}
