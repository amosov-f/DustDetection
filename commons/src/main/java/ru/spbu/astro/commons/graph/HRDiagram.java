package ru.spbu.astro.commons.graph;

import org.jetbrains.annotations.NotNull;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.commons.StarFilter;
import ru.spbu.astro.commons.Stars;
import ru.spbu.astro.commons.spect.LuminosityClass;

import java.awt.*;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;


public final class HRDiagram {
    public static final double BV_COLOR_LOWER_BOUND = -0.5;
    public static final double BV_COLOR_UPPER_BOUND = 2;
    public static final double ABSOLUTE_MAGNITUDE_LOWER_BOUND = -5;
    public static final double ABSOLUTE_MAGNITUDE_UPPER_BOUND = 15;

    public static final double SCALE = (ABSOLUTE_MAGNITUDE_UPPER_BOUND - ABSOLUTE_MAGNITUDE_LOWER_BOUND) / (BV_COLOR_UPPER_BOUND - BV_COLOR_LOWER_BOUND);

    private static final double ERROR_VIEW_SHARE = 1;

    @NotNull
    private final ChartFrame frame;

    public HRDiagram(@NotNull final Star[] stars) {
        final Map<LuminosityClass, List<Star>> lumin2stars = new EnumMap<>(LuminosityClass.class);
        for (final Star star : stars) {
            final LuminosityClass lumin = star.getSpectType().getLumin();
            if (lumin != null) {
                lumin2stars.putIfAbsent(lumin, new ArrayList<>());
                lumin2stars.get(lumin).add(star);
            }
        }

        final XYIntervalSeriesCollection dataset = new XYIntervalSeriesCollection();
        for (final LuminosityClass lumin : lumin2stars.keySet()) {
            final List<Star> luminStars = lumin2stars.get(lumin);
            final XYIntervalSeries series = new XYIntervalSeries(lumin);

            for (final Star star : luminStars) {
                final double bv = star.getBVColor().val();
                final double dbv = star.getBVColor().err();
                final double M = star.getAbsMag().val();
                final double dM = star.getAbsMag().err();

                if (Math.random() < ERROR_VIEW_SHARE) {
                    series.add(bv, bv - dbv, bv + dbv, M, M - dM, M + dM);
                } else {
                    series.add(bv, bv, bv, M, M, M);
                }
            }

            System.out.println("#" + lumin + ": " + luminStars.size());

            dataset.addSeries(series);
        }

        final NumberAxis bvAxis = new NumberAxis("B-V [зв. вел.]");
        bvAxis.setTickUnit(new NumberTickUnit(0.3));
        final XYPlot plot = new XYPlot(
                dataset,
                bvAxis,
                new NumberAxis("M [зв. вел.]"),
                new XYErrorRenderer()
        );

        final JFreeChart chart = new JFreeChart(
                null,
                JFreeChart.DEFAULT_TITLE_FONT,
                plot,
                true
        );

        final XYDotRenderer renderer = new XYDotRenderer();
        renderer.setDotWidth(2);
        renderer.setDotHeight(2);
        chart.getXYPlot().setRenderer(renderer);

        chart.getXYPlot().getRangeAxis().setInverted(true);

        //for (int i = 0; i < dataset.getSeriesCount(); ++i) {
        //    chart.getXYPlot().getRenderer().setSeriesShape(i, new Ellipse2D.Float(-0.5f, -0.5f, 1, 1));
        //}

        chart.getXYPlot().getDomainAxis().setLowerBound(BV_COLOR_LOWER_BOUND);
        chart.getXYPlot().getDomainAxis().setUpperBound(BV_COLOR_UPPER_BOUND);
        chart.getXYPlot().getRangeAxis().setLowerBound(ABSOLUTE_MAGNITUDE_LOWER_BOUND);
        chart.getXYPlot().getRangeAxis().setUpperBound(ABSOLUTE_MAGNITUDE_UPPER_BOUND);

        plot.getDomainAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 16));
        plot.getDomainAxis().setLabelFont(new Font("SansSerif", Font.PLAIN, 16));
        plot.getRangeAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 16));
        plot.getRangeAxis().setLabelFont(new Font("SansSerif", Font.PLAIN, 16));
        chart.getLegend().setItemFont(new Font("SansSerif", Font.PLAIN, 16));
        
        frame = new ChartFrame("Hershprung-Russel diagram", chart);
        frame.pack();
    }
    
    public XYPlot getPlot() {
        return frame.getChartPanel().getChart().getXYPlot();
    }
    
    public void show() {
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        new HRDiagram(StarFilter.of(Stars.ALL).mainLumin().stars()).show();
    }
}
