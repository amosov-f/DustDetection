package ru.spbu.astro.dust.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.dust.model.table.SpectTable;

import java.util.*;

public final class SpectralType {
    private static final Map<String, SpectralType> cache = new HashMap<>();

    public static enum LuminosityClass {
        I, Ia, Ib, Iab, II, IIb, III, IIIa, IIIb, IV, IVa, V, Va, Vb, VI, VII;

        public static final List<LuminosityClass> USED = Arrays.asList(V);

        public static boolean containsSymbol(final char c) {
            for (final LuminosityClass luminosityClass : LuminosityClass.values()) {
                if (luminosityClass.name().contains(String.valueOf(c))) {
                    return true;
                }
            }
            return false;
        }
    }

    public static enum TypeSymbol {
        O, B, A, F, G, K, M;

        @Nullable
        public static TypeSymbol parse(final char c) {
            try {
                return valueOf(String.valueOf(c));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    private static enum Relation {
        OR, INTERMEDIATE
    }

    private final List<Component> types;
    private Relation typesRelation;

    private final List<Component> luminosityClasses;
    private Relation luminosityClassesRelation = Relation.OR;

    private static final char STOP_SYMBOL = '$';

    private final static class Component {
        private String value = "";
        private boolean doubt = false;

        private enum Type {
            TYPE, LUMINOSITY_CLASS
        }

        public Component() {
        }

        public Component(final LuminosityClass luminosityClass) {
            this.value += luminosityClass.name();
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
                return Type.TYPE;
            }
            if (LuminosityClass.containsSymbol(c)) {
                return Type.LUMINOSITY_CLASS;
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
                if (TypeSymbol.parse(c) != null) {
                    return false;
                }
                value += c;
                return true;
            }
            if (c == ':') {
                doubt = true;
                return true;
            }
            return false;
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

        private static boolean isTypeSymbol(char c) {
            return TypeSymbol.parse(c) != null || c == '.' || Character.isDigit(c);
        }

        private static boolean isComponentSymbol(char c) {
            return isTypeSymbol(c) || LuminosityClass.containsSymbol(c);
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private static enum ExceptionSpectralType {
        R, S, N, C, DA, DB, DC, DD, DE, DF, DG, WR, WN, WC
    }

    private static SpectTable spectTable = SpectTable.COMBINED;

    @Nullable
    public static SpectralType parse(@NotNull final String str) {
        if (!cache.containsKey(str)) {
            for (ExceptionSpectralType exceptionSpectralType : ExceptionSpectralType.values()) {
                if (str.startsWith(exceptionSpectralType.name())) {
                    return null;
                }
            }

            if (str.equals(str.toLowerCase())) {
                return null;
            }

            final List<Component> luminosityClasses = new ArrayList<>();
            if (str.startsWith("sd")) {
                luminosityClasses.add(new Component(LuminosityClass.VI));
            }

            final String s = str.split(" ")[0]
                    .replaceAll("va", "")           //magic
                    .replaceAll("CN.*", "")          //magic
                    .replaceAll("\\+.*", "")          //magic
                    .replaceAll("\\.+$", "")           //magic
                    .replaceAll("[^OBAFGKM\\d\\.IVab/\\-:]", "")
                    + STOP_SYMBOL;


            final List<Component> types = new ArrayList<>();
            Relation typesRelation = Relation.OR;
            Relation luminosityClassesRelation = Relation.OR;
            Component cur = new Component();
            for (char c : s.toCharArray()) {
                if (cur.add(c)) {
                    continue;
                }

                if (cur.getType() == Component.Type.TYPE) {
                    if (!types.isEmpty()) {
                        cur.completeBy(types.get(types.size() - 1));
                    }
                    types.add(cur);
                    if (c == '-') {
                        typesRelation = Relation.INTERMEDIATE;
                    }
                }
                if (cur.getType() == Component.Type.LUMINOSITY_CLASS) {
                    if (!luminosityClasses.isEmpty()) {
                        cur.completeBy(luminosityClasses.get(luminosityClasses.size() - 1));
                    }
                    try {
                        LuminosityClass.valueOf(cur.value);
                        luminosityClasses.add(cur);
                    } catch (IllegalArgumentException ignored) {
                    }
                    if (c == '-') {
                        luminosityClassesRelation = Relation.INTERMEDIATE;
                    }
                }

                cur = new Component();
                cur.add(c);
            }

            cache.put(str, new SpectralType(types, typesRelation, luminosityClasses, luminosityClassesRelation));
        }
        return cache.get(str);
    }

    private SpectralType(@NotNull final List<Component> types, @NotNull final Relation typesRelation,
                         @NotNull final List<Component> luminosityClasses, @NotNull final Relation luminosityClassesRelation)
    {
        this.types = types;
        this.typesRelation = typesRelation;
        this.luminosityClasses = luminosityClasses;
        this.luminosityClassesRelation = luminosityClassesRelation;
    }

    @Override
    public String toString() {
        String s = "";
        for (int i = 0; i < types.size(); ++i) {
            s += types.get(i).value;
            if (types.get(i).doubt) {
                s += ":";
            }
            if (i < types.size() - 1) {
                switch (typesRelation) {
                    case INTERMEDIATE:
                        s += "-";
                        break;
                    case OR:
                        s += "/";
                        break;
                }
            }
        }
        for (int i = 0; i < luminosityClasses.size(); ++i) {
            s += luminosityClasses.get(i).value;
            if (luminosityClasses.get(i).doubt) {
                s += ":";
            }
            if (i < luminosityClasses.size() - 1) {
                switch (luminosityClassesRelation) {
                    case INTERMEDIATE:
                        s += "-";
                        break;
                    case OR:
                        s += "/";
                        break;
                }
            }
        }
        return s;
    }

    @Nullable
    public Value toBV() {
        final List<Double> bvs = new ArrayList<>();
        for (final Component type : types) {
            for (final Component luminosityClass : luminosityClasses) {
                final Double bv = spectTable.getBV(type.value, LuminosityClass.valueOf(luminosityClass.value));
                if (bv != null) {
                    bvs.add(bv);
                }
            }
        }

        if (bvs.isEmpty()) {
            return null;
        }

        double bv = 0.0;
        for (final double bvEntry : bvs) {
            bv += bvEntry;
        }
        bv /= bvs.size();

        return new Value(bv, Collections.max(bvs) - bv);
    }

    @Nullable
    public LuminosityClass getLumin() {
        if (luminosityClasses.isEmpty()) {
            return null;
        }
        return LuminosityClass.valueOf(luminosityClasses.get(0).value);
    }

    @NotNull
    public String getSpect() {
        return (getTypeSymbol() + "" + getTypeNumber()).replaceAll("\\.0", "");
    }

    @NotNull
    public String getIntSpect() {
        return getTypeSymbol() + "" + (int) getTypeNumber();
    }

    public double getTypeNumber() {
        if (types.get(0).value.length() < 2) {
            return 5;
        }
        return Double.valueOf(types.get(0).value.substring(1));
    }

    public TypeSymbol getTypeSymbol() {
        return TypeSymbol.parse(types.get(0).value.charAt(0));
    }
}