package ru.spbu.astro.dust;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.commons.Catalog;
import ru.spbu.astro.commons.Catalogs;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.dust.algo.LuminosityClassifier;

import java.util.function.Function;

/**
 * User: amosov-f
 * Date: 11.01.15
 * Time: 19:03
 */
public final class DustCatalogs {
    public static final Catalog HIPPARCOS_UPDATED = Catalogs.update(Catalogs.HIPPARCOS_2007, new Function<Star, Star>() {
        private final LuminosityClassifier classifier = new LuminosityClassifier(Catalogs.HIPPARCOS_2007.getStars());

        @NotNull
        @Override
        public Star apply(@NotNull final Star star) {
            if (star.getSpectType().hasLumin()) {
                return star;
            }
            return new Star.Builder(star).setSpectType(star.getSpectType().setLumin(classifier.classify(star))).build();
        }
    });

    private DustCatalogs() {
    }
}
