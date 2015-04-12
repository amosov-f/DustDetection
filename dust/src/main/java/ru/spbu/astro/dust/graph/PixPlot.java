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

import static org.jfree.chart.JFreeChart.DEFAULT_TITLE_FONT;

public final class PixPlot {
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
        final Star[] baseStars = dustTrendCalculator.getInlierStars(dir);
        final Star[] outlierStars = dustTrendCalculator.getOutlierStars(dir);
        if (baseStars == null || outlierStars == null) {
            return;
        }

        final Value slope = dustTrendCalculator.getSlope(dir);
        if (slope == null) {
            return;
        }


        final Star[] stars = ArrayUtils.addAll(baseStars, outlierStars);

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

        final double r = Arrays.stream(stars).mapToDouble(s -> s.getR().val()).max().getAsDouble();

        final XYSeries trend = new XYSeries("Тренд");
        trend.add(0, 0);
        trend.add(r, slope.val() * r);

        final XYSeries maxTrend = new XYSeries("Тренд + sigma");
        maxTrend.add(0, 0);
        maxTrend.add(r, slope.plusNSigma(1) * r);

        final XYSeries minTrend = new XYSeries("Тренд - sigma");
        minTrend.add(0, 0);
        minTrend.add(r, slope.plusNSigma(-1) * r);

        final XYSeriesCollection seriesCollection = new XYSeriesCollection();
        seriesCollection.addSeries(trend);
        seriesCollection.addSeries(minTrend);
        seriesCollection.addSeries(maxTrend);

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
