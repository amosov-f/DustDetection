package ru.spbu.astro.util.ml.target;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.util.Point;
import ru.spbu.astro.util.ml.Regression;

import java.util.Arrays;

/**
 * User: amosov-f
 * Date: 12.04.15
 * Time: 19:52
 */
public class MeanSquareError implements Target {
    @Override
    public double value(@NotNull final Regression regression, @NotNull final Point... test) {
        return Arrays.stream(test)
                .mapToDouble(p -> Math.pow(regression.predict(p.x().val()) - p.y().val(), 2))
                .sum();
    }
}
