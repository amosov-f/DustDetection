package ru.spbu.astro.util;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * User: amosov-f
 * Date: 28.03.15
 * Time: 18:52
 */
public final class TextUtils {
    private static final double HUNDRED = 100.0;

    private TextUtils() {
    }

    @NotNull
    public static String percents(final int num, final int denum) {
        return percents((double) num / denum);
    }

    @NotNull
    public static String percents(final double share) {
        return String.format("%.1f%%", HUNDRED * share);
    }

    public static double removeUnnecessaryDigits(final double x) {
        return Double.valueOf(String.format(Locale.US, "%.2f", x));
    }

    @NotNull
    public static String format(@NotNull final String format, final double x) {
        return String.valueOf(Double.parseDouble(String.format(Locale.US, format, x)));
    }

    @NotNull
    public static String tex(@NotNull final Object[][] table, final int col, final int nest) {
        final StringBuilder sb = new StringBuilder();
        sb.append(tab(nest)).append("\\begin{longtable}{").append(StringUtils.repeat("| l ", table[0].length * col)).append("} \\centering\n");
        sb.append(tab(nest + 1)).append("\\caption {} \\label{}\n");
        sb.append(tab(nest + 1)).append("\\hline\n");
        final int n = table.length % col == 0 ? table.length / col : table.length / (col - 1);
        for (int i = 0; i < n; i++) {
            sb.append(tab(nest + 1));
            for (int j = 0; j < col; j++) {
                final int rowIndex = i + j * n;
                if (rowIndex < table.length) {
                    final Object[] row = table[rowIndex];
                    sb.append(Joiner.on("    &    ").join(row));
                    if (rowIndex + n < table.length) {
                        sb.append("    &    ");
                    }
                }
            }
            sb.append("    \\\\\n");
        }
        sb.append(tab(nest + 1)).append("\\hline\n");
        sb.append(tab(nest)).append("\\end{table}\n");
        return sb.toString();
    }

    @NotNull
    public static String tab(final int nest) {
        return StringUtils.repeat(' ', 4 * nest);
    }
}
