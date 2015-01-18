package ru.spbu.astro.dust.spect;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.core.spect.LuminosityClass;
import ru.spbu.astro.core.spect.SpectClass;
import ru.spbu.astro.core.spect.SpectTable;

import java.util.EnumMap;
import java.util.NavigableMap;
import java.util.TreeMap;

import static ru.spbu.astro.core.spect.LuminosityClass.III;

/**
 * User: amosov-f
 * Date: 26.10.14
 * Time: 14:24
 */
public final class IIIM2SpectTableCombinator implements SpectTableCombinator {
    private static final double CODE = SpectClass.parse("M2").getDoubleNumber();

    @NotNull
    @Override
    public SpectTable combine(@NotNull final SpectTable... spectTables) {
        final EnumMap<LuminosityClass, NavigableMap<Integer, Double>> table = new EnumMap<>(LuminosityClass.class);
        for (final LuminosityClass lumin : spectTables[0].table.keySet()) {
            table.put(lumin, new TreeMap<>());
            for (final Integer code : spectTables[0].table.get(lumin).keySet()) {
                if (!lumin.equals(III) || code < CODE) {
                    table.get(lumin).put(code, spectTables[0].table.get(lumin).get(code));
                }
            }
        }
        for (final Integer code : spectTables[1].table.get(III).keySet()) {
            if (code >= CODE) {
                table.get(III).put(code, spectTables[1].table.get(III).get(code));
            }
        }
        return new SpectTable(spectTables[0].getName() + "+" + spectTables[1].getName(), table);
    }
}
