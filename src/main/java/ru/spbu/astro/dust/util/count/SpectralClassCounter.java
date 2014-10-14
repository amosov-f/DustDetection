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
    @NotNull
    @Override
    public Map<String, Integer> count(@NotNull List<Star> stars) {
        final Map<String, Integer> counts = new LinkedHashMap<>();
        for (final SpectralType.TypeSymbol typeSymbol : SpectralType.TypeSymbol.values()) {
            if (typeSymbol != SpectralType.TypeSymbol.O) {
                counts.put(typeSymbol.name() + "0-4", 0);
            }
            counts.put(typeSymbol.name() + "5-9", 0);
        }
        for (final Star star : stars) {
            final SpectralType.TypeSymbol typeSymbol = star.getSpectralType().getTypeSymbol();
            final double d = star.getSpectralType().getTypeNumber();
            final String spectralClass;
            if (d < 5) {
                spectralClass = typeSymbol + "0-4";
            } else {
                spectralClass = typeSymbol + "5-9";
            }
            if (counts.containsKey(spectralClass)) {
                counts.put(spectralClass, counts.get(spectralClass) + 1);
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
