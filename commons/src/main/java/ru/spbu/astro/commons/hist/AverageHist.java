package ru.spbu.astro.commons.hist;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.util.Split;

import java.util.function.Function;

/**
 * User: amosov-f
 * Date: 08.03.15
 * Time: 20:50
 */
public final class AverageHist extends DoubleHist.Lambda<Double> {
    public AverageHist(@NotNull final String name, 
                       @NotNull final Function<Star, Double> fx, 
                       @NotNull final Function<Star, Double> fy, 
                       @NotNull final Split split) 
    {
        super(name, fx, new Average<>(fy), split);
    }
}
