package ru.spbu.astro.util;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * User: amosov-f
 * Date: 31.01.16
 * Time: 20:51
 */
public enum StreamTools {
    ;

    @NotNull
    public static DoubleStream convert(@NotNull final Stream<Double> s) {
        return s.filter(Objects::nonNull).mapToDouble(d -> d);
    }

    @NotNull
    public static <T, U, R> Stream<R> map(@NotNull final Stream<T> s1, @NotNull final Stream<U> s2, @NotNull final BiFunction<T, U, R> f) {
        return stream(new Iterator<R>() {
            @Override
            public boolean hasNext() {
                return s1.iterator().hasNext() && s2.iterator().hasNext();
            }

            @Override
            public R next() {
                return f.apply(s1.iterator().next(), s2.iterator().next());
            }
        });
    }

    @NotNull
    public static <T> Stream<T> stream(@NotNull final Iterator<T> it) {
        return stream(() -> it);
    }

    @NotNull
    public static <T> Stream<T> stream(@NotNull final Iterable<T> it) {
        return StreamSupport.stream(it.spliterator(), false);
    }
}
