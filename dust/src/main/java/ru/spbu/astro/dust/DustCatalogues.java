package ru.spbu.astro.dust;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.commons.Catalogue;
import ru.spbu.astro.commons.Catalogues;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.dust.algo.LuminosityClassifier;

import java.util.function.Function;

/**
 * User: amosov-f
 * Date: 11.01.15
 * Time: 19:03
 */
public final class DustCatalogues {
    public static final Catalogue HIPPARCOS_UPDATED = Catalogues.HIPPARCOS_2007.updateBy(new Function<Star, Star>() {
        private final LuminosityClassifier classifier = new LuminosityClassifier(Catalogues.HIPPARCOS_2007.getStars());

        @Nullable
        @Override
        public Star apply(@NotNull final Star star) {
            if (star.getSpectType().hasLumin()) {
                return star;
            }
            return star.setSpectType(star.getSpectType().setLumin(classifier.classify(star)));
        }
    });

    private DustCatalogues() {
    }
}
