package ru.spbu.astro.util;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * User: amosov-f
 * Date: 28.03.15
 * Time: 18:52
 */
public final class TextUtils {
    private static final double HUNDRED = 100.0;

    @NotNull
    public static String percents(final int num, final int denum) {
        return String.format("%.1f%%", HUNDRED * num / denum);
    }

    public static double removeUnnecessaryDigits(final double x) {
        return Double.valueOf(String.format(Locale.US, "%.2f", x));
    }

    @NotNull
    public static String format(@NotNull final String format, final double x) {
        return String.valueOf(Double.parseDouble(String.format(Locale.US, format, x)));
    }

    private TextUtils() {
    }
}
