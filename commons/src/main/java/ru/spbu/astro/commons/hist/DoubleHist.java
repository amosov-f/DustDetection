package ru.spbu.astro.commons.hist;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.util.Split;
import ru.spbu.astro.util.TextUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * User: amosov-f
 * Date: 25.10.14
 * Time: 18:49
 */
public abstract class DoubleHist<Y extends Number> extends AbstractStarHist<Double, Y> {
    @NotNull
    private final Function<Star, Double> f;
    @NotNull
    private final Split split;

    protected DoubleHist(@NotNull final String name, @NotNull final Function<Star, Double> f, @NotNull final Split split) {
        super(name);
        this.f = f;
        this.split = split;
    }

    @Nullable
    @Override
    protected Double getX(@NotNull final Star star) {
        final Double x = f.apply(star);
        if (x == null) {
            return null;
        }
        if (x < split.getMin() || split.getMax() < x) {
            return null;
        }
        return Arrays.stream(split.getCenters())
                .mapToObj(TextUtils::removeUnnecessaryDigits)
                .min(Comparator.comparing(center -> Math.abs(x - center)))
                .get();
    }

    public static class Lambda<Y extends Number> extends DoubleHist<Y> {
        @NotNull
        private final Function<Stream<Star>, Y> fy;

        public Lambda(@NotNull final String name,
                      @NotNull final Function<Star, Double> fx,
                      @NotNull final Function<Stream<Star>, Y> fy,
                      @NotNull Split split) {
            super(name, fx, split);
            this.fy = fy;
        }

        @Nullable
        @Override
        protected Y getY(@NotNull Stream<Star> stars) {
            return fy.apply(stars);
        }
    }
}
