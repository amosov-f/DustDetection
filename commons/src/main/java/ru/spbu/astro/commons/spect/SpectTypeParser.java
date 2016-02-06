package ru.spbu.astro.commons.spect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.commons.Stars;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: amosov-f
 * Date: 07.01.15
 * Time: 1:17
 */
public enum SpectTypeParser {
    INSTANCE;

    private static final Map<String, SpectType> CACHE = new HashMap<>();

    @Nullable
    public SpectType parse(@NotNull final String str) {
        if (CACHE.containsKey(str)) {
            return CACHE.get(str);
        }

        if (str.contains("+")) {
            return process(str, null);
        }

        final List<TempClass> temps = new ArrayList<>();
        final List<LuminosityClass> lumins = new ArrayList<>();
        SpectType.Relation tempsRelation = SpectType.Relation.INTERMEDIATE;
        SpectType.Relation luminRelation = SpectType.Relation.INTERMEDIATE;

        boolean lastTemp = true;
        String s = str;
        while (!s.isEmpty()) {
            final TempClass temp = nextTemp(s);
            if (temp != null) {
                s = s.substring(temp.toString().length());
                temps.add(temp);
                lastTemp = true;
                continue;
            }
            final LuminosityClass lumin = nextLumin(s);
            if (lumin != null) {
                s = s.substring(lumin.name().length());
                lumins.add(lumin);
                lastTemp = false;
                continue;
            }
            final SpectType.Relation relation = nextRelation(s);
            if (relation != null) {
                s = s.substring(1);
                if (lastTemp) {
                    tempsRelation = relation;
                } else {
                    luminRelation = relation;
                }
                continue;
            }
            s = s.substring(1);
        }
        if (temps.isEmpty()) {
            return null;
        }
        return process(str, new SpectType(temps, tempsRelation, lumins, luminRelation));
    }

    @Nullable
    private static SpectType process(@NotNull final String str, @Nullable final SpectType type) {
        CACHE.put(str, type);
        return type;
    }

    @Nullable
    private static TempClass nextTemp(@NotNull final String str) {
        for (int i = str.length(); i > 0; i--) {
            final TempClass temp = TempClass.parse(str.substring(0, i));
            if (temp != null) {
                return temp;
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
        Stars.ALL.getClass();
        for (final String key : CACHE.keySet()) {
            System.out.println(key + " -> " + CACHE.get(key));
        }
    }
}
