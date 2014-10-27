package ru.spbu.astro.dust.model.table;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.dust.algo.SpectTableCalculator;
import ru.spbu.astro.dust.model.SpectralType;

import java.awt.geom.Point2D;
import java.io.InputStream;
import java.util.*;

import static ru.spbu.astro.dust.model.SpectralType.LuminosityClass;

/**
* User: amosov-f
* Date: 26.10.14
* Time: 13:03
*/
public final class SpectTable {
    public static final SpectTable TSVETKOV = SpectTable.read("Tsvetkov", SpectTable.class.getResourceAsStream("/spect2bv/spect2bv_new.txt"));
    public static final SpectTable STRIGEST = SpectTable.read("Strigest", SpectTable.class.getResourceAsStream("/spect2bv/spect2bv.txt"));
    public static final SpectTable COMBINED = new IIIM2SpectTableCombinator().combine(TSVETKOV, STRIGEST);
    public static final SpectTable MAX = SpectTableCalculator.calculate(0.1);

    @NotNull
    private final String name;

    @NotNull
    public final EnumMap<LuminosityClass, List<Point2D.Double>> table;

    public SpectTable(@NotNull final String name) {
        this.name = name;
        table = new EnumMap<>(LuminosityClass.class);
    }

    public SpectTable(@NotNull final SpectTable table) {
        this.name = table.name;
        this.table = new EnumMap<>(table.table);
    }

    @NotNull
    private static SpectTable read(@NotNull final String name, @NotNull final InputStream in) {
        final SpectTable spectTable = new SpectTable(name);

        final Scanner fin = new Scanner(in);

        final String[] titles = fin.nextLine().trim().split("\\s+");
        for (int i = 2; i < titles.length; ++i) {
            spectTable.table.put(LuminosityClass.valueOf(titles[i]), new ArrayList<>());
        }

        while (fin.hasNextLine()) {
            final String[] fields = fin.nextLine().trim().split("\\s+");
            for (int i = 2; i < titles.length; i++) {
                if (!"-".equals(fields[i])) {
                    double bv = Double.valueOf(fields[i]);
                    spectTable.table.get(LuminosityClass.valueOf(titles[i])).add(new Point2D.Double(code(fields[0]), bv));
                }
            }
        }
        System.out.println("spect table loaded");
        return spectTable;
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
    public Map<Double, Double> getBVs(@NotNull final LuminosityClass lumin) {
        final Map<Double, Double> bvs = new TreeMap<>();
        for (final Point2D.Double p : table.get(lumin)) {
            bvs.put(p.getX(), p.getY());
        }
        return bvs;
    }

    @Nullable
    public Double getBV(final String spect, @NotNull final LuminosityClass lumin) {
        if (!table.containsKey(lumin)) {
            return null;
        }
        final List<Point2D.Double> bvs = table.get(lumin);

        if (spect.length() < 2) {
            return null;
        }
        final double code = code(spect);
        for (int i = 0; i < bvs.size() - 1; ++i) {
            if (bvs.get(i).x <= code && code < bvs.get(i + 1).x) {
                return interpolate(bvs.get(i), bvs.get(i + 1), code);
            }
        }

        return null;
    }

    public static double code(@NotNull final String spectType) {
        return 10 * Arrays.asList(SpectralType.TypeSymbol.values()).indexOf(SpectralType.TypeSymbol.parse(spectType.charAt(0))) + Double.valueOf(spectType.substring(1));
    }

    public static String spect(final double code) {
        return (SpectralType.TypeSymbol.values()[(int) code / 10] + "" + (code - (int) code / 10 * 10)).replaceAll("\\.0", "");
    }

    private static double interpolate(@NotNull final Point2D.Double p1, @NotNull final Point2D.Double p2, final double x) {
        return (p2.y - p1.y) / (p2.x - p1.x) * (x - p1.x) + p1.y;
    }
}
