package ru.spbu.astro.commons.func;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.commons.HealpixTools;
import ru.spbu.astro.commons.Spheric;
import ru.spbu.astro.util.Value;

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
        return HealpixTools.pix(HealpixTools.nSide(values.length), dir);
    }
}
