package ru.spbu.astro.dust.model.spect.table;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.dust.algo.SpectTableCalculator;
import ru.spbu.astro.dust.model.spect.SpectClass;
import ru.spbu.astro.dust.model.spect.LuminosityClass;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;


/**
* User: amosov-f
* Date: 26.10.14
* Time: 13:03
*/
public final class SpectTable {
    public static final SpectTable TSVETKOV = SpectTable.read("tsvetkov", SpectTable.class.getResourceAsStream("/table/tsvetkov.txt"));
    public static final SpectTable STRIGEST = SpectTable.read("strigest", SpectTable.class.getResourceAsStream("/table/strigest.txt"));
    public static final SpectTable COMBINED = new IIIM2SpectTableCombinator().combine(TSVETKOV, STRIGEST);
    public static final SpectTable MAX_5 = SpectTable.read("max-5%", SpectTable.class.getResourceAsStream("/table/max-0.05.txt"));
    public static final SpectTable MAX_3 = SpectTable.read("max-3%", SpectTable.class.getResourceAsStream("/table/max-0.03.txt"));
    public static final SpectTable COMPOSITE = SpectTable.read("composite", SpectTable.class.getResourceAsStream("/table/min(tsvetkov,max-3%).txt"));
    //public static final SpectTable MAX = SpectTableCalculator.calculate(0.1);

    @NotNull
    private final String name;

    @NotNull
    public final EnumMap<LuminosityClass, NavigableMap<Integer, Double>> table;

    public SpectTable(@NotNull final String name, @NotNull final EnumMap<LuminosityClass, NavigableMap<Integer, Double>> table) {
        this.name = name;
        this.table = table;
    }

    @NotNull
    private static SpectTable read(@NotNull final String name, @NotNull final InputStream in) {
        final EnumMap<LuminosityClass, NavigableMap<Integer, Double>> table = new EnumMap<>(LuminosityClass.class);

        final Scanner fin = new Scanner(in);

        final String[] titles = fin.nextLine().trim().split("\\s+");
        for (int i = 1; i < titles.length; ++i) {
            table.put(LuminosityClass.valueOf(titles[i]), new TreeMap<>());
        }

        while (fin.hasNextLine()) {
            final String[] fields = fin.nextLine().trim().split("\\s+");
            for (int i = 1; i < titles.length; i++) {
                if (!"-".equals(fields[i])) {
                    double bv = Double.valueOf(fields[i]);
                    final SpectClass spect = SpectClass.parse(fields[0]);
                    assert spect != null;
                    table.get(LuminosityClass.valueOf(titles[i])).put(spect.getCode(), bv);
                }
            }
        }
        System.out.println("spect table loaded");
        return new SpectTable(name, table);
    }

    public void write(@NotNull final OutputStream out) {
        final PrintWriter writer = new PrintWriter(out);
        writer.print("Spec\t");
        for (final LuminosityClass lumin : table.keySet()) {
            writer.print(lumin + "\t");
        }
        writer.println();
        for (int code = 5; code <= 67; code++) {
            final SpectClass spect = SpectClass.valueOf(code);
            writer.print(spect + "\t");
            for (final LuminosityClass lumin : table.keySet()) {
                //System.out.println(spect + " " + lumin + " -> " + getBV(spect, lumin));
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
        final NavigableMap<Integer, Double> bvs = table.get(lumin);
        final Integer x1 = bvs.floorKey(spect.getCode());
        final Integer x2 = bvs.higherKey(spect.getCode());
        if (x1 != null && x2 != null) {
            return interpolate(x1, bvs.get(x1), x2, bvs.get(x2), spect.getDoubleCode());
        }
        return null;
    }

    private static double interpolate(final double x1, final double y1, final double x2, final double y2, final double x) {
        return (y2 - y1) / (x2 - x1) * (x - x1) + y1;
    }
}
