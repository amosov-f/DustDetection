package ru.spbu.astro.commons.hist;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.commons.spect.SpectClass;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * User: amosov-f
 * Date: 12.10.14
 * Time: 19:26
 */
public final class SpectClassHist extends StarHist<String, Integer> {
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
    public Integer getY(@NotNull final List<Star> stars) {
        return stars.size() > 1 ? stars.size() : null;
    }

    @Nullable
    @Override
    protected Comparator<String> getComparator() {
        return new Comparator<String>() {
            @Override
            public int compare(@NotNull final String x1, @NotNull final String x2) {
                final int compare = Objects.requireNonNull(SpectClass.TypeSymbol.parse(x1.charAt(0)))
                        .compareTo(Objects.requireNonNull(SpectClass.TypeSymbol.parse(x2.charAt(0))));
                return compare != 0 ? compare : x1.substring(1).compareTo(x2.substring(1));
            }
        };
    }
}
