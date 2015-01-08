package ru.spbu.astro.core.spect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: amosov-f
 * Date: 07.01.15
 * Time: 1:17
 */
public class SpectTypeParser {
    private static final Map<String, SpectType> CACHE = new HashMap<>();

    private static final char STOP_SYMBOL = '$';

    @Nullable
    public static SpectType parse(@NotNull final String str) {
        if (!CACHE.containsKey(str)) {
            for (ExceptionSpectralType exceptionSpectralType : ExceptionSpectralType.values()) {
                if (str.startsWith(exceptionSpectralType.name())) {
                    return null;
                }
            }

            if (str.equals(str.toLowerCase())) {
                return null;
            }

            final List<Component> luminComponents = new ArrayList<>();
            final List<LuminosityClass> lumins = new ArrayList<>();
            if (str.startsWith("sd")) {
                luminComponents.add(new Component(LuminosityClass.VI));
                lumins.add(LuminosityClass.VI);
            }

            final String s = str.split(" ")[0]
                    .replaceAll("va", "")           //magic
                    .replaceAll("CN.*", "")          //magic
                    .replaceAll("\\+.*", "")          //magic
                    .replaceAll("\\.+$", "")           //magic
                    .replaceAll("[^OBAFGKM\\d\\.IVab/\\-:]", "")
                    + STOP_SYMBOL;


            final List<Component> spectComponents = new ArrayList<>();
            final List<SpectClass> spects = new ArrayList<>();
            SpectType.Relation spectsRelation = SpectType.Relation.OR;
            SpectType.Relation luminosityClassesRelation = SpectType.Relation.OR;
            Component cur = new Component();
            for (char c : s.toCharArray()) {
                if (cur.add(c)) {
                    continue;
                }

                if (cur.getType() == Component.Type.SPECT) {
                    if (!spectComponents.isEmpty()) {
                        cur.completeBy(spectComponents.get(spectComponents.size() - 1));
                    }
                    final SpectClass spect = SpectClass.parse(cur.value);
                    if (spect != null) {
                        spectComponents.add(cur);
                        spects.add(spect);
                    }
                    if (c == '-') {
                        spectsRelation = SpectType.Relation.INTERMEDIATE;
                    }
                }
                if (cur.getType() == Component.Type.LUMIN) {
                    if (!luminComponents.isEmpty()) {
                        cur.completeBy(luminComponents.get(luminComponents.size() - 1));
                    }
                    try {
                        luminComponents.add(cur);
                        lumins.add(LuminosityClass.valueOf(cur.value));
                    } catch (IllegalArgumentException ignored) {
                    }
                    if (c == '-') {
                        luminosityClassesRelation = SpectType.Relation.INTERMEDIATE;
                    }
                }

                cur = new Component();
                cur.add(c);
            }
            if (spectComponents.isEmpty()) {
                return null;
            }

            CACHE.put(str, new SpectType(spects, spectsRelation, lumins, luminosityClassesRelation));
        }
        return CACHE.get(str);
    }

    private static enum ExceptionSpectralType {
        R, S, N, C, DA, DB, DC, DD, DE, DF, DG, WR, WN, WC
    }

    private final static class Component {
        @NotNull
        private String value = "";

        public Component() {
        }

        public Component(final LuminosityClass luminosityClass) {
            this.value += luminosityClass.name();
        }

        private static boolean isTypeSymbol(char c) {
            return SpectClass.TypeSymbol.parse(c) != null || c == '.' || Character.isDigit(c);
        }

        private static boolean isComponentSymbol(char c) {
            return isTypeSymbol(c) || LuminosityClass.contains(c);
        }

        @Nullable
        public Type getType() {
            if (value.isEmpty()) {
                return null;
            }
            return getType(value.charAt(0));
        }

        public Type getType(char c) {
            if (isTypeSymbol(c)) {
                return Type.SPECT;
            }
            if (LuminosityClass.contains(c)) {
                return Type.LUMIN;
            }
            return null;
        }

        public boolean add(char c) {
            if (value.isEmpty()) {
                if (isComponentSymbol(c)) {
                    value += c;
                    return true;
                }
                return false;
            }

            if (getType() == getType(c)) {
                if (SpectClass.TypeSymbol.parse(c) != null) {
                    return false;
                }
                value += c;
                return true;
            }
            return c == ':';
        }

        public void completeBy(@NotNull final Component prev) {
            if (value.isEmpty() || prev.value.isEmpty()) {
                return;
            }
            if (getType() != prev.getType()) {
                return;
            }
            if (!Character.isUpperCase(value.charAt(0)) && Character.isUpperCase(prev.value.charAt(0))) {
                value = prev.value.charAt(0) + value;
            }
        }

        @NotNull
        @Override
        public String toString() {
            return value;
        }

        private enum Type {
            SPECT, LUMIN
        }
    }
}
