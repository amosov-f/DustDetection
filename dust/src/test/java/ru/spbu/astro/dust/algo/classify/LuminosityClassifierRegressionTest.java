package ru.spbu.astro.dust.algo.classify;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.commons.StarFilter;
import ru.spbu.astro.commons.Stars;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

/**
 * User: amosov-f
 * Date: 03.05.15
 * Time: 0:55
 */
public final class LuminosityClassifierRegressionTest {
    private LuminosityClassifierRegressionTest() {
    }

    public static void main(@NotNull final String[] args) throws IOException {
        final Star[] stars = LuminosityClassifiers.SVM.classify(StarFilter.of(Stars.ALL).noLumin().stars());
        FileUtils.writeStringToFile(new File("dust/src/test/resources/predict-lumin.txt"), toString(stars));
    }

    @NotNull
    private static String toString(@NotNull final Star[] stars) {
        final StringBuilder sb = new StringBuilder();
        sb.append("HIP\tl\t\tb\t\tV\t\tSp\tLumin\n");
        for (final Star star : stars) {
            sb.append(star.getId()).append("\t")
                    .append(String.format(Locale.US, "%.2f\t%.2f\t", Math.toDegrees(star.getDir().getL()), Math.toDegrees(star.getDir().getB())))
                    .append(star.getVMag()).append("\t")
                    .append(star.getSpectType().getSpect()).append("\t")
                    .append(star.getSpectType().getLumin()).append("\n");
        }
        return sb.toString();
    }
}
