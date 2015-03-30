package ru.spbu.astro.dust.algo.classify;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.commons.Stars;
import ru.spbu.astro.commons.spect.LuminosityClass;

/**
 * User: amosov-f
 * Date: 28.03.15
 * Time: 18:57
 */
public final class LuminosityClassifiers {
    public static final LuminosityClassifier SVM = createSVM(Stars.ALL);
    public static final LuminosityClassifier LEFT_III = new ConstLuminosityClassifier.Left(LuminosityClass.III);
    public static final LuminosityClassifier LEFT_V = new ConstLuminosityClassifier.Left(LuminosityClass.V);
    public static final LuminosityClassifier COMBINING = new CombiningLuminosityClassifier(SVM);
    
    public static LuminosityClassifier createSVM(@NotNull final Star[] stars) {
        return new SVMLuminosityClassifier(stars);
    }
    
    private LuminosityClassifiers() {
    }
}
