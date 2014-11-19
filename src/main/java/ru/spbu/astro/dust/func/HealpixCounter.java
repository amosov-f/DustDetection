package ru.spbu.astro.dust.func;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.dust.graph.HammerProjection;
import ru.spbu.astro.dust.model.Catalogue;
import ru.spbu.astro.dust.model.Spheric;
import ru.spbu.astro.dust.model.Star;
import ru.spbu.astro.dust.model.Value;
import ru.spbu.astro.dust.util.StarSelector;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class HealpixCounter extends HealpixDistribution {
    public HealpixCounter(@NotNull final Iterable<Spheric> dirs, final int nSide) {
        super(nSide);
        Arrays.setAll(values, i -> new Value(0, 0));
        for (final Spheric dir : dirs) {
            int pix = getPix(dir);
            values[pix] = values[pix].add(new Value(1, 0));
        }
    }

    public HealpixCounter(@NotNull final List<Star> stars, final int nSide) {
        this(stars.stream().map(Star::getDir).collect(Collectors.toList()), nSide);
    }

    public static void main(@NotNull final String[] args) {
        final Catalogue catalogue = Catalogue.HIPPARCOS_UPDATED;
        new HammerProjection(new HealpixCounter(new StarSelector(catalogue).selectByNegativeExtinction().getStars(), 18)).setVisible(true);
    }
}
