package ru.spbu.astro.commons.hist;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.commons.Star;

import java.util.*;
import java.util.logging.Logger;

/**
 * User: amosov-f
 * Date: 12.10.14
 * Time: 18:52
 */
public abstract class StarHist<X extends Comparable<X>, Y extends Number> {
    private static final Logger LOG = Logger.getLogger(StarHist.class.getName());

    @NotNull
    private final String name;

    protected StarHist(@NotNull final String name) {
        this.name = name;
    }

    @NotNull
    public final String getName() {
        return name;
    }

    @Nullable
    protected abstract X getX(@NotNull final Star star);

    @Nullable
    protected abstract Y getY(@NotNull final Star[] stars);

    @NotNull
    public Map<X, Y> hist(@NotNull final Star[] stars) {
        final Map<X, List<Star>> bins = new TreeMap<>();
        for (final Star star : stars) {
            final X x = getX(star);
            if (x != null) {
                bins.putIfAbsent(x, new ArrayList<>());
                bins.get(x).add(star);
            }
        }
        final Map<X, Y> hist = getComparator() == null ? new TreeMap<>() : new TreeMap<>(getComparator());
        for (final X x : bins.keySet()) {
            final Star[] binStars = bins.get(x).stream().toArray(Star[]::new);
            final Y y = getY(binStars);
            LOG.info(x + " -> " + y + " by " + binStars.length + " stars");
            if (y != null) {
                hist.put(x, y);
            }
        }
        return hist;
    }
    
    @Nullable
    protected Comparator<X> getComparator() {
        return null;
    }
}
