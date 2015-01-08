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
import ru.spbu.astro.dust.model.spect.LuminosityClass;
import ru.spbu.astro.dust.model.spect.SpectClass;
import ru.spbu.astro.dust.model.spect.table.SpectTable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * User: amosov-f
 * Date: 25.10.14
 * Time: 16:19
 */
public final class SpectTablePlot {
    private static final List<LuminosityClass> USED = Arrays.asList(LuminosityClass.III);

    public SpectTablePlot(@NotNull final List<SpectTable> tables) {
        final XYSeriesCollection dataset = new XYSeriesCollection();

        for (final SpectTable table : tables) {
            for (final LuminosityClass lumin : table.getLumins()) {
                if (USED.contains(lumin)) {
                    final String name;
                    if (tables.size() == 1) {
                        name = lumin.name();
                    } else {
                        name = table.getName() + "-" + lumin;
                    }
                    final XYSeries series = new XYSeries(name);
                    for (final int code : table.getBVs(lumin).keySet()) {
                        series.add(code - SpectTable.MIN_CODE, table.getBVs(lumin).get(code));
                    }
                    dataset.addSeries(series);
                }
            }
        }

        final List<String> spects = SpectTable.getCodeRange().mapToObj(code -> SpectClass.valueOf(code).toString()).collect(Collectors.toList());

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
                SpectTable.COMPOSITE,
                SpectTable.SCHMIDT_KALER
                //SpectTable.MAX_5
                //SpectTable.MAX,
                //new SpectTableCalculator().calculate(0.05)
        ));
    }
}
