package ru.spbu.astro.dust.algo.classify;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.commons.spect.LuminosityClass;

/**
 * User: amosov-f
 * Date: 28.03.15
 * Time: 0:26
 */
public class ConstLuminosityClassifier implements LuminosityClassifier {
    @NotNull
    private final LuminosityClass lumin;

    public ConstLuminosityClassifier(@NotNull final LuminosityClass lumin) {
        this.lumin = lumin;
    }

    @NotNull
    @Override
    public LuminosityClass classify(@NotNull final Star star) {
        return lumin;
    }
    
    public static final class Left extends ConstLuminosityClassifier {
        public static final double DELIMETER = 0.6;

        public Left(@NotNull final LuminosityClass lumin) {
            super(lumin);
        }

        @NotNull
        @Override
        public LuminosityClass classify(@NotNull final Star star) {
            if (star.getBVColor().val() > DELIMETER) {
                throw new UnsupportedOperationException();
            }
            return super.classify(star);
        }
    }
}
