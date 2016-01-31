package ru.spbu.astro.commons.hist;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * User: amosov-f
 * Date: 31.01.16
 * Time: 20:57
 */
final class CountOrNull<T> implements Function<Stream<T>, Integer> {
    @Override
    public Integer apply(@NotNull final Stream<T> s) {
        final long count = s.count();
        return count > 1 ? (int) count : null;
    }
}
