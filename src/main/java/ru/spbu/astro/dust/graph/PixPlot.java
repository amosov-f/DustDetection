package ru.spbu.astro.dust.graph;

import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.SamplingXYLineRenderer;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import ru.spbu.astro.dust.algo.DustDetector;
import ru.spbu.astro.dust.model.Spheric;
import ru.spbu.astro.dust.model.Star;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PixPlot {

    private final DustDetector dustDetector;
    private ChartFrame frame;

    public PixPlot(final DustDetector dustDetector) {
        this.dustDetector = dustDetector;

        frame = new ChartFrame("Покраснение", null);

        plot(new Spheric(0, 0));

        frame.pack();
        frame.setVisible(true);
    }

    public void plot(final Spheric dir) {
        if (dir == null) {
            return;
        }

        final List<Star> supportStars = dustDetector.getSupportStars(dir);
        final List<Star> missStars = dustDetector.getMissStars(dir);

        final List<Star> stars = new ArrayList<>(supportStars);
        stars.addAll(missStars);

        XYIntervalSeriesCollection starsDataset = new XYIntervalSeriesCollection();
        starsDataset.addSeries(createXYIntegervalSeries(supportStars, "Звезды, по которым строится тренд"));
        starsDataset.addSeries(createXYIntegervalSeries(missStars, "Выбросы"));

        XYPlot plot = new XYPlot(
                starsDataset,
                new NumberAxis("Расстояние [пк]"),
                new NumberAxis("Покраснение [зв.вел.]"),
                new XYErrorRenderer()
        );

        double r = 0;
        for (final Star s : stars) {
            if (r < s.getR().value + s.getR().error) {
                r = s.getR().value + s.getR().error;
            }
        }

        double a = dustDetector.getSlope(dir).value;
        double b = dustDetector.getIntercept(dir).value;

        XYSeries trendSeries = new XYSeries("Тренд");
        trendSeries.add(0, b);
        trendSeries.add(r, a * r + b);

        plot.setDataset(1, new XYSeriesCollection(trendSeries));

        XYItemRenderer renderer = new SamplingXYLineRenderer();
        renderer.setSeriesStroke(0, new BasicStroke(3));
        plot.setRenderer(1, renderer);

        plot.setRangeZeroBaselineVisible(true);

        JFreeChart chart = new JFreeChart(
                "Покраснение в направлении " + dustDetector.getPixCenter(dustDetector.getPix(dir)),
                JFreeChart.DEFAULT_TITLE_FONT,
                plot,
                true
        );

        try {
            ChartUtilities.saveChartAsPNG(new File("documents/presentation/next.png"), chart, 900, 600);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        frame.getChartPanel().setChart(chart);
    }

    private XYIntervalSeries createXYIntegervalSeries(final List<Star> stars, final String name) {
        XYIntervalSeries series = new XYIntervalSeries(name);
        for (final Star s : stars) {
            series.add(
                    s.getR().value,
                    s.getR().value - s.getR().error,
                    s.getR().value + s.getR().error,
                    s.getExtinction().value,
                    s.getExtinction().value - s.getExtinction().error,
                    s.getExtinction().value + s.getExtinction().error
            );
        }
        return series;
    }
}
