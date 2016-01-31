package ru.spbu.astro.commons.hist;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.commons.spect.SpectClass;

import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * User: amosov-f
 * Date: 31.01.16
 * Time: 20:25
 */
public abstract class SpectClassHist<Y extends Number> extends AbstractStarHist<String, Y> {
    private final int bin;

    public SpectClassHist(final int bin) {
        super("Спектральный класс");
        this.bin = bin;
    }

    @Nullable
    @Override
    public String getX(@NotNull final Star star) {
        final SpectClass spect = SpectClass.valueOf(star.getSpectType().getSpect().getCode());
        final String symbol = spect.getSymbol().name();
        final int number = spect.getDoubleNumber().intValue();
        int l = number / bin * bin;
        if (l + bin > 10) {
            l -= bin;
        }
        final int r = l + 2 * bin > 10 ? 10 : l + bin;
        return r == l + 1 ? symbol + l : symbol + l + "-" + r;
    }

    @Nullable
    @Override
    protected Comparator<String> getComparator() {
        return (spect1, spect2) -> new CompareToBuilder()
                .append(SpectClass.TypeSymbol.parse(spect1.charAt(0)), SpectClass.TypeSymbol.parse(spect2.charAt(0)))
                .append(spect1.substring(1), spect2.substring(1))
                .build();
    }

    public static class Lambda<Y extends Number> extends SpectClassHist<Y> {
        @NotNull
        private final Function<Stream<Star>, Y> fy;

        public Lambda(int bin, @NotNull final Function<Stream<Star>, Y> fy) {
            super(bin);
            this.fy = fy;
        }

        @Nullable
        @Override
        protected Y getY(@NotNull Stream<Star> stars) {
            return fy.apply(stars);
        }
    }

    public static class Average extends Lambda<Double> {
        public Average(int bin, @NotNull Function<Star, Double> fy) {
            super(bin, new ru.spbu.astro.commons.hist.Average<>(fy));
        }
    }

    public static class Count extends Lambda<Integer> {
        public Count(int bin) {
            super(bin, new CountOrNull<>());
        }
    }
}
