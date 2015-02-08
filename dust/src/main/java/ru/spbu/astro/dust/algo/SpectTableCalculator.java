package ru.spbu.astro.dust.algo;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.commons.spect.LuminosityClass;
import ru.spbu.astro.commons.spect.SpectTable;
import ru.spbu.astro.dust.DustCatalogs;
import ru.spbu.astro.dust.spect.MinCombiner;

import java.util.*;


/**
 * User: amosov-f
 * Date: 26.10.14
 * Time: 21:12
 */
public final class SpectTableCalculator {
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

    public static void main(@NotNull final String[] args) {
        final SpectTable spectTable = new MinCombiner().combine(SpectTable.TSVETKOV, new SpectTableCalculator().calculate(0.05));
        //spectTable.write(new FileOutputStream("src/main/resources/table/" + spectTable.getName() + ".txt"));
    }

    @NotNull
    public SpectTable calculate(final double outlierPart) {
        final SpectTable spectTable = new SpectTable("max-" + (int) (outlierPart * 100) + "%");
        final Map<LuminosityClass, Map<Integer, List<Star>>> spect2stars = new EnumMap<>(LuminosityClass.class);
        spect2stars.put(LuminosityClass.III, new HashMap<>());
        spect2stars.put(LuminosityClass.V, new HashMap<>());
        for (final Star star : DustCatalogs.HIPPARCOS_UPDATED.getStars()) {
            final LuminosityClass lumin = star.getSpectType().getLumin();
            if (spect2stars.containsKey(lumin)) {
                final int key = key(star.getSpectType().getSpect().getCode());
                spect2stars.get(lumin).putIfAbsent(key, new ArrayList<>());
                spect2stars.get(lumin).get(key).add(star);
            }
        }
        for (final LuminosityClass lumin : spect2stars.keySet()) {
            for (final Integer key : spect2stars.get(lumin).keySet()) {
                final List<Star> stars = spect2stars.get(lumin).get(key);
                stars.sort(Comparator.comparingDouble(star -> star.getExtinction().getNSigma(3)));
                final double ext = stars.get(Math.min((int) (outlierPart * stars.size()), stars.size() - 1)).getBVColor().getNSigma(3);
                spectTable.add(lumin, key, ext);
            }
        }
        return spectTable;
    }
}
