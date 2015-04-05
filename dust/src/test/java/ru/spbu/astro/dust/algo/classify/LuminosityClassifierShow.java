package ru.spbu.astro.dust.algo.classify;

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.SamplingXYLineRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.commons.StarFilter;
import ru.spbu.astro.commons.Stars;
import ru.spbu.astro.commons.graph.HRDiagram;
import ru.spbu.astro.commons.spect.LuminosityClass;

import java.awt.*;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import static ru.spbu.astro.commons.graph.HRDiagram.BV_COLOR_LOWER_BOUND;
import static ru.spbu.astro.commons.graph.HRDiagram.BV_COLOR_UPPER_BOUND;

/**
 * User: amosov-f
 * Date: 31.03.15
 * Time: 0:33
 */
@Ignore
@SuppressWarnings("MagicNumber")
public class LuminosityClassifierShow {
    @Test
    public void show() throws IOException {
        final Star[] stars = StarFilter.of(Stars.ALL).mainLumin().stars();
        final SVMLuminosityClassifier classifier = new SVMLuminosityClassifier(stars, SVMLuminosityClassifier.Mode.TEST);

        final HRDiagram diagram = new HRDiagram(classifier.getStars());
        final XYPlot plot = diagram.getPlot();
        final XYSeriesCollection seriesCollection = new XYSeriesCollection();
//        seriesCollection.addSeries(create("Разделяющая прямая", -2.8388, 0.5055, 1.0056)); // all small errors
//        seriesCollection.addSeries(create("Разделяющая прямая", -3.9761, 0.668, 1.377)); // > 0.6
        seriesCollection.addSeries(create("Разделяющая прямая", -2.9876, 0.4526, 1.3547)); // all
//        seriesCollection.addSeries(create("Разделяющая прямая", classifier.getA(), classifier.getB(), classifier.getC()));
        plot.setDataset(1, seriesCollection);
        plot.setDataset(2, theory());
        plot.setRenderer(1, samplingRenderer(1));
        final XYItemRenderer renderer = samplingRenderer(2);
        renderer.setSeriesPaint(0, Color.CYAN);
        plot.setRenderer(2, renderer);
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        diagram.show();
    }

    @NotNull
    private static XYSeries create(@NotNull final String name, final double a, final double b, final double c) {
        final XYSeries series = new XYSeries(name);
        final double x1 = BV_COLOR_LOWER_BOUND;
        series.add(x1, -(c + a * x1) / b);
        final double x2 = BV_COLOR_UPPER_BOUND;
        series.add(x2, -(c + a * x2) / b);
        return series;
    }

    @NotNull
    private static XYItemRenderer samplingRenderer(final int nSeries) {
        final XYItemRenderer renderer = new SamplingXYLineRenderer();
        for (int i = 0; i < nSeries; i++) {
            renderer.setSeriesStroke(i, new BasicStroke(3));
        }
        return renderer;
    }

    @NotNull
    private static XYSeriesCollection theory() throws IOException {
        final Map<LuminosityClass, XYSeries> series = new EnumMap<>(LuminosityClass.class);
        for (final String line : IOUtils.toString(LuminosityClassifierShow.class.getResourceAsStream("/table/tsvetkov-m.txt")).split("\n")) {
            final String[] parts = line.split("\\s+");
            final LuminosityClass lumin = LuminosityClass.valueOf(parts[2]);
            series.putIfAbsent(lumin, new XYSeries(lumin + " в теории"));
            series.get(lumin).add(Double.parseDouble(parts[1]), Double.parseDouble(parts[0]));
        }
        return new XYSeriesCollection() {{
            series.values().forEach(this::addSeries);
        }};
    }

    @After
    public void tearDown() throws InterruptedException {
        Thread.sleep(Long.MAX_VALUE);
    }
}
