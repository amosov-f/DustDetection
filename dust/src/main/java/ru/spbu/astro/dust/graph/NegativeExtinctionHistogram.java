package ru.spbu.astro.dust.graph;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.core.Star;
import ru.spbu.astro.core.graph.OutlierHistogram;
import ru.spbu.astro.core.hist.StarHist;
import ru.spbu.astro.core.StarFilter;

import java.util.List;

/**
 * User: amosov-f
 * Date: 17.01.15
 * Time: 1:56
 */
public class NegativeExtinctionHistogram extends OutlierHistogram {
    public <T extends Comparable<T>> NegativeExtinctionHistogram(@NotNull List<Star> stars, @NotNull StarHist<T> counter) {
        super(stars, StarFilter.NEGATIVE_EXTINCTION, counter);
    }
}
