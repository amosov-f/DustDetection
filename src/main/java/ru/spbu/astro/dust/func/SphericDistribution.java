package ru.spbu.astro.dust.func;

import ru.spbu.astro.dust.model.Spheric;

public interface SphericDistribution {
    double[] get(final Spheric dir);
    int dim();
}
