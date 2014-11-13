package ru.spbu.astro.dust.model.spect.table;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.dust.model.spect.LuminosityClass;
import ru.spbu.astro.dust.model.spect.SpectClass;

import java.util.EnumMap;
import java.util.NavigableMap;
import java.util.TreeMap;

import static ru.spbu.astro.dust.model.spect.LuminosityClass.III;

/**
 * User: amosov-f
 * Date: 26.10.14
 * Time: 14:24
 */
public class IIIM2SpectTableCombinator implements SpectTableCombinator {
    private static final double CODE = SpectClass.parse("M2").getNumber();

    @NotNull
    @Override
    public SpectTable combine(@NotNull final SpectTable... tables) {
        final EnumMap<LuminosityClass, NavigableMap<Integer, Double>> table = new EnumMap<>(LuminosityClass.class);
        for (final LuminosityClass lumin : tables[0].table.keySet()) {
            table.put(lumin, new TreeMap<>());
            for (final Integer code : tables[0].table.get(lumin).keySet()) {
                if (!lumin.equals(III) || code < CODE) {
                    table.get(lumin).put(code, tables[0].table.get(lumin).get(code));
                }
            }
        }
        for (final Integer code : tables[1].table.get(III).keySet()) {
            if (code >= CODE) {
                table.get(III).put(code, tables[1].table.get(III).get(code));
            }
        }
        return new SpectTable(tables[0].getName() + "+" + tables[1].getName(), table);
    }
}
