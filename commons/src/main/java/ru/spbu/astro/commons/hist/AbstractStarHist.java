package ru.spbu.astro.commons.hist;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.commons.Star;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * User: amosov-f
 * Date: 12.10.14
 * Time: 18:52
 */
public abstract class AbstractStarHist<X, Y extends Number> implements StarHist<X, Y> {
    private static final Logger LOG = Logger.getLogger(AbstractStarHist.class.getName());

    @NotNull
    private final String name;

    protected AbstractStarHist(@NotNull final String name) {
        this.name = name;
    }

    @NotNull
    @Override
    public final String getName() {
        return name;
    }

    @Nullable
    protected abstract X getX(@NotNull final Star star);

    @Nullable
    protected abstract Y getY(@NotNull final Stream<Star> stars);

    @NotNull
    @Override
    public final Map<X, Y> hist(@NotNull final Star[] stars) {
        final Multimap<X, Star> bins = HashMultimap.create();
        for (final Star star : stars) {
            final X x = getX(star);
            if (x != null) {
                bins.get(x).add(star);
            }
        }
        final Map<X, Y> hist = getComparator() == null ? new TreeMap<>() : new TreeMap<>(getComparator());
        for (final X x : bins.keySet()) {
            final Collection<Star> binStars = bins.get(x);
            final Y y = getY(bins.get(x).stream());
            LOG.info(x + " -> " + y + " by " + binStars.size() + " stars");
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

    public static class Lambda<X, Y extends Number> extends AbstractStarHist<X, Y> {
        @NotNull
        private final Function<Star, X> fx;
        @NotNull
        private final Function<Stream<Star>, Y> fy;

        public Lambda(@NotNull final String name,
                      @NotNull final Function<Star, X> fx,
                      @NotNull final Function<Stream<Star>, Y> fy)
        {
            super(name);
            this.fx = fx;
            this.fy = fy;
        }

        @Nullable
        @Override
        protected X getX(@NotNull Star star) {
            return fx.apply(star);
        }

        @Nullable
        @Override
        protected Y getY(@NotNull Stream<Star> stars) {
            return fy.apply(stars);
        }
    }
}
