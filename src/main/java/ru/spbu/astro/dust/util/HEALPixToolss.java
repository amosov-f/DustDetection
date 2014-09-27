package ru.spbu.astro.dust.util;

import static java.lang.Math.round;
import static java.lang.Math.sqrt;

/**
 * User: amosov-f
 * Date: 28.09.14
 * Time: 0:36
 */
public final class HEALPixToolss {
    public static int pixNumber(final int sideNumber) {
        return 12 * sideNumber * sideNumber;
    }

    public static int sideNumber(final int pixNumber) {
        return (int) round(sqrt(pixNumber / 12.0));
    }
}
