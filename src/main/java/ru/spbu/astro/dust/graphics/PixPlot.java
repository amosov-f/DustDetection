package ru.spbu.astro.dust.graphics;

import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.SamplingXYLineRenderer;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.math.plot.Plot2DPanel;
import ru.spbu.astro.dust.algo.DustDetector;
import ru.spbu.astro.dust.model.Spheric;
import ru.spbu.astro.dust.model.Star;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PixPlot extends Plot2DPanel {

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

        XYIntervalSeriesCollection dataset1 = new XYIntervalSeriesCollection();
        dataset1.addSeries(createXYIntegervalSeries(supportStars, "Звезды, по которым строится тренд"));
        dataset1.addSeries(createXYIntegervalSeries(missStars, "Выбросы"));

        XYErrorRenderer renderer = new XYErrorRenderer();

        XYPlot plot = new XYPlot(dataset1, new NumberAxis("r"), new NumberAxis("ext"), renderer);

        double a = dustDetector.getSlope(dir).value;
        double b = dustDetector.getIntercept(dir).value;

        final List<Star> stars = new ArrayList<>(supportStars);
        stars.addAll(missStars);
        Collections.sort(stars);

        XYSeries lineSeries = new XYSeries("Тренд");
        lineSeries.add(0, b);
        for (final Star s : stars) {
            double r = s.getR().value;
            lineSeries.add(r, a * r + b);
        }
        plot.setDataset(1, new XYSeriesCollection(lineSeries));
        plot.setRenderer(1, new SamplingXYLineRenderer());

        JFreeChart chart = new JFreeChart(
                "Покраснение в направлении " + dir,
                JFreeChart.DEFAULT_TITLE_FONT,
                plot,
                true
        );

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
