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
    private static final Logger LOG = Logger.getLogger(SpectTable.class.getName());

    public static final SpectTable TSVETKOV = read("tsvetkov", SpectTable.class.getResourceAsStream("/table/tsvetkov.txt"));
    public static final SpectTable SCHMIDT_KALER = read("schmidt-kaler", SpectTable.class.getResourceAsStream("/table/schmidt-kaler.txt"));
    public static final SpectTable STRIGEST = read("strigest", SpectTable.class.getResourceAsStream("/table/strigest.txt"));
    public static final SpectTable BINNEY_MERRIFIELD = read("binney-merrifield", SpectTable.class.getResourceAsStream("/table/binney-merrifield.txt"));

    public static final int MIN_CODE = 15;
    public static final int MAX_CODE = 79;

    private static final double BV_ERROR = 0.01;

    @NotNull
    private final String name;
    @NotNull
    private final Map<LuminosityClass, NavigableMap<Integer, Double>> table = new EnumMap<>(LuminosityClass.class);

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
                    final TempClass spect = TempClass.parse(fields[0]);
                    if (spect == null) {
                        throw new RuntimeException(fields[0] + " isn't spectral class!");
                    }
                    spectTable.add(LuminosityClass.valueOf(titles[i]), spect.getCode(), bv);
                }
            }
        }
        LOG.info("'" + name + "' + spect table loaded");
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
            final TempClass spect = TempClass.valueOf(code);
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
    public LuminosityClass[] getLumins() {
        return table.keySet().toArray(new LuminosityClass[table.size()]);
    }

    @NotNull
    public NavigableMap<Integer, Double> getBVs(@NotNull final LuminosityClass lumin) {
        return table.get(lumin);
    }

    @Nullable
    public Value getBV(@NotNull final TempClass spect, @NotNull final LuminosityClass lumin) {
        if (!table.containsKey(lumin)) {
            return null;
        }
        final NavigableMap<Integer, Double> bvs = getBVs(lumin);
        final int code = spect.getCode();
        if (spect.hasIntCode() && bvs.containsKey(code)) {
            return Value.of(bvs.get(code), BV_ERROR);
        }
        final Integer x1 = bvs.floorKey(code);
        final Integer x2 = bvs.higherKey(code);
        if (x1 == null || x2 == null) {
            return null;
        }
        return Value.of(MathTools.interpolate(x1, bvs.get(x1), x2, bvs.get(x2), spect.getDoubleCode()), BV_ERROR);
    }
}
