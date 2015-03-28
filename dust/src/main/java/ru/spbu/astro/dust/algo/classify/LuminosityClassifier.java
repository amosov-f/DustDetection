package ru.spbu.astro.dust.algo.classify;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.commons.spect.LuminosityClass;

import java.util.Arrays;

/**
 * User: amosov-f
 * Date: 28.03.15
 * Time: 0:24
 */
public interface LuminosityClassifier {
    @NotNull
    LuminosityClass classify(@NotNull Star star);

    @NotNull
    default Star[] classify(@NotNull final Star[] stars) {
        return Arrays.stream(stars).map(star -> {
            if (star.getSpectType().hasLumin()) {
                return star;
            }
            return new Star.Builder(star).setSpectType(star.getSpectType().setLumin(classify(star))).build();
        }).toArray(Star[]::new);
    }
}
