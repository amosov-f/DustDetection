package ru.spbu.astro.commons;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * User: amosov-f
 * Date: 28.03.15
 * Time: 19:00
 */
public final class Stars {
    public static final Star[] ALL = Catalogs.HIPPARCOS_2007.getStars();

    @NotNull
    public static Map<Integer, Star> map(@NotNull final Star[] stars) {
        return new HashMap<Integer, Star>() {{
            Arrays.stream(stars).forEach(star -> put(star.getId(), star));
        }};
    }

    private Stars() {
    }
}
