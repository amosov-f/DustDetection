package ru.spbu.astro.dust.model;

import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.core.Star;
import ru.spbu.astro.core.spect.LuminosityClass;
import ru.spbu.astro.dust.algo.LuminosityClassifier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static ru.spbu.astro.dust.model.Catalogue.Parameter.SPECT_TYPE;

/**
 * User: amosov-f
 * Date: 11.01.15
 * Time: 19:03
 */
public class Catalogues {
    public static final Catalogue HIPPARCOS_1997 = Catalogue.read(Catalogue.class.getResourceAsStream("/catalogues/hipparcos1997.txt"));
    public static final Catalogue HIPPARCOS_2007 = innerJoin(Catalogue.read(Catalogue.class.getResourceAsStream("/catalogues/hipparcos2007.txt")), HIPPARCOS_1997);
    public static final Catalogue HIPPARCOS_UPDATED = HIPPARCOS_2007.updateBy(new Function<Catalogue.Row, Catalogue.Row>() {
        private final LuminosityClassifier classifier = new LuminosityClassifier(HIPPARCOS_2007.getStars());

        @Nullable
        @Override
        public Catalogue.Row apply(@NotNull final Catalogue.Row row) {
            final Star star = row.toStar();
            if (star == null) {
                return null;
            }

            final Catalogue.Row updatedRow = new Catalogue.Row(row);

            final LuminosityClass lumin = star.getSpectType().getLumin();
            if (lumin == null) {
                star.getSpectType().setLumin(classifier.classify(star));
                updatedRow.values.put(SPECT_TYPE, star.getSpectType());
            }

            return updatedRow;
        }
    });

    @NotNull
    public static Catalogue innerJoin(@NotNull final Catalogue catalogue1, @NotNull final Catalogue catalogue2) {
        final Catalogue result = new Catalogue();
        for (final int id : Sets.intersection(catalogue1.id2row.keySet(), catalogue2.id2row.keySet())) {
            final Map<Catalogue.Parameter, Object> values = new HashMap<>(catalogue2.id2row.get(id).values);
            values.putAll(catalogue1.id2row.get(id).values);
            result.add(new Catalogue.Row(id, values));
        }
        return result;
    }
}
