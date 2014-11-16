package ru.spbu.astro.dust.model.spect.table;

import org.jetbrains.annotations.NotNull;

/**
 * User: amosov-f
 * Date: 26.10.14
 * Time: 14:23
 */
public interface SpectTableCombinator {
    @NotNull
    SpectTable combine(@NotNull final SpectTable... spectTables);
}
