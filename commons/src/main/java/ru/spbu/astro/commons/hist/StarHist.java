package ru.spbu.astro.commons.hist;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.commons.Star;

import java.util.Map;

/**
 * User: amosov-f
 * Date: 31.01.16
 * Time: 20:36
 */
public interface StarHist<X, Y extends Number> {
    @NotNull
    String getName();

    @NotNull
    Map<X, Y> hist(@NotNull final Star... stars);
}
