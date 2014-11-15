package ru.spbu.astro.dust.model.spect.table;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.dust.algo.SpectTableCalculator;
import ru.spbu.astro.dust.model.spect.SpectClass;
import ru.spbu.astro.dust.model.spect.LuminosityClass;

import java.io.InputStream;
import java.util.*;


/**
* User: amosov-f
* Date: 26.10.14
* Time: 13:03
*/
public final class SpectTable {
    public static final SpectTable TSVETKOV = SpectTable.read("Tsvetkov", SpectTable.class.getResourceAsStream("/table/tsvetkov.txt"));
    public static final SpectTable STRIGEST = SpectTable.read("Strigest", SpectTable.class.getResourceAsStream("/table/strigest.txt"));
    public static final SpectTable COMBINED = new IIIM2SpectTableCombinator().combine(TSVETKOV, STRIGEST);
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
                    table.get(LuminosityClass.valueOf(titles[i])).put(SpectClass.parse(fields[0]).getCode(), bv);
                }
            }
        }
        System.out.println("spect table loaded");
        return new SpectTable(name, table);
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
    public Map<Integer, Double> getBVs(@NotNull final LuminosityClass lumin) {
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
