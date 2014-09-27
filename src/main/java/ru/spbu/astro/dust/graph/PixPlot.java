package ru.spbu.astro.dust.graph;

import org.jetbrains.annotations.NotNull;
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
import ru.spbu.astro.dust.algo.DustTrendCalculator;
import ru.spbu.astro.dust.model.Spheric;
import ru.spbu.astro.dust.model.Star;
import ru.spbu.astro.dust.model.Value;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.jfree.chart.JFreeChart.DEFAULT_TITLE_FONT;

public final class PixPlot {
    @NotNull
    private final DustTrendCalculator dustTrendCalculator;
    @NotNull
    private ChartFrame frame;

    public PixPlot(@NotNull final DustTrendCalculator dustTrendCalculator) {
        this.dustTrendCalculator = dustTrendCalculator;

        frame = new ChartFrame("Покраснение", null);

        plot(new Spheric(0, 0));

        frame.pack();
        frame.setVisible(true);
    }

    public void plot(@NotNull final Spheric dir) {
        final List<Star> supportStars = dustTrendCalculator.getSupportStars(dir);
        final List<Star> missStars = dustTrendCalculator.getMissStars(dir);
        if (supportStars == null || missStars == null) {
            return;
        }

        final Value slope = dustTrendCalculator.getSlope(dir);
        if (slope == null) {
            return;
        }

        final List<Star> stars = new ArrayList<>(supportStars);
        stars.addAll(missStars);

        final XYIntervalSeriesCollection starsDataset = new XYIntervalSeriesCollection() {{
            addSeries(createXYIntegervalSeries(supportStars, "Звезды, по которым строится тренд"));
            addSeries(createXYIntegervalSeries(missStars, "Выбросы"));
        }};

        final XYPlot plot = new XYPlot(
                starsDataset,
                new NumberAxis("Расстояние [пк]"),
                new NumberAxis("Покраснение [зв.вел.]"),
                new XYErrorRenderer()
        );

        double r = 0;
        for (final Star star : stars) {
            if (r < star.getR().getValue() + star.getR().getError()) {
                r = star.getR().getValue() + star.getR().getError();
            }
        }

        final double a = slope.getValue();

        final XYSeries trendSeries = new XYSeries("Тренд");
        trendSeries.add(0, 0);
        trendSeries.add(r, a * r);

        plot.setDataset(1, new XYSeriesCollection(trendSeries));

        final XYItemRenderer renderer = new SamplingXYLineRenderer();
        renderer.setSeriesStroke(0, new BasicStroke(3));
        plot.setRenderer(1, renderer);

        plot.setRangeZeroBaselineVisible(true);

        final JFreeChart chart = new JFreeChart(
                "Покраснение в направлении " + dustTrendCalculator.getPixCenter(dustTrendCalculator.getPix(dir)),
                DEFAULT_TITLE_FONT,
                plot,
                true
        );

        try {
            ChartUtilities.saveChartAsPNG(new File("documents/presentation/buffer.png"), chart, 900, 600);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        frame.getChartPanel().setChart(chart);
    }

    private XYIntervalSeries createXYIntegervalSeries(List<Star> stars, String name) {
        XYIntervalSeries series = new XYIntervalSeries(name);
        for (Star s : stars) {
            series.add(
                    s.getR().getValue(),
                    s.getR().getValue() - s.getR().getError(),
                    s.getR().getValue() + s.getR().getError(),
                    s.getExtinction().getValue(),
                    s.getExtinction().getValue() - s.getExtinction().getError(),
                    s.getExtinction().getValue() + s.getExtinction().getError()
            );
        }
        return series;
    }
}
