package ru.spbu.astro.dust.graph;

import org.jetbrains.annotations.NotNull;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import ru.spbu.astro.dust.model.table.SpectTable;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static ru.spbu.astro.dust.model.SpectralType.LuminosityClass;

/**
 * User: amosov-f
 * Date: 25.10.14
 * Time: 16:19
 */
public final class SpectTablePlot {
    public SpectTablePlot(@NotNull final List<SpectTable> tables) {
        final XYSeriesCollection dataset = new XYSeriesCollection();

        final Set<Double> codes = new TreeSet<>();

        for (final SpectTable table : tables) {
            for (final LuminosityClass lumin : table.getLumins()) {
                if (LuminosityClass.USED.contains(lumin)) {
                    final String name;
                    if (tables.size() == 1) {
                        name = lumin.name();
                    } else {
                        name = table.getName() + "-" + lumin;
                    }
                    final XYSeries series = new XYSeries(name);
                    for (final Double code : table.getBVs(lumin).keySet()) {
                        series.add(code, table.getBVs(lumin).get(code));
                        codes.add(code);
                    }
                    dataset.addSeries(series);
                }
            }
        }

        final List<String> spects = codes.stream().map(SpectTable::spect).collect(Collectors.toList());

        final XYPlot plot = new XYPlot(
                dataset,
                new SymbolAxis("спектральный класс", spects.toArray(new String[spects.size()])),
                new NumberAxis("B-V"),
                new XYSplineRenderer()
        );

        final ChartFrame frame = new ChartFrame("Таблица B-V", new JFreeChart("Таблица B-V", plot));
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(@NotNull final String[] args) {
        new SpectTablePlot(Arrays.asList(
                SpectTable.TSVETKOV,
                SpectTable.STRIGEST,
                SpectTable.COMBINED
        ));
    }
}
