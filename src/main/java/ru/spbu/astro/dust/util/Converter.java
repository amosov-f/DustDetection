package ru.spbu.astro.dust.util;

/**
 * User: amosov-f
 * Date: 27.09.14
 * Time: 21:53
 */
public final class Converter {
    public static double deg2rad(final double deg) {
        return deg / 180 * Math.PI;
    }

    public static double rad2deg(final double rad) {
        return rad / Math.PI * 180;
    }
}
