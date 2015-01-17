package ru.spbu.astro.dust;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.core.Catalogue;
import ru.spbu.astro.core.Catalogues;
import ru.spbu.astro.core.Star;
import ru.spbu.astro.core.spect.LuminosityClass;
import ru.spbu.astro.dust.algo.LuminosityClassifier;

import java.util.function.Function;

/**
 * User: amosov-f
 * Date: 11.01.15
 * Time: 19:03
 */
public class DustCatalogues {
    public static final Catalogue HIPPARCOS_UPDATED = Catalogues.HIPPARCOS_2007.updateBy(new Function<Star, Star>() {
        private final LuminosityClassifier classifier = new LuminosityClassifier(Catalogues.HIPPARCOS_2007.getStars());

        @Nullable
        @Override
        public Star apply(@NotNull final Star star) {
            final LuminosityClass lumin = star.getSpectType().getLumin();
            if (lumin == null) {
                star.getSpectType().setLumin(classifier.classify(star));
            }
            return star;
        }
    });

}
