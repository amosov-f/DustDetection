package ru.spbu.astro.core.hist;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.core.Star;
import ru.spbu.astro.core.spect.SpectClass;
import ru.spbu.astro.core.spect.SpectTable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * User: amosov-f
 * Date: 12.10.14
 * Time: 19:26
 */
public final class SpectClassHist implements StarHist<String> {
    private final int bin;

    public SpectClassHist(final int bin) {
        this.bin = bin;
    }

    @NotNull
    @Override
    public Map<String, Integer> hist(@NotNull List<Star> stars) {
        final Map<String, Integer> counts = new LinkedHashMap<>();
        SpectTable.getCodeRange().forEach(code -> counts.put(key(code), 0));
        for (final Star star : stars) {
            final String key = key(star.getSpectType().getSpect().getCode());
            if (counts.containsKey(key)) {
                counts.put(key, counts.get(key) + 1);
            }
        }
        return clean(counts);
    }

    @NotNull
    private String key(final int code) {
        final SpectClass spect = SpectClass.valueOf(code);
        final String symbol = spect.getSymbol().name();
        final int number = spect.getDoubleNumber().intValue();
        int l = number / bin * bin;
        if (l + bin > 10) {
            l -= bin;
        }
        final int r = l + 2 * bin > 10 ? 10 : l + bin;
        return r == l + 1 ? symbol + l : symbol + l + "-" + r;
    }

    @NotNull
    @Override
    public String getName() {
        return "Спектральный класс";
    }
}
