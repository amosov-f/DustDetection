package ru.spbu.astro.dust.func;

import ru.spbu.astro.dust.model.Spheric;
import ru.spbu.astro.dust.model.Value;

public class HealpixCounter extends HealpixDistribution {

    public HealpixCounter(final Iterable<Spheric> dirs, int nSide) {
        super(nSide);

        for (int i = 0; i < values.length; ++i) {
            values[i] = new Value(0, 0);
        }

        for (final Spheric dir : dirs) {
            int pix = getPix(dir);
            values[pix] = values[pix].add(new Value(1, 0));
        }

    }

}
