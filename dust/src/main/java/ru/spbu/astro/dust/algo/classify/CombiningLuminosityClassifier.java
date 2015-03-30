package ru.spbu.astro.dust.algo.classify;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.commons.spect.LuminosityClass;

/**
 * User: amosov-f
 * Date: 29.03.15
 * Time: 0:55
 */
public class CombiningLuminosityClassifier implements LuminosityClassifier {
    @NotNull
    private final LuminosityClassifier rightClassifier;

    public CombiningLuminosityClassifier(@NotNull final LuminosityClassifier rightClassifier) {
        this.rightClassifier = rightClassifier;
    }


    @NotNull
    @Override
    public LuminosityClass classify(@NotNull final Star star) {
        return star.getBVColor().getValue() < 0.6 ? LuminosityClass.III_V : rightClassifier.classify(star);
    }
}
