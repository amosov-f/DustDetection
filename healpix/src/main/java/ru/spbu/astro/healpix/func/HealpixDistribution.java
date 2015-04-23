package ru.spbu.astro.healpix.func;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.healpix.Healpix;
import ru.spbu.astro.commons.Spheric;
import ru.spbu.astro.commons.func.SphericDistribution;
import ru.spbu.astro.util.Filter;
import ru.spbu.astro.util.Value;

import java.util.Arrays;

public class HealpixDistribution implements SphericDistribution {
    @NotNull
    protected final Value[] values;
    @NotNull
    protected final Healpix healpix;

    public HealpixDistribution(final int nSide) {
        this(new Value[Healpix.nPix(nSide)]);
    }

    public HealpixDistribution(@NotNull final Value[] values) {
        this(values, null);
    }

    public HealpixDistribution(@NotNull final Value[] values, @Nullable final Filter<Value> filter) {
        this.values = new Value[values.length];
        Arrays.setAll(this.values, pix -> filter == null || filter.getPredicate().test(values[pix]) ? values[pix] : null);
        healpix = new Healpix(Healpix.nSide(values.length));
    }

    public HealpixDistribution(@NotNull final double[] values) {
        this(Arrays.stream(values).mapToObj(Value::of).toArray(Value[]::new));
    }

    @Nullable
    @Override
    public final Value get(@NotNull final Spheric dir) {
        return values[healpix.getPix(dir)];
    }
}
