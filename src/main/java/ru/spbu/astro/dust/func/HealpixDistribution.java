package ru.spbu.astro.dust.func;

import gov.fnal.eag.healpix.PixTools;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.dust.model.Spheric;
import ru.spbu.astro.dust.model.Value;

public class HealpixDistribution implements SphericDistribution {
    @NotNull
    protected final Value[] values;

    public HealpixDistribution(final int nSide) {
        values = new Value[12 * nSide * nSide];
    }

    public HealpixDistribution(@NotNull final Value[] values) {
        this.values = values.clone();
    }

    @NotNull
    @Override
    public Value get(@NotNull final Spheric dir) {
        return values[getPix(dir)];
    }

    public int getPix(final Spheric dir) {
        return (int) new PixTools().ang2pix_ring(getNSide(), dir.getTheta(), dir.getPhi());
    }

    public int getNSide() {
        return (int) Math.round(Math.sqrt(values.length / 12.0));
    }

}
