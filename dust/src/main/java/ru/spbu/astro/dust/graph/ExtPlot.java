package ru.spbu.astro.dust.graph;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jfree.chart.ChartFrame;
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
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.dust.algo.DustTrendCalculator;
import ru.spbu.astro.healpix.Healpix;
import ru.spbu.astro.util.Value;

import java.awt.*;
import java.util.Arrays;
import java.util.logging.Logger;

import static org.jfree.chart.JFreeChart.DEFAULT_TITLE_FONT;

/**
 * User: amosov-f
 * Date: 03.05.15
 * Time: 12:33
 */
public class ExtPlot {
    private static final Logger LOG = Logger.getLogger(ExtPlot.class.getName());

    @NotNull
    private final ChartFrame frame;

    public ExtPlot() {
        frame = new ChartFrame("Покраснение", null);
        frame.pack();
        frame.setVisible(true);
    }

    public void plot(@NotNull final DustTrendCalculator.Regression regression, @Nullable final String title) {
        final Star[] inliers = regression.getInliers();
        final Star[] outliers = regression.getOutliers();

        final Value slope = regression.getSlope();

        LOG.info("k = " + slope.multiply(1000));

        final Star[] stars = ArrayUtils.addAll(inliers, outliers);
        LOG.info("n = " + stars.length);

        final XYIntervalSeriesCollection starsDataset = new XYIntervalSeriesCollection() {{
            addSeries(createXYIntegervalSeries(inliers, "Звезды, по которым строится тренд"));
            addSeries(createXYIntegervalSeries(outliers, "Выбросы"));
        }};

        final XYPlot plot = new XYPlot(
                starsDataset,
                new NumberAxis("Расстояние [пк]"),
                new NumberAxis("Покраснение [зв.вел.]"),
                new XYErrorRenderer()
        );

        final double r = Arrays.stream(stars).mapToDouble(s -> s.getR().plusNSigma(1)).max().getAsDouble();

        final XYSeries trend = new XYSeries("Тренд");
        trend.add(0, 0);
        trend.add(r, slope.val() * r);

        final XYSeriesCollection seriesCollection = new XYSeriesCollection();
        seriesCollection.addSeries(trend);

        plot.setDataset(1, seriesCollection);

        final XYItemRenderer renderer = new SamplingXYLineRenderer();
        renderer.setStroke(new BasicStroke(3));
        plot.setRenderer(1, renderer);

        plot.setRangeZeroBaselineVisible(true);

        final JFreeChart chart = new JFreeChart(title, DEFAULT_TITLE_FONT, plot, true);

        final int fontSize = 16;
        plot.getDomainAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, fontSize));
        plot.getDomainAxis().setLabelFont(new Font("SansSerif", Font.PLAIN, fontSize));
        plot.getRangeAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, fontSize));
        plot.getRangeAxis().setLabelFont(new Font("SansSerif", Font.PLAIN, fontSize));
        chart.getLegend().setItemFont(new Font("SansSerif", Font.PLAIN, fontSize));

        frame.getChartPanel().setChart(chart);
    }

    @NotNull
    private XYIntervalSeries createXYIntegervalSeries(@NotNull final Star[] stars, @NotNull final String name) {
        final XYIntervalSeries series = new XYIntervalSeries(name);
        for (final Star s : stars) {
            series.add(
                    s.getR().val(),
                    s.getR().val() - s.getR().err(),
                    s.getR().val() + s.getR().err(),
                    s.getExtinction().val(),
                    s.getExtinction().val() - s.getExtinction().err(),
                    s.getExtinction().val() + s.getExtinction().err()
            );
        }
        return series;
    }
}
