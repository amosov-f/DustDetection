package ru.spbu.astro.healpix.func;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.commons.Spheric;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.util.Value;

import java.util.Arrays;

public final class HealpixCounter extends HealpixDistribution {
    public HealpixCounter(@NotNull final Spheric[] dirs, final int nSide) {
        super(nSide);
        Arrays.fill(values, Value.ZERO);
        for (final Spheric dir : dirs) {
            final int pix = healpix.getPix(dir);
            values[pix] = values[pix].add(Value.ONE);
        }
    }

    public HealpixCounter(@NotNull final Star[] stars, final int nSide) {
        this(Arrays.stream(stars).map(Star::getDir).toArray(Spheric[]::new), nSide);
    }

//    public static void main(@NotNull final String[] args) {
//        final Catalogue catalogue = Catalogue.HIPPARCOS_UPDATED;
//        new HammerProjection(new HealpixCounter(new StarSelector(catalogue).selectByNegativeExtinction().getStars(), 18)).setVisible(true);
//    }
}
