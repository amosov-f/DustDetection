package ru.spbu.astro.dust.graph;

import org.jetbrains.annotations.NotNull;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import ru.spbu.astro.dust.model.Catalogue;
import ru.spbu.astro.dust.model.SpectralType;
import ru.spbu.astro.dust.model.Star;
import ru.spbu.astro.dust.model.Value;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.*;

public class MissObserver {

    private final List<Star> missStars = new ArrayList<>();

    public MissObserver(@NotNull final Catalogue catalogue) {
        for (final Star star : catalogue.getStars()) {
            final Value ext = star.getExtinction();
            if (ext.getValue() + 3 * ext.getError() < 0) {
                missStars.add(star);
            }
        }

        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        final Map<String, Integer> missCount = countMap(missStars);
        final Map<String, Integer> count = countMap(catalogue.getStars());

        for (final String type : count.keySet()) {
            if (!missCount.containsKey(type)) {
                missCount.put(type, 0);
            }
            dataset.addValue(1.0 * missCount.get(type) / count.get(type), type, count.get(type));
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Гистограмма звезд с отрицательными покраснениями",
                "B-V",
                "Count",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                true
        );

        ChartFrame frame = new ChartFrame("Звезды с отрицательным покраснением", chart);
        frame.pack();
        frame.setVisible(true);
    }

    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder();
        str.append("hip\tspect\text\tsigma_ext\n");
        for (final Star star : missStars) {
            final Value ext = star.getExtinction();
            str.append(String.format(
                    "%d\t%s\t%.2f\t%.2f\n",
                    star.getId(), star.getSpectralType().toString(), ext.getValue(), star.getExtinction().getError()
            ));
        }
        return str.toString();
    }

    @NotNull
    public static Map<String, Integer> countMap(@NotNull final List<Star> stars) {
        final Map<String, Integer> count = new HashMap<>();

        for (final Star star : stars) {
            final SpectralType.TypeSymbol sym = star.getSpectralType().getTypeSymbol();
            final double d = star.getSpectralType().getTypeNumber();

            final String key;
            if (d < 5) {
                key = sym + "0-4";
            } else {
                key = sym + "5-9";
            }

            if (!count.containsKey(key)) {
                count.put(key, 0);
            }
            count.put(key, count.get(key) + 1);
        }

        return count;
    }

    @NotNull
    public List<Integer> getMissIds() {
        final List<Integer> missIds = new ArrayList<>();
        for (final Star s : missStars) {
            missIds.add(s.getId());
        }
        return missIds;
    }

    public static void main(final String[] args) throws FileNotFoundException {
        final Catalogue catalogue = Catalogue.HIPPARCOS_UPDATED;

        final MissObserver missObserver = new MissObserver(catalogue);
        {
            final PrintWriter fout = new PrintWriter(new FileOutputStream("results/6.txt"));

            Locale.setDefault(Locale.US);
            fout.print(missObserver.toString());
            fout.flush();
        }
        {
            final PrintWriter fout = new PrintWriter(new FileOutputStream("results/10.txt"));
            for (int id : missObserver.getMissIds()) {
                fout.println(id);
            }
            fout.flush();
        }
    }

}
