package ru.spbu.astro.util;

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * User: amosov-f
 * Date: 04.04.15
 * Time: 17:51
 */
public final class Filter<T> {
    @NotNull
    private final String name;

    @NotNull
    private final Predicate<T> predicate;

    private Filter(@NotNull final String name, @NotNull final Predicate<T> predicate) {
        this.name = name;
        this.predicate = predicate;
    }

    @NotNull
    public static <T> Filter<T> by(@NotNull final String name, @NotNull final Predicate<T> predicate) {
        return new Filter<>(name, predicate);
    }

    @NotNull
    public static <T> Filter<T> trueFilter() {
        return by("all", t -> true);
    }

    @NotNull
    public Filter<T> negate() {
        return new Filter<>("not " + name, predicate.negate());
    }

    @NotNull
    public Filter<T> and(@NotNull final Filter<T> filter) {
        return new Filter<>("(" + name + ") and (" + filter.name + ")", predicate.and(filter.predicate));
    }

    @NotNull
    public Filter<T> or(@NotNull final Filter<T> filter) {
        return new Filter<>("(" + name + ") or (" + filter.name + ")", predicate.or(filter.predicate));
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public Predicate<T> getPredicate() {
        return predicate;
    }

    @NotNull
    @Override
    public String toString() {
        return getName();
    }
}
