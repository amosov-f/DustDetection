package ru.spbu.astro.commons;

import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * User: amosov-f
 * Date: 17.01.15
 * Time: 2:16
 */
public enum Catalogs {
    ;

    static final Catalog XHIP = Catalog.read("xhip", Catalog.class.getResourceAsStream("/catalog/xhip.txt"));

    @NotNull
    static Catalog innerJoin(@NotNull final String name,
                             @NotNull final Catalog catalog1,
                             @NotNull final Catalog catalog2)
    {
        final Catalog result = new Catalog(name);
        for (final int id : Sets.intersection(catalog1.id2row.keySet(), catalog2.id2row.keySet())) {
            final Map<Parameter<?>, Object> values = new HashMap<>(catalog2.id2row.get(id).values);
            values.putAll(catalog1.id2row.get(id).values);
            result.add(new Catalog.Row(id, values));
        }
        return result;
    }
}
