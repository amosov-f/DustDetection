package ru.spbu.astro.core.func;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.core.Spheric;
import ru.spbu.astro.core.func.SphericDistribution;
import ru.spbu.astro.util.Value;
import ru.spbu.astro.core.HealpixTools;

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
