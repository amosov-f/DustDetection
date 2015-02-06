package ru.spbu.astro.commons.spect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.commons.Catalogues;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: amosov-f
 * Date: 07.01.15
 * Time: 1:17
 */
public final class SpectTypeParser {
    private static final Map<String, SpectType> CACHE = new HashMap<>();

    private SpectTypeParser() {
    }

    @Nullable
    public static SpectType parse(@NotNull final String str) {
        if (CACHE.containsKey(str)) {
            return CACHE.get(str);
        }

        if (str.contains("+")) {
            return process(str, null);
        }

        final List<SpectClass> spects = new ArrayList<>();
        final List<LuminosityClass> lumins = new ArrayList<>();
        SpectType.Relation spectsRelation = SpectType.Relation.INTERMEDIATE;
        SpectType.Relation luminRelation = SpectType.Relation.INTERMEDIATE;

        boolean lastSpect = true;
        String s = str;
        while (!s.isEmpty()) {
            final SpectClass spect = nextSpect(s);
            if (spect != null) {
                s = s.substring(spect.toString().length());
                spects.add(spect);
                lastSpect = true;
                continue;
            }
            final LuminosityClass lumin = nextLumin(s);
            if (lumin != null) {
                s = s.substring(lumin.name().length());
                lumins.add(lumin);
                lastSpect = false;
                continue;
            }
            final SpectType.Relation relation = nextRelation(s);
            if (relation != null) {
                s = s.substring(1);
                if (lastSpect) {
                    spectsRelation = relation;
                } else {
                    luminRelation = relation;
                }
                continue;
            }
            s = s.substring(1);
        }
        if (spects.isEmpty()) {
            return null;
        }
        return process(str, new SpectType(spects, spectsRelation, lumins, luminRelation));
    }

    @Nullable
    private static SpectType process(@NotNull final String str, @Nullable final SpectType type) {
        CACHE.put(str, type);
        return type;
    }

    @Nullable
    private static SpectClass nextSpect(@NotNull final String str) {
        for (int i = str.length(); i > 0; i--) {
            final SpectClass spect = SpectClass.parse(str.substring(0, i));
            if (spect != null) {
                return spect;
            }
        }
        return null;
    }

    @Nullable
    private static LuminosityClass nextLumin(@NotNull final String str) {
        for (int i = str.length(); i > 0; i--) {
            try {
                return LuminosityClass.valueOf(str.substring(0, i));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }

    @Nullable
    private static SpectType.Relation nextRelation(@NotNull final String str) {
        if (str.isEmpty()) {
            return null;
        }
        try {
            return SpectType.Relation.parse(str.charAt(0));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public static void main(@NotNull final String[] args) {
        Catalogues.HIPPARCOS_2007.getStars();
        for (final String key : CACHE.keySet()) {
            System.out.println(key + " -> " + CACHE.get(key));
        }
    }
}
