package ru.spbu.astro.dust.func;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.dust.graph.HammerProjection;
import ru.spbu.astro.dust.model.Catalogue;
import ru.spbu.astro.dust.model.Spheric;
import ru.spbu.astro.dust.model.Star;
import ru.spbu.astro.dust.model.Value;

import java.util.List;
import java.util.stream.Collectors;

public final class HealpixCounter extends HealpixDistribution {

    public HealpixCounter(@NotNull final Iterable<Spheric> dirs, final int nSide) {
        super(nSide);

        for (int i = 0; i < values.length; ++i) {
            values[i] = new Value(0, 0);
        }

        for (Spheric dir : dirs) {
            int pix = getPix(dir);
            values[pix] = values[pix].add(new Value(1, 0));
        }
    }

    public static void main(@NotNull final String[] args) {
        final Catalogue catalogue = Catalogue.HIPPARCOS_2007;

        final List<Spheric> dirs = catalogue.getStars().stream().map(Star::getDir).collect(Collectors.toList());

        new HammerProjection(new HealpixCounter(dirs, 18));
    }

}
