package ru.spbu.astro.dust.util;

import static java.lang.Math.round;
import static java.lang.Math.sqrt;

/**
 * User: amosov-f
 * Date: 28.09.14
 * Time: 0:36
 */
public final class HealpixTools {
    public static int nPix(final int nSide) {
        return 12 * nSide * nSide;
    }

    public static int nSide(final int nPix) {
        return (int) round(sqrt(nPix / 12.0));
    }
}
