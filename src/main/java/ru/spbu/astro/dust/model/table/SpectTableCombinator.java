package ru.spbu.astro.dust.model.table;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * User: amosov-f
 * Date: 26.10.14
 * Time: 14:23
 */
public interface SpectTableCombinator {
    @NotNull
    SpectTable combine(@NotNull final SpectTable... tables);
}
