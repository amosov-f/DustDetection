package ru.spbu.astro.dust.model;

import java.awt.geom.Point2D;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

public class SpectralType {

    public static final List<String> parseLuminosityClasses = Arrays.asList(
            "I", "Ia", "Ib", "Iab", "II", "IIb", "III", "IIIa", "IIIb", "IV", "IVa", "V", "Va", "Vb", "VI", "VII"
    );

    private enum Relation {
        OR, INTERMEDIATE
    }

    private final List<Component> types = new ArrayList<>();
    private Relation typesRelation = Relation.OR;

    private final List<Component> luminosityClasses = new ArrayList<>();
    private Relation luminosityClassesRelation = Relation.OR;

    private static final char STOP_SYMBOL = '$';

    private static class Component {

        private String value = "";
        private boolean doubt = false;

        enum Type {
            TYPE, LUMINOSITY_CLASS
        }

        public Component() {
        }

        public Component(final String value) {
            this.value += value;
        }

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
            if (isLuminosityClassSymbol(c)) {
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
                if (typeSymbols.contains(c)) {
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

        public void completeBy(final Component prev) {
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

        private static final List<Character> typeSymbols = Arrays.asList('O', 'B', 'A', 'F', 'G', 'K', 'M');
        private static final List<Character> luminosityClassSymbols = Arrays.asList('I', 'V', 'a', 'b');

        private static boolean isTypeSymbol(char c) {
            return typeSymbols.contains(c) || c == '.' || Character.isDigit(c);
        }

        private static boolean isLuminosityClassSymbol(char c) {
            return luminosityClassSymbols.contains(c);
        }

        private static boolean isComponentSymbol(char c) {
            return isTypeSymbol(c) || isLuminosityClassSymbol(c);
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private static final List<String> exceptionSpectralTypes
            = Arrays.asList("R", "S", "N", "C", "DA", "DB", "DC", "DD", "DE", "DF", "DG", "WR", "WN", "WC");

    public SpectralType(final String str) throws IllegalArgumentException {

        for (String exceptionSpectralType : exceptionSpectralTypes) {
            if (str.startsWith(exceptionSpectralType)) {
                throw new IllegalArgumentException();
            }
        }

        if (str.equals(str.toLowerCase())) {
            throw new IllegalArgumentException();
        }

        if (str.startsWith("sd")) {
            luminosityClasses.add(new Component("VI"));
        }

        final String s = str.split(" ")[0]
                .replaceAll("va", "")           //magic
                .replaceAll("CN.*", "")          //magic
                .replaceAll("\\+.*", "")          //magic
                .replaceAll("\\.+$", "")           //magic
                .replaceAll("[^OBAFGKM\\d\\.IVab/\\-:]", "")
                + STOP_SYMBOL;

        //System.out.println(s);

        Component cur = new Component();
        for (char c : s.toCharArray()) {
            if (cur.add(c)) {
                continue;
            }

            if (cur.getType() == Component.Type.TYPE) {
                if (!types.isEmpty()) {
                    //System.out.println(cur + " " + types.get(types.size() - 1));
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
                luminosityClasses.add(cur);
                if (c == '-') {
                    luminosityClassesRelation = Relation.INTERMEDIATE;
                }
            }

            cur = new Component();
            //System.out.println("! " + c);
            cur.add(c);
        }
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

    public Value toBV() {
        final List<Double> bvs = new ArrayList<>();
        for (Component type : types) {
            for (Component luminosityClass : luminosityClasses) {
                Double bv = toBV(type.value, luminosityClass.value);
                if (bv != null) {
                    bvs.add(bv);
                }
            }
        }

        if (bvs.isEmpty()) {
            return null;
        }

        double bv = 0.0;
        for (double bvEntry : bvs) {
            bv += bvEntry;
        }
        bv /= bvs.size();

        return new Value(bv, Collections.max(bvs) - bv);
    }

    private static Double toBV(final String type, final String luminosityClass) {
        Map<String, Integer> start = new HashMap<>();
        /*start.put("O", -10);
        start.put("B", 0);
        start.put("A", 10);
        start.put("F", 20);
        start.put("G", 30);
        start.put("K", 40);
        start.put("M", 48);*/
        start.put("O", 0);
        start.put("B", 10);
        start.put("A", 20);
        start.put("F", 30);
        start.put("G", 40);
        start.put("K", 50);
        start.put("M", 60);



        if (!lumin2bvs.containsKey(luminosityClass)) {
            return null;
        }
        List<Point2D.Double> bvs = lumin2bvs.get(luminosityClass);

        if (type.length() < 2) {
            return null;
        }
        //System.out.println(types);
        double code = start.get(type.substring(0, 1)) + Double.valueOf(type.substring(1, type.length()));
        for (int i = 0; i < bvs.size() - 1; ++i) {
            if (bvs.get(i).x <= code && code < bvs.get(i + 1).x) {
                return interpolate(bvs.get(i), bvs.get(i + 1), code);
            }
        }

        return null;
    }

    public String getLuminosityClass() {
        if (luminosityClasses.isEmpty()) {
            return null;
        }
        return luminosityClasses.get(0).value;
    }

    public String getType() {
        return types.get(0).value;
    }

    public double getTypeNumber() {
        if (types.get(0).value.length() < 2) {
            return 5;
        }
        return Double.valueOf(types.get(0).value.substring(1));
    }

    public String getTypeSymbol() {
        return types.get(0).value.substring(0, 1);
    }

    private static final Map<String, List<Point2D.Double>> lumin2bvs = new HashMap<>();

    static {
        final Scanner fin;
        try {
            fin = new Scanner(new FileInputStream("datasets/spect2bv_new.txt"));
        } catch (FileNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }

        final String[] titles = fin.nextLine().trim().split("\\s+");
        for (int i = 2; i < titles.length; ++i) {
            lumin2bvs.put(titles[i], new ArrayList<>());
        }

        while (fin.hasNextLine()) {
            String[] fields = fin.nextLine().trim().split("\\s+");
            double code = Double.valueOf(fields[1]);
            for (int i = 2; i < titles.length; ++i) {
                if (!fields[i].equals("-")) {
                    double bv = Double.valueOf(fields[i]);
                    lumin2bvs.get(titles[i]).add(new Point2D.Double(code, bv));
                }
            }
        }
        System.out.println("spect2bv loaded");
    }

    private static double interpolate(final Point2D.Double p1, final Point2D.Double p2, double x) {
        return (p2.y - p1.y) / (p2.x - p1.x) * (x - p1.x) + p1.y;
    }

    public static void main(final String[] args) throws FileNotFoundException {
        final Catalogue catalogue = new Catalogue("datasets/hipparcos1997.txt");
        int count = 0;
        for (String s : catalogue.getColumn("spect_type")) {
            try {
                final SpectralType spectralType = new SpectralType(s);
                if (spectralType.toBV() != null) {
                    count++;
                }
                System.out.println(s + " -> " + spectralType + " ~ " + spectralType.toBV());
            } catch (IllegalArgumentException e) {
                System.out.println(s + " -> null");
            }
        }
        System.out.println(count);
    }
}
