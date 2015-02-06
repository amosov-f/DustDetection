package ru.spbu.astro.commons.hist;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.commons.spect.LuminosityClass;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * User: amosov-f
 * Date: 12.10.14
 * Time: 19:30
 */
public final class LuminosityClassHist implements StarHist<String> {
    @NotNull
    @Override
    public Map<String, Integer> hist(@NotNull final List<Star> stars) {
        final Map<String, Integer> counts = new LinkedHashMap<>();
        for (final LuminosityClass luminosityClass : LuminosityClass.values()) {
            counts.put(luminosityClass.name(), 0);
        }
        for (final Star star : stars) {
            final LuminosityClass luminosityClass = star.getSpectType().getLumin();
            if (luminosityClass != null) {
                counts.put(luminosityClass.name(), counts.get(luminosityClass.name()) + 1);
            }
        }
        return clean(counts);
    }

    @NotNull
    @Override
    public String getName() {
        return "Класс светимости";
    }
}
