package ru.spbu.astro.commons.func;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.commons.Spheric;
import ru.spbu.astro.util.Value;

public interface SphericDistribution {
    @Nullable
    Value get(@NotNull Spheric dir);
}
