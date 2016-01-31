package ru.spbu.astro.util;

import org.jetbrains.annotations.NotNull;

import java.util.function.ToDoubleFunction;

/**
 * User: amosov-f
 * Date: 31.01.16
 * Time: 20:49
 */
public enum MoreFunctions {
    ;

    @NotNull
    public static ToDoubleFunction<Double> identity() {
        return d -> d;
    }
}
