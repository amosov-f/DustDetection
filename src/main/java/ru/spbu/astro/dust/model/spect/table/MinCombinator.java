package ru.spbu.astro.dust.model.spect.table;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.dust.model.spect.LuminosityClass;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

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
        final String name = "min(" + Joiner.on(',').join(Arrays.asList(spectTables).stream().map(SpectTable::getName).collect(Collectors.toList())) + ")";
        return new SpectTable(name, table);
    }
}
