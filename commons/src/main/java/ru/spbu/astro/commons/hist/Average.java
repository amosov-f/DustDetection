package ru.spbu.astro.commons.hist;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.util.StreamTools;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * User: amosov-f
 * Date: 31.01.16
 * Time: 22:22
 */
public class Average<T> implements Function<Stream<T>, Double> {
    @NotNull
    private final Function<T, Double> f;

    public Average(@NotNull final Function<T, Double> f) {
        this.f = f;
    }

    @NotNull
    @Override
    public Double apply(@NotNull final Stream<T> stream) {
        return StreamTools.convert(stream.map(f)).average().getAsDouble();
    }
}
