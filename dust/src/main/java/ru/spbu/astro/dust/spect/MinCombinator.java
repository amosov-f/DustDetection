package ru.spbu.astro.dust.spect;

import com.google.common.base.Joiner;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.core.spect.LuminosityClass;
import ru.spbu.astro.core.spect.SpectTable;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * User: amosov-f
 * Date: 16.11.14
 * Time: 0:05
 */
public class MinCombinator implements SpectTableCombinator {
    @NotNull
    @Override
    public SpectTable combine(@NotNull SpectTable... spectTables) {
        final EnumMap<LuminosityClass, NavigableMap<Integer, Double>> table = new EnumMap<>(LuminosityClass.class);
        for (final LuminosityClass lumin : LuminosityClass.values()) {
            boolean ok = true;
            for (final SpectTable spectTable : spectTables) {
                if (!spectTable.getLumins().contains(lumin)) {
                    ok = false;
                }
            }
            if (ok) {
                final NavigableMap<Integer, Double> bvs = new TreeMap<>(spectTables[0].getBVs(lumin));
                table.put(lumin, bvs);
                for (int i = 1; i < spectTables.length; i++) {
                    bvs.keySet().retainAll(spectTables[i].getBVs(lumin).keySet());
                    for (final int code : spectTables[i].getBVs(lumin).keySet()) {
                        if (bvs.containsKey(code)) {
                            bvs.put(code, Math.min(bvs.get(code), spectTables[i].getBVs(lumin).get(code)));
                        }
                    }
                }
            }
        }
        final String name = "min(" + Joiner.on(',').join(Arrays.stream(spectTables).map(SpectTable::getName).toArray()) + ")";
        return new SpectTable(name, table);
    }
}
