package ru.spbu.astro.dust.algo;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.dust.model.Catalogue;
import ru.spbu.astro.dust.model.Star;
import ru.spbu.astro.dust.model.spect.LuminosityClass;
import ru.spbu.astro.dust.model.spect.SpectClass;
import ru.spbu.astro.dust.model.spect.table.SpectTable;

import java.util.*;


/**
 * User: amosov-f
 * Date: 26.10.14
 * Time: 21:12
 */
public class SpectTableCalculator {
    @NotNull
    public static SpectTable calculate(final double missPart) {
        final EnumMap<LuminosityClass, NavigableMap<Integer, Double>> table = new EnumMap<>(LuminosityClass.class);
        final Map<LuminosityClass, Map<Integer, List<Star>>> spect2stars = new EnumMap<>(LuminosityClass.class);
        for (final Star star : Catalogue.HIPPARCOS_UPDATED.getStars()) {
            final LuminosityClass lumin = star.getSpectType().getLumin();
            final SpectClass spect = star.getSpectType().getSpect();

            spect2stars.putIfAbsent(lumin, new HashMap<>());
            spect2stars.get(lumin).putIfAbsent(spect.getCode(), new ArrayList<>());
            spect2stars.get(lumin).get(spect.getCode()).add(star);
        }
        for (final LuminosityClass lumin : spect2stars.keySet()) {
            table.put(lumin, new TreeMap<>());
            for (final Integer code : spect2stars.get(lumin).keySet()) {
                final List<Star> stars = spect2stars.get(lumin).get(code);
                stars.sort(
                        (star1, star2) -> new Double(star1.getExtinction().getMax()).compareTo(star2.getExtinction().getMax())
                );
                final double ext = stars.get(Math.min((int) (missPart * stars.size()), stars.size() - 1)).getBVColor().getMax();
                table.get(lumin).put(code, ext);
            }
        }
        return new SpectTable("Max" + missPart, table);
    }
}
