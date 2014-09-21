package ru.spbu.astro.dust.func;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.dust.model.Spheric;
import ru.spbu.astro.dust.model.Value;

public interface SphericDistribution {
    @NotNull
    Value get(@NotNull final Spheric dir);
}
