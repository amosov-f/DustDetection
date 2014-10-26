package ru.spbu.astro.dust.util.count;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.dust.model.SpectralType;
import ru.spbu.astro.dust.model.Star;
import ru.spbu.astro.dust.util.StarSelector;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static ru.spbu.astro.dust.model.SpectralType.LuminosityClass;

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
            counts.put(typeSymbol.name() + "0-4", 0);
            counts.put(typeSymbol.name() + "5-9", 0);
        }
        for (final Star star : stars) {
            final SpectralType.TypeSymbol typeSymbol = star.getSpectralType().getTypeSymbol();
            final String spectType;
            if (star.getSpectralType().getTypeNumber() < 5) {
                spectType = typeSymbol + "0-4";
            } else {
                spectType = typeSymbol + "5-9";
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
