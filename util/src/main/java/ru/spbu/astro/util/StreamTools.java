package ru.spbu.astro.util;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

/**
 * User: amosov-f
 * Date: 31.01.16
 * Time: 20:51
 */
public enum StreamTools {
    ;

    @NotNull
    public static DoubleStream convert(@NotNull final Stream<Double> s) {
        return s.filter(Objects::nonNull).mapToDouble(MoreFunctions.identity());
    }
}
