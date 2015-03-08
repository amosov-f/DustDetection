package ru.spbu.astro.commons.hist;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.commons.spect.LuminosityClass;

import java.util.List;

/**
 * User: amosov-f
 * Date: 12.10.14
 * Time: 19:30
 */
public final class LuminosityClassHist extends StarHist<LuminosityClass, Integer> {
    public LuminosityClassHist() {
        super("Класс светимости");
    }

    @Nullable
    @Override
    public LuminosityClass getX(@NotNull final Star star) {
        return star.getSpectType().getLumin();
    }

    @Nullable
    @Override
    public Integer getY(@NotNull final List<Star> stars) {
        return stars.size() > 1 ? stars.size() : null;
    }
}
