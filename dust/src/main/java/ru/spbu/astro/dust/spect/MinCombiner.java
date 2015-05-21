package ru.spbu.astro.dust.spect;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.commons.spect.LuminosityClass;
import ru.spbu.astro.commons.spect.SpectTable;

import java.util.*;

/**
 * User: amosov-f
 * Date: 16.11.14
 * Time: 0:05
 */
public final class MinCombiner implements SpectTableCombiner {
    @NotNull
    @Override
    public SpectTable combine(@NotNull final SpectTable... spectTables) {
        final String name = "min(" + Joiner.on(',').join(Arrays.stream(spectTables).map(SpectTable::getName).toArray()) + ")";
        final SpectTable combinedSpectTable = new SpectTable(name);
        for (final LuminosityClass lumin : LuminosityClass.values()) {
            boolean ok = true;
            for (final SpectTable spectTable : spectTables) {
                if (!ArrayUtils.contains(spectTable.getLumins(), lumin)) {
                    ok = false;
                }
            }
            if (ok) {
                final NavigableMap<Integer, Double> bvs = new TreeMap<>(spectTables[0].getBVs(lumin));
                for (int i = 1; i < spectTables.length; i++) {
                    bvs.keySet().retainAll(spectTables[i].getBVs(lumin).keySet());
                    for (final int code : spectTables[i].getBVs(lumin).keySet()) {
                        if (bvs.containsKey(code)) {
                            bvs.put(code, Math.min(bvs.get(code), spectTables[i].getBVs(lumin).get(code)));
                        }
                    }
                }
                for (final int code : bvs.keySet()) {
                    combinedSpectTable.add(lumin, code, bvs.get(code));
                }
            }
        }
        return combinedSpectTable;
    }
}
