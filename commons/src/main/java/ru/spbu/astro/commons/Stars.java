package ru.spbu.astro.commons;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * User: amosov-f
 * Date: 28.03.15
 * Time: 19:00
 */
public final class Stars {
    public static final Star[] ALL = Catalogs.HIPPARCOS_2007.getStars();
    public static final Star BARNARDS = Objects.requireNonNull(Catalogs.HIPPARCOS_2007.get(87937));

    @NotNull
    public static Map<Integer, Star> map(@NotNull final Star[] stars) {
        return Arrays.stream(stars).collect(Collectors.toMap(Star::getId, Function.identity()));
    }

    private Stars() {
    }
}
