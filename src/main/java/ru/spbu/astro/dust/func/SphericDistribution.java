package ru.spbu.astro.dust.func;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.dust.model.Spheric;
import ru.spbu.astro.dust.model.Value;

public interface SphericDistribution {
    @Nullable
    Value get(@NotNull Spheric dir);
}
