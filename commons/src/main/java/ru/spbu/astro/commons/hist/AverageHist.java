package ru.spbu.astro.commons.hist;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.util.Split;

import java.util.List;
import java.util.function.Function;
import java.util.stream.DoubleStream;

/**
 * User: amosov-f
 * Date: 08.03.15
 * Time: 20:50
 */
public final class AverageHist extends DoubleHist<Double> {
    @NotNull
    private final Function<Star, Double> fy;
    
    public AverageHist(@NotNull final String name, 
                       @NotNull final Function<Star, Double> fx, 
                       @NotNull final Function<Star, Double> fy, 
                       @NotNull final Split split) 
    {
        super(name, fx, split);
        this.fy = fy;
    }

    @Nullable
    @Override
    public Double getY(@NotNull final List<Star> stars) {
        return stars.stream().flatMapToDouble(star -> {
            final Double y = fy.apply(star);
            return y != null ? DoubleStream.of(y) : DoubleStream.empty();
        }).average().getAsDouble();
    }
}
