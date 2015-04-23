package ru.spbu.astro.commons.spect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.util.MathTools;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
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
    public static final SpectTable TSVETKOV_MID = new SpectTable("tsvetkov-mid") {
        @Nullable
        @Override
        public Double getBV(@NotNull final SpectClass spect, @NotNull final LuminosityClass lumin) {
            if (lumin != LuminosityClass.III_V) {
                return TSVETKOV.getBV(spect, lumin);
            }
            final Double bvIII = TSVETKOV.getBV(spect, LuminosityClass.III);
            final Double bvV = TSVETKOV.getBV(spect, LuminosityClass.V);
            final double w1 = 0.114;
            final double w2 = 1 - w1;
            return bvIII != null && bvV != null ? w1 * bvIII + w2 * bvV : null;
        }
    };

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
    public Double getBV(@NotNull final SpectClass spect, @NotNull final LuminosityClass lumin) {
        if (!table.containsKey(lumin)) {
            return null;
        }
        final NavigableMap<Integer, Double> bvs = getBVs(lumin);
        final int code = spect.getCode();
        if (bvs.containsKey(code)) {
            return bvs.get(code);
        }
        final Integer x1 = bvs.floorKey(code);
        final Integer x2 = bvs.higherKey(code);
        if (x1 != null && x2 != null) {
            return MathTools.interpolate(x1, bvs.get(x1), x2, bvs.get(x2), spect.getDoubleCode());
        }
        return null;
    }

    public static void main(String[] args) {
        System.out.println("");
        System.out.println();
    }
}
