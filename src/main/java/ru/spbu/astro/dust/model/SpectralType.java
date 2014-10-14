package ru.spbu.astro.dust.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.geom.Point2D;
import java.util.*;

public final class SpectralType {
    private static final Map<String, SpectralType> cache = new HashMap<>();

    public static enum LuminosityClass {
        I, Ia, Ib, Iab, II, IIb, III, IIIa, IIIb, IV, IVa, V, Va, Vb, VI, VII;

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
        private static TypeSymbol parse(final char c) {
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
                final Double bv = toBV(type.value, LuminosityClass.valueOf(luminosityClass.value));
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

    private static final EnumMap<TypeSymbol, Integer> START = new EnumMap<TypeSymbol, Integer>(TypeSymbol.class) {{
        /*.put("O", -10);
        put("B", 0);
        put("A", 10);
        put("F", 20);
        put("G", 30);
        put("K", 40);
        put("M", 48);*/
        put(TypeSymbol.O, 0);
        put(TypeSymbol.B, 10);
        put(TypeSymbol.A, 20);
        put(TypeSymbol.F, 30);
        put(TypeSymbol.G, 40);
        put(TypeSymbol.K, 50);
        put(TypeSymbol.M, 60);
    }};

    @Nullable
    private static Double toBV(@NotNull final String type, @NotNull final LuminosityClass luminosityClass) {
        if (!LUMIN_2_BVS.containsKey(luminosityClass)) {
            return null;
        }
        final List<Point2D.Double> bvs = LUMIN_2_BVS.get(luminosityClass);

        if (type.length() < 2) {
            return null;
        }
        final double code = START.get(TypeSymbol.parse(type.charAt(0))) + Double.valueOf(type.substring(1, type.length()));
        for (int i = 0; i < bvs.size() - 1; ++i) {
            if (bvs.get(i).x <= code && code < bvs.get(i + 1).x) {
                return interpolate(bvs.get(i), bvs.get(i + 1), code);
            }
        }

        return null;
    }

    @Nullable
    public LuminosityClass getLuminosityClass() {
        if (luminosityClasses.isEmpty()) {
            return null;
        }
        return LuminosityClass.valueOf(luminosityClasses.get(0).value);
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

    private static final EnumMap<LuminosityClass, List<Point2D.Double>> LUMIN_2_BVS = new EnumMap<>(LuminosityClass.class);
    static {
        final Scanner fin = new Scanner(SpectralType.class.getResourceAsStream("/spect2bv/spect2bv_new.txt"));

        final String[] titles = fin.nextLine().trim().split("\\s+");
        for (int i = 2; i < titles.length; ++i) {
            LUMIN_2_BVS.put(LuminosityClass.valueOf(titles[i]), new ArrayList<>());
        }

        while (fin.hasNextLine()) {
            final String[] fields = fin.nextLine().trim().split("\t");
            final double code = Double.valueOf(fields[1]);
            for (int i = 2; i < titles.length; ++i) {
                if (!fields[i].equals("-")) {
                    double bv = Double.valueOf(fields[i]);
                    LUMIN_2_BVS.get(LuminosityClass.valueOf(titles[i])).add(new Point2D.Double(code, bv));
                }
            }
        }
        System.out.println("spect2bv loaded");
    }

    private static double interpolate(@NotNull final Point2D.Double p1, @NotNull final Point2D.Double p2, final double x) {
        return (p2.y - p1.y) / (p2.x - p1.x) * (x - p1.x) + p1.y;
    }
}