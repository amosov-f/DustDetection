package ru.spbu.astro.core;

import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * User: amosov-f
 * Date: 17.01.15
 * Time: 2:16
 */
public final class Catalogues {
    public static final Catalogue HIPPARCOS_1997 = Catalogue.read(Catalogue.class.getResourceAsStream("/catalogue/hipparcos1997.txt"));
    public static final Catalogue HIPPARCOS_2007 = innerJoin(Catalogue.read(Catalogue.class.getResourceAsStream("/catalogue/hipparcos2007.txt")), HIPPARCOS_1997);

    private Catalogues() {
    }

    @NotNull
    public static Catalogue innerJoin(@NotNull final Catalogue catalogue1, @NotNull final Catalogue catalogue2) {
        final Catalogue result = new Catalogue();
        for (final int id : Sets.intersection(catalogue1.id2row.keySet(), catalogue2.id2row.keySet())) {
            final Map<Catalogue.Parameter<?>, Object> values = new HashMap<>(catalogue2.id2row.get(id).values);
            values.putAll(catalogue1.id2row.get(id).values);
            result.add(new Catalogue.Row(id, values));
        }
        return result;
    }
}
