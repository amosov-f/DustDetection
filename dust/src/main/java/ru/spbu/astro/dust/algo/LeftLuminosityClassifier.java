package ru.spbu.astro.dust.algo;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.commons.spect.LuminosityClass;

/**
 * User: amosov-f
 * Date: 28.03.15
 * Time: 0:26
 */
public abstract class LeftLuminosityClassifier implements LuminosityClassifier {
    public static class III extends LeftLuminosityClassifier {
        @NotNull
        @Override
        public LuminosityClass classify(@NotNull final Star star) {
            if (star.getBVColor().getValue() > 0.6) {
                throw new UnsupportedOperationException();
            }
            return LuminosityClass.III;
        }
    }

    public static class V extends LeftLuminosityClassifier {
        @NotNull
        @Override
        public LuminosityClass classify(@NotNull final Star star) {
            if (star.getBVColor().getValue() > 0.6) {
                throw new UnsupportedOperationException();
            }
            return LuminosityClass.V;
        }
    }
}
