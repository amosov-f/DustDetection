package ru.spbu.astro.dust.algo.classify;

import org.jetbrains.annotations.NotNull;
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

import java.awt.*;

import static ru.spbu.astro.commons.graph.HRDiagram.*;

/**
 * User: amosov-f
 * Date: 31.03.15
 * Time: 0:33
 */
@Ignore
@SuppressWarnings("MagicNumber")
public class LuminosityClassifierShow {
    @Test
    public void testSVM() {
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
        final XYItemRenderer renderer = new SamplingXYLineRenderer();
        renderer.setSeriesStroke(0, new BasicStroke(3));
        renderer.setSeriesStroke(1, new BasicStroke(3));
        plot.setRenderer(1, renderer);
        diagram.show();
    }

    private static XYSeries create(@NotNull final String name, final double a, final double b, final double c) {
        final XYSeries series = new XYSeries(name);
        final double x1 = BV_COLOR_LOWER_BOUND;
        series.add(x1, -(c + a * x1) / b);
        final double x2 = BV_COLOR_UPPER_BOUND;
        series.add(x2, -(c + a * x2) / b);
        return series;
    }

    @After
    public void tearDown() throws InterruptedException {
        Thread.sleep(Long.MAX_VALUE);
    }
}
