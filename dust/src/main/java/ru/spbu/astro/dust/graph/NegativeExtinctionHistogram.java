package ru.spbu.astro.dust.graph;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.commons.graph.OutlierHistogram;
import ru.spbu.astro.commons.hist.StarHist;
import ru.spbu.astro.commons.StarFilter;

import java.util.List;

/**
 * User: amosov-f
 * Date: 17.01.15
 * Time: 1:56
 */
public class NegativeExtinctionHistogram extends OutlierHistogram {
    public <T extends Comparable<T>> NegativeExtinctionHistogram(@NotNull final List<Star> stars,
                                                                 @NotNull final StarHist<T, Integer> hist)
    {
        super(stars, StarFilter.NEGATIVE_EXTINCTION, hist);
    }
}
