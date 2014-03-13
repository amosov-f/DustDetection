package ru.spbu.astro.dust.func;

import ru.spbu.astro.dust.model.Spheric;
import ru.spbu.astro.dust.model.Value;

public interface SphericDistribution {

    Value get(Spheric dir);

}
