package ru.spbu.astro.commons;

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
    public static final Map<Integer, Star> MAP_ALL = new HashMap<Integer, Star>() {{
        Arrays.stream(ALL).forEach(star -> put(star.getId(), star));
    }};

    private Stars() {
    }
}
