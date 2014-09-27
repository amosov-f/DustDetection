package ru.spbu.astro.dust.func;

import gov.fnal.eag.healpix.PixTools;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.dust.util.HealpixTools;
import ru.spbu.astro.dust.model.Spheric;
import ru.spbu.astro.dust.model.Value;

import static java.lang.Math.round;

public class HealpixDistribution implements SphericDistribution {
    @NotNull
    protected final Value[] values;

    public HealpixDistribution(final int nSide) {
        this(new Value[12 * nSide * nSide]);
    }

    public HealpixDistribution(@NotNull final Value[] values) {
        this.values = values.clone();
    }

    @Nullable
    @Override
    public Value get(@NotNull final Spheric dir) {
        return values[getPix(dir)];
    }

    public int getPix(@NotNull final Spheric dir) {
        return (int) new PixTools().ang2pix_ring(HealpixTools.sideNumber(values.length), dir.getTheta(), dir.getPhi());
    }

}
