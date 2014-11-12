package ru.spbu.astro.dust.model.spect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.dust.model.Value;
import ru.spbu.astro.dust.model.spect.table.SpectTable;

import java.util.*;

public final class SpectType {
    private static final Map<String, SpectType> cache = new HashMap<>();

    private static enum Relation {
        OR, INTERMEDIATE
    }

    private final List<Component> spects;
    private Relation spectsRelation;

    private final List<Component> lumins;
    private Relation luminosRelation = Relation.OR;

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
                if (SpectClass.TypeSymbol.parse(c) != null) {
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
            return SpectClass.TypeSymbol.parse(c) != null || c == '.' || Character.isDigit(c);
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

    private static SpectTable spectTable = SpectTable.TSVETKOV;

    @Nullable
    public static SpectType parse(@NotNull final String str) {
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

            cache.put(str, new SpectType(types, typesRelation, luminosityClasses, luminosityClassesRelation));
        }
        return cache.get(str);
    }

    private SpectType(@NotNull final List<Component> spects, @NotNull final Relation spectsRelation,
                      @NotNull final List<Component> lumins, @NotNull final Relation luminosRelation) {
        this.spects = spects;
        this.spectsRelation = spectsRelation;
        this.lumins = lumins;
        this.luminosRelation = luminosRelation;
    }

    @Override
    public String toString() {
        String s = "";
        for (int i = 0; i < spects.size(); ++i) {
            s += spects.get(i).value;
            if (spects.get(i).doubt) {
                s += ":";
            }
            if (i < spects.size() - 1) {
                switch (spectsRelation) {
                    case INTERMEDIATE:
                        s += "-";
                        break;
                    case OR:
                        s += "/";
                        break;
                }
            }
        }
        for (int i = 0; i < lumins.size(); ++i) {
            s += lumins.get(i).value;
            if (lumins.get(i).doubt) {
                s += ":";
            }
            if (i < lumins.size() - 1) {
                switch (luminosRelation) {
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
        for (final Component spectComponent : spects) {
            for (final Component luminComponent : lumins) {
                final SpectClass spect = SpectClass.valueOf(spectComponent.value);
                final LuminosityClass lumin = LuminosityClass.valueOf(luminComponent.value);
                System.out.println(spect);
                System.out.println(lumin);
                if (spect != null && lumin != null) {
                    final Double bv = spectTable.getBV(spect, lumin);
                    if (bv != null) {
                        bvs.add(bv);
                    }
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
        if (lumins.isEmpty()) {
            return null;
        }
        return LuminosityClass.valueOf(lumins.get(0).value);
    }

    @NotNull
    public SpectClass getSpect() {
        return SpectClass.valueOf(spects.get(0).value);
    }

    @NotNull
    public String getIntSpect() {
        return getTypeSymbol() + "" + (int) getTypeNumber();
    }

    public double getTypeNumber() {
        if (spects.get(0).value.length() < 2) {
            return 5;
        }
        return Double.valueOf(spects.get(0).value.substring(1));
    }

    public SpectClass.TypeSymbol getTypeSymbol() {
        return SpectClass.TypeSymbol.parse(spects.get(0).value.charAt(0));
    }
}