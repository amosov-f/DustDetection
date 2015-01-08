package ru.spbu.astro.dust.func;

import gov.fnal.eag.healpix.PixTools;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.dust.model.Spheric;
import ru.spbu.astro.dust.model.Value;
import ru.spbu.astro.dust.util.HealpixTools;

public class HealpixDistribution implements SphericDistribution {
    @NotNull
    protected final Value[] values;

    public HealpixDistribution(final int nSide) {
        this(new Value[HealpixTools.nPix(nSide)]);
    }

    public HealpixDistribution(@NotNull final Value[] values) {
        this.values = values.clone();
    }

    @Nullable
    @Override
    public final Value get(@NotNull final Spheric dir) {
        return values[getPix(dir)];
    }

    protected final int getPix(@NotNull final Spheric dir) {
        return (int) new PixTools().ang2pix_ring(HealpixTools.nSide(values.length), dir.getPhi(), dir.getTheta());
    }
}
