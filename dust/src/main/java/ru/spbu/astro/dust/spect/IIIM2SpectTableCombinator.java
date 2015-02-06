package ru.spbu.astro.dust.spect;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.commons.spect.LuminosityClass;
import ru.spbu.astro.commons.spect.SpectClass;
import ru.spbu.astro.commons.spect.SpectTable;

import static ru.spbu.astro.commons.spect.LuminosityClass.III;

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
        final SpectTable spectTable = new SpectTable(spectTables[0].getName() + "+" + spectTables[1].getName());
        for (final LuminosityClass lumin : spectTables[0].getLumins()) {
            for (final Integer code : spectTables[0].getBVs(lumin).keySet()) {
                if (lumin != III || code < CODE) {
                    spectTable.add(lumin, code, spectTables[0].getBVs(lumin).get(code));
                }
            }
        }
        for (final Integer code : spectTables[1].getBVs(III).keySet()) {
            if (code >= CODE) {
                spectTable.add(III, code, spectTables[1].getBVs(III).get(code));
            }
        }
        return spectTable;
    }
}
