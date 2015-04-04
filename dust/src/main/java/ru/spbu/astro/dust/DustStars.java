package ru.spbu.astro.dust;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.commons.StarFilter;
import ru.spbu.astro.commons.Stars;
import ru.spbu.astro.dust.algo.classify.LuminosityClassifier;
import ru.spbu.astro.dust.algo.classify.LuminosityClassifiers;

/**
 * User: amosov-f
 * Date: 28.03.15
 * Time: 16:16
 */
public final class DustStars {
    public static final Star[] ALL = classified(Stars.ALL, LuminosityClassifiers.SVM);

    public static Star[] classified(@NotNull final Star[] stars, @NotNull final LuminosityClassifier classifier) {
        return StarFilter.of(classifier.classify(stars)).hasExt().stars();
    }
    
    private DustStars() {
    }
}
