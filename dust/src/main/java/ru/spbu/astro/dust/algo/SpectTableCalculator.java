package ru.spbu.astro.dust.algo;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.core.Star;
import ru.spbu.astro.core.spect.LuminosityClass;
import ru.spbu.astro.dust.model.Catalogues;
import ru.spbu.astro.dust.model.spect.table.MinCombinator;
import ru.spbu.astro.core.spect.SpectTable;

import java.io.FileNotFoundException;
import java.util.*;


/**
 * User: amosov-f
 * Date: 26.10.14
 * Time: 21:12
 */
public class SpectTableCalculator {
    @NotNull
    public SpectTable calculate(final double outlierPart) {
        final EnumMap<LuminosityClass, NavigableMap<Integer, Double>> table = new EnumMap<>(LuminosityClass.class);
        final Map<LuminosityClass, Map<Integer, List<Star>>> spect2stars = new EnumMap<>(LuminosityClass.class);
        spect2stars.put(LuminosityClass.III, new HashMap<>());
        spect2stars.put(LuminosityClass.V, new HashMap<>());
        for (final Star star : Catalogues.HIPPARCOS_UPDATED.getStars()) {
            final LuminosityClass lumin = star.getSpectType().getLumin();
            if (spect2stars.containsKey(lumin)) {
                final int key = key(star.getSpectType().getSpect().getCode());
                spect2stars.get(lumin).putIfAbsent(key, new ArrayList<>());
                spect2stars.get(lumin).get(key).add(star);
            }
        }
        for (final LuminosityClass lumin : spect2stars.keySet()) {
            table.put(lumin, new TreeMap<>());
            for (final Integer key : spect2stars.get(lumin).keySet()) {
                final List<Star> stars = spect2stars.get(lumin).get(key);
                stars.sort(
                        (star1, star2) -> new Double(star1.getExtinction().getMax()).compareTo(star2.getExtinction().getMax())
                );
                final double ext = stars.get(Math.min((int) (outlierPart * stars.size()), stars.size() - 1)).getBVColor().getMax();
                table.get(lumin).put(key, ext);
            }
        }
        return new SpectTable("max-" + (int) (outlierPart * 100) + "%", table);
    }

    private static final int BIN = 3;
    private static final int B = 2;

    private static int key(final int code) {
        final int key = (code - B + (BIN - 1) / 2) / BIN * BIN + B;
        if (key < SpectTable.MIN_CODE) {
            return key(key + BIN);
        }
        if (key > SpectTable.MAX_CODE) {
            return key(key - BIN);
        }
        if (BIN % 2 == 0 && key != SpectTable.MAX_CODE && key + BIN > SpectTable.MAX_CODE) {
            return key + 1;
        }
        return key;
    }

    public static void main(@NotNull final String[] args) throws FileNotFoundException {
        final SpectTable spectTable = new MinCombinator().combine(SpectTable.TSVETKOV, new SpectTableCalculator().calculate(0.05));
        //spectTable.write(new FileOutputStream("src/main/resources/table/" + spectTable.getName() + ".txt"));
    }
}
