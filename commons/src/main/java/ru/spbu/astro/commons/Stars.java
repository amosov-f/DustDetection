package ru.spbu.astro.commons;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * User: amosov-f
 * Date: 28.03.15
 * Time: 19:00
 */
public enum Stars {
    ;

    public static final Star[] ALL = Catalogs.HIPNEWCAT.getStars();
    public static final Map<Integer, Star> MAP = map(ALL);

    public static final Star BARNARDS = MAP.get(87937);
    public static final Star KAPTEYNS = MAP.get(24186);

    @NotNull
    public static Map<Integer, Star> map(@NotNull final Star[] stars) {
        return Arrays.stream(stars).collect(Collectors.toMap(Star::getId, Function.identity()));
    }
}
