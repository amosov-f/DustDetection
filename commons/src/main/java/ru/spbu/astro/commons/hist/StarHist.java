package ru.spbu.astro.commons.hist;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.commons.Star;

import java.util.*;

/**
 * User: amosov-f
 * Date: 12.10.14
 * Time: 18:52
 */
public abstract class StarHist<X extends Comparable<X>, Y extends Number> {
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
    public abstract X getX(@NotNull final Star star);

    @Nullable
    public abstract Y getY(@NotNull final List<Star> stars);

    @NotNull
    public Map<X, Y> hist(@NotNull final List<Star> stars) {
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
            final Y y = getY(bins.get(x));
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
