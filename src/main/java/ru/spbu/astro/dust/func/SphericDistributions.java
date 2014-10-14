package ru.spbu.astro.dust.func;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.dust.model.Spheric;
import ru.spbu.astro.dust.model.Value;

/**
 * User: amosov-f
 * Date: 12.10.14
 * Time: 18:06
 */
public class SphericDistributions {
    public static SphericDistribution sum(@NotNull final SphericDistribution f1, @NotNull final SphericDistribution f2) {
        return dir -> {
            final Value v1 = f1.get(dir);
            final Value v2 = f2.get(dir);
            if (v1 == null || v2 == null) {
                return null;
            }
            return v1.add(v2);
        };
    }
}
