package ru.spbu.astro.dust.util.count;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.dust.model.SpectralType;
import ru.spbu.astro.dust.model.Star;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * User: amosov-f
 * Date: 12.10.14
 * Time: 19:26
 */
public class SpectralClassCounter extends Counter<String> {
    private final int bin;

    public SpectralClassCounter(final int bin) {
        this.bin = bin;
    }

    @NotNull
    @Override
    public Map<String, Integer> count(@NotNull List<Star> stars) {
        final Map<String, Integer> counts = new LinkedHashMap<>();
        for (final SpectralType.TypeSymbol typeSymbol : SpectralType.TypeSymbol.values()) {
            for (int l = 0; l < 10; l += bin) {
                final int r = l + 2 * bin > 10 ? 10 : l + bin;
                if (r == l + 1) {
                    counts.put(typeSymbol.name() + l, 0);
                } else {
                    counts.put(typeSymbol.name() + l + "-" + r, 0);
                }

            }
        }
        for (final Star star : stars) {
            final SpectralType.TypeSymbol typeSymbol = star.getSpectralType().getTypeSymbol();
            final int n = (int) star.getSpectralType().getTypeNumber();
            int l = n / bin * bin;
            if (l + bin > 10) {
                l -= bin;
            }
            final int r = l + 2 * bin > 10 ? 10 : l + bin;
            //System.out.println(star.getSpectralType().getTypeNumber() + " -> " + l + " " + r);
            final String spectType;
            if (r == l + 1) {
                spectType = typeSymbol.name() + l;
            } else {
                spectType = typeSymbol.name() + l + "-" + r;
            }
            if (counts.containsKey(spectType)) {
                counts.put(spectType, counts.get(spectType) + 1);
            }
        }
        return clean(counts);
    }

    @NotNull
    @Override
    public String getName() {
        return "Спектральный класс";
    }
}
