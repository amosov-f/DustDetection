package ru.spbu.astro.dust.util.count;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.dust.model.Star;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * User: amosov-f
 * Date: 12.10.14
 * Time: 18:52
 */
public abstract class Counter<T extends Comparable<T>> {
    @NotNull
    public abstract Map<T, Integer> count(@NotNull final List<Star> stars);

    @NotNull
    public abstract String getName();

    private static final int LIMIT = 1;

    @NotNull
    protected Map<String, Integer> clean(@NotNull final Map<String, Integer> counts) {
        final Map<String, Integer> cleanedCounts = new LinkedHashMap<>(counts);
        counts.keySet().stream().filter(name -> counts.get(name) <= LIMIT).forEach(cleanedCounts::remove);
        return cleanedCounts;
    }
}
