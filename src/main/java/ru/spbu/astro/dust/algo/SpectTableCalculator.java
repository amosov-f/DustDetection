package ru.spbu.astro.dust.algo;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.dust.model.Catalogue;
import ru.spbu.astro.dust.model.Star;
import ru.spbu.astro.dust.model.table.SpectTable;

import java.awt.geom.Point2D;
import java.util.*;

import static ru.spbu.astro.dust.model.SpectralType.LuminosityClass;

/**
 * User: amosov-f
 * Date: 26.10.14
 * Time: 21:12
 */
public class SpectTableCalculator {
    @NotNull
    public static SpectTable calculate(final double missPart) {
        final SpectTable table = new SpectTable("Max" + missPart);
        final Map<LuminosityClass, Map<String, List<Star>>> spect2stars = new EnumMap<>(LuminosityClass.class);
        for (final Star star : Catalogue.HIPPARCOS_UPDATED.getStars()) {
            final LuminosityClass lumin = star.getSpectralType().getLumin();
            final String spect = star.getSpectralType().getIntSpect();
            spect2stars.putIfAbsent(lumin, new HashMap<>());
            spect2stars.get(lumin).putIfAbsent(spect, new ArrayList<>());
            spect2stars.get(lumin).get(spect).add(star);
        }
        for (final LuminosityClass lumin : spect2stars.keySet()) {
            table.table.put(lumin, new ArrayList<>());
            for (final String spect : spect2stars.get(lumin).keySet()) {
                final List<Star> stars = spect2stars.get(lumin).get(spect);
                stars.sort(
                        (star1, star2) -> new Double(star1.getExtinction().getMax()).compareTo(star2.getExtinction().getMax())
                );
                final double ext = stars.get(Math.min((int) (missPart * stars.size()), stars.size() - 1)).getBVColor().getMax();
                table.table.get(lumin).add(new Point2D.Double(SpectTable.code(spect), ext));
            }
        }
        return table;
    }
}
