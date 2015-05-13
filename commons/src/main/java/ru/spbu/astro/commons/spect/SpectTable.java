package ru.spbu.astro.commons.spect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.util.MathTools;
import ru.spbu.astro.util.Value;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.IntStream;


/**
 * User: amosov-f
 * Date: 26.10.14
 * Time: 13:03
 */
public class SpectTable {
    private static final Logger LOGGER = Logger.getLogger(SpectTable.class.getName());

    public static final SpectTable TSVETKOV = read("tsvetkov", SpectTable.class.getResourceAsStream("/table/tsvetkov.txt"));
    public static final SpectTable SCHMIDT_KALER = read("schmidt-kaler", SpectTable.class.getResourceAsStream("/table/schmidt-kaler.txt"));

    public static final int MIN_CODE = 5;
    public static final int MAX_CODE = 69;

    @NotNull
    private final String name;
    @NotNull
    private final EnumMap<LuminosityClass, NavigableMap<Integer, Double>> table = new EnumMap<>(LuminosityClass.class);

    public SpectTable(@NotNull final String name) {
        this.name = name;
    }

    @NotNull
    public static IntStream codeRange() {
        return IntStream.rangeClosed(MIN_CODE, MAX_CODE);
    }

    @NotNull
    public static SpectTable getInstance() {
        return TSVETKOV;
    }

    @NotNull
    public static SpectTable read(@NotNull final String name, @NotNull final InputStream in) {
        final SpectTable spectTable = new SpectTable(name);
        final Scanner fin = new Scanner(in);
        final String[] titles = fin.nextLine().trim().split("\\s+");
        while (fin.hasNextLine()) {
            final String[] fields = fin.nextLine().trim().split("\\s+");
            for (int i = 1; i < titles.length; i++) {
                if (!"-".equals(fields[i])) {
                    final double bv = Double.parseDouble(fields[i]);
                    final SpectClass spect = SpectClass.parse(fields[0]);
                    if (spect == null) {
                        throw new RuntimeException(fields[0] + " isn't spectral class!");
                    }
                    spectTable.add(LuminosityClass.valueOf(titles[i]), spect.getCode(), bv);
                }
            }
        }
        LOGGER.info("'" + name + "' + spect table loaded");
        return spectTable;
    }

    public void add(@NotNull final LuminosityClass lumin, final int code, final double bv) {
        table.putIfAbsent(lumin, new TreeMap<>());
        table.get(lumin).put(code, bv);
    }

    public void write(@NotNull final OutputStream out) {
        final PrintWriter writer = new PrintWriter(out);
        writer.print("Spec\t");
        for (final LuminosityClass lumin : table.keySet()) {
            writer.print(lumin + "\t");
        }
        writer.println();
        for (final int code : table.get(LuminosityClass.V).keySet()) {
            final SpectClass spect = SpectClass.valueOf(code);
            writer.print(spect + "\t");
            for (final LuminosityClass lumin : table.keySet()) {
                writer.printf(Locale.US, "%.3f\t", getBV(spect, lumin));
            }
            writer.println();
        }
        writer.flush();
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public List<LuminosityClass> getLumins() {
        return new ArrayList<>(table.keySet());
    }

    @NotNull
    public NavigableMap<Integer, Double> getBVs(@NotNull final LuminosityClass lumin) {
        return table.get(lumin);
    }

    @Nullable
    public Value getBV(@NotNull final SpectClass spect, @NotNull final LuminosityClass lumin) {
        if (!table.containsKey(lumin)) {
            return null;
        }
        final NavigableMap<Integer, Double> bvs = getBVs(lumin);
        final int code = spect.getCode();
        final Integer x1;
        final Integer x2 = bvs.higherKey(code);
        final Double d1;
        final Double d2;
        final double val;
        if (spect.hasIntCode() && bvs.containsKey(code)) {
            x1 = bvs.lowerKey(code);
            val = bvs.get(code);
            d1 = x1 != null ? Math.abs(val - bvs.get(x1)) / 2 : null;
            d2 = x2 != null ? Math.abs(bvs.get(x2) - val) / 2 : null;
            if (d1 == null && d2 == null) {
                return null;
            }
        } else {
            x1 = bvs.floorKey(code);
            if (x1 == null || x2 == null) {
                return null;
            }
            val = MathTools.interpolate(x1, bvs.get(x1), x2, bvs.get(x2), spect.getDoubleCode());
            d1 = Math.abs(val - bvs.get(x1));
            d2 = Math.abs(bvs.get(x2) - val);
        }
        return Value.of(val, Arrays.stream(new Double[]{d1, d2})
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .getAsDouble());
    }

//    @NotNull
//    private static Value bv(final int code, @NotNull final NavigableMap<Integer, Double> bvs) {
//        final Integer x1 = bvs.lowerKey(code);
//        final Integer x2 = bvs.higherKey(code);
//        if (x1 != null) {
//            x1
//        }
//    }

//    public static void main(String[] args) {
//        final Scanner fin = new Scanner(SpectTable.class.getResourceAsStream("/table/tsvetkov.txt"));
//        fin.nextLine();
//        final List<String[]> fields = new ArrayList<>();
//        while (fin.hasNextLine()) {
//            fields.add(fin.nextLine().trim().split("\\s+"));
//        }
//        for (int i = 0; i < fields.size() / 4; i++) {
//            final String[] f1 = fields.get(i);
//            final String[] f2 = fields.get(i + fields.size() / 4);
//            final String[] f3 = fields.get(i + 2 * fields.size() / 4);
//            final String[] f4 = fields.get(i + 3 * fields.size() / 4);
//            System.out.println("                " + f1[0] + "    &    " + f1[1] + "    &    " + f1[2] + "    &    "
//                    + f2[0] + "    &    " + f2[1] + "    &    " + f2[2] + "    &    "
//                    + f3[0] + "    &    " + f3[1] + "    &    " + f3[2] + "    &    "
//                    + f4[0] + "    &    " + f4[1] + "    &    " + f4[2] + "    \\\\");
//        }
//    }

    public static void main(String[] args) throws FileNotFoundException {
        Scanner fin = new Scanner(new FileInputStream("docs/articles/related/tables/III.TXT"));
        final List<String[]> iii = new ArrayList<>();
        while (fin.hasNextLine()) {
            iii.add(Arrays.copyOfRange(fin.nextLine().trim().split("\\s+"), 1, 4));
        }
        fin = new Scanner(new FileInputStream("docs/articles/related/tables/V.TXT"));
        final List<String[]> v = new ArrayList<>();
        while (fin.hasNextLine()) {
            v.add(Arrays.copyOfRange(fin.nextLine().trim().split("\\s+"), 1, 4));
        }
        final int k = 2;
        for (int i = 0; i < iii.size() / k; i++) {
            System.out.print("                    ");
            for (int j = 0; j < k; j++) {
                final String[] f1 = iii.get(i + j * iii.size() / k);
                final String[] f2 = v.get(i + j * v.size() / k);
                System.out.print(f1[0] + "    &    " + f1[1] + "    &    " + f1[2] + "    &    ");
                System.out.print(f2[1] + "    &    " + f2[2]);
                if (j != k - 1) {
                    System.out.print("    &    ");
                }
            }
            System.out.println("    \\\\");
        }
    }
}
