package ru.spbu.astro.dust.algo.classify;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.commons.StarFilter;
import ru.spbu.astro.commons.Stars;
import ru.spbu.astro.commons.spect.LuminosityClass;

import static ru.spbu.astro.commons.graph.HRDiagram.SCALE;

/**
 * User: amosov-f
 * Date: 28.03.15
 * Time: 18:57
 */
public final class LuminosityClassifiers {
    private static final double BV_COLOR_ERROR_LIMIT = 0.01;

    public static final LuminosityClassifier SVM = createSVM(StarFilter.of(Stars.ALL)
            .mainLumin()
            .absMagErr(SCALE * BV_COLOR_ERROR_LIMIT)
            .bvErr(BV_COLOR_ERROR_LIMIT).stars());
    public static final LuminosityClassifier LEFT_III = new ConstLuminosityClassifier.Left(LuminosityClass.III);
    public static final LuminosityClassifier LEFT_V = new ConstLuminosityClassifier.Left(LuminosityClass.V);
    public static final LuminosityClassifier COMBINING = new CombiningLuminosityClassifier(SVM);

    @NotNull
    public static LuminosityClassifier createSVM(@NotNull final Star[] stars) {
        return new SVMLuminosityClassifier(stars);
    }
    
    private LuminosityClassifiers() {
    }
}
