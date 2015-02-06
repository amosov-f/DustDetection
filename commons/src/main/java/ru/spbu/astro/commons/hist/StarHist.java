package ru.spbu.astro.commons.hist;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.commons.Star;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * User: amosov-f
 * Date: 12.10.14
 * Time: 18:52
 */
public interface StarHist<T extends Comparable<T>> {
    @NotNull
    Map<T, Integer> hist(@NotNull List<Star> stars);

    @NotNull
    String getName();

    @NotNull
    default Map<String, Integer> clean(@NotNull final Map<String, Integer> counts) {
        final Map<String, Integer> cleanedCounts = new LinkedHashMap<>(counts);
        counts.keySet().stream().filter(name -> counts.get(name) <= 1).forEach(cleanedCounts::remove);
        return cleanedCounts;
    }
}
