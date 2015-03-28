package ru.spbu.astro.util;

import org.jetbrains.annotations.NotNull;

import static java.lang.String.format;

/**
 * User: amosov-f
 * Date: 28.03.15
 * Time: 18:52
 */
public final class TextUtils {
    private static final double HUNDRED = 100.0;

    @NotNull
    public static String percents(final int num, final int denum) {
        return format("%.1f%%", HUNDRED * num / denum);
    }
    
    private TextUtils() {
    }
}
