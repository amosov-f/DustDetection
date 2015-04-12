package ru.spbu.astro.util.ml.target;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.util.Point;
import ru.spbu.astro.util.ml.Regression;

/**
 * User: amosov-f
 * Date: 12.04.15
 * Time: 19:50
 */
public interface Target {
    double value(@NotNull Regression regression, @NotNull Point... test);
}
