package ru.spbu.astro.util;

import static java.lang.Math.abs;
import static java.lang.Math.max;

/**
 * User: amosov-f
 * Date: 07.02.15
 * Time: 3:12
 */
public final class MathTools {
    private MathTools() {
    }

    public static double interpolate(final double x1, final double y1, final double x2, final double y2, final double x) {
        final double dx = x2 - x1;
        final double dy = y2 - y1;
        return dy / dx * (x - x1) + y1;
    }

    public static double normalize(final double x, final double min, final double max) {
        double normalized = x;
        if (normalized > max) {
            normalized = max;
        }
        if (normalized < min) {
            normalized = min;
        }

        final double d = max(abs(min), abs(max));
        return d == 0 ? 0 : normalized / d;
    }
}
