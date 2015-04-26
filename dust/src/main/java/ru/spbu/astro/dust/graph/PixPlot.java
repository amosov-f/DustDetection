package ru.spbu.astro.dust.graph;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
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
import ru.spbu.astro.commons.Spheric;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.dust.algo.DustTrendCalculator;
import ru.spbu.astro.healpix.Healpix;
import ru.spbu.astro.util.Value;

import java.awt.*;
import java.util.Arrays;
import java.util.logging.Logger;

import static org.jfree.chart.JFreeChart.DEFAULT_TITLE_FONT;

public final class PixPlot {
    private static final Logger LOG = Logger.getLogger(PixPlot.class.getName());

    @NotNull
    private final DustTrendCalculator dustTrendCalculator;
    @NotNull
    private final ChartFrame frame;

    public PixPlot(@NotNull final DustTrendCalculator dustTrendCalculator) {
        this.dustTrendCalculator = dustTrendCalculator;

        frame = new ChartFrame("Покраснение", null);

        plot(new Spheric(0, 0));

        frame.pack();
        frame.setVisible(true);
    }

    public void plot(@NotNull final Spheric dir) {
        final Star[] baseStars = dustTrendCalculator.getInliers(dir);
        final Star[] outlierStars = dustTrendCalculator.getOutliers(dir);
        if (baseStars == null || outlierStars == null) {
            return;
        }

        final Value slope = dustTrendCalculator.getSlope(dir);
        if (slope == null) {
            return;
        }
        LOG.info("k_" + new Healpix(dustTrendCalculator.getNSide()).getPix(dir) + " = " + slope.multiply(1000));

        final Star[] stars = ArrayUtils.addAll(baseStars, outlierStars);
        LOG.info("n = " + stars.length);

        final XYIntervalSeriesCollection starsDataset = new XYIntervalSeriesCollection() {{
            addSeries(createXYIntegervalSeries(baseStars, "Звезды, по которым строится тренд"));
            addSeries(createXYIntegervalSeries(outlierStars, "Выбросы"));
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

        final JFreeChart chart = new JFreeChart(
                "Покраснение в направлении " + new Healpix(dustTrendCalculator.getNSide()).getCenter(dir),
                DEFAULT_TITLE_FONT,
                plot,
                true
        );

        plot.getDomainAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 16));
        plot.getDomainAxis().setLabelFont(new Font("SansSerif", Font.PLAIN, 16));
        plot.getRangeAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 16));
        plot.getRangeAxis().setLabelFont(new Font("SansSerif", Font.PLAIN, 16));
        chart.getLegend().setItemFont(new Font("SansSerif", Font.PLAIN, 16));

//        try {
//            ChartUtilities.saveChartAsPNG(new File("documents/presentation/buffer.png"), chart, 900, 600);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

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
