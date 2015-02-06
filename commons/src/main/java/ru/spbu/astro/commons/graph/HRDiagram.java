package ru.spbu.astro.commons.graph;

import org.jetbrains.annotations.NotNull;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.commons.spect.LuminosityClass;
import ru.spbu.astro.commons.Catalogues;
import ru.spbu.astro.commons.StarFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;


public final class HRDiagram {
    public static final double BV_COLOR_LOWER_BOUND = -0.5;
    public static final double BV_COLOR_UPPER_BOUND = 2;
    public static final double ABSOLUTE_MAGNITUDE_LOWER_BOUND = -5;
    public static final double ABSOLUTE_MAGNITUDE_UPPER_BOUND = 15;

    public static final double SCALE = (ABSOLUTE_MAGNITUDE_UPPER_BOUND - ABSOLUTE_MAGNITUDE_LOWER_BOUND) / (BV_COLOR_UPPER_BOUND - BV_COLOR_LOWER_BOUND);

    private static final double ERROR_VIEW_SHARE = 1;

    public HRDiagram(@NotNull final List<Star> stars) {
        final EnumMap<LuminosityClass, List<Star>> lumin2stars = new EnumMap<>(LuminosityClass.class);
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
                final double bv = star.getBVColor().getValue();
                final double dbv = star.getBVColor().getError();
                final double M = star.getAbsoluteMagnitude().getValue();
                final double dM = star.getAbsoluteMagnitude().getError();

                if (Math.random() < ERROR_VIEW_SHARE) {
                    series.add(bv, bv - dbv, bv + dbv, M, M - dM, M + dM);
                } else {
                    series.add(bv, bv, bv, M, M, M);
                }
            }

            System.out.println("#" + lumin + ": " + luminStars.size());

            dataset.addSeries(series);
        }

        final JFreeChart chart = ChartFactory.createScatterPlot(
                String.format("%d звезы", lumin2stars.values().stream().mapToInt(List::size).sum()),
                "B-V [зв. вел.]",
                "M [зв. вел.]",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                true
        );

        chart.getXYPlot().setRenderer(new XYErrorRenderer());

        chart.getXYPlot().getRangeAxis().setInverted(true);

        //for (int i = 0; i < dataset.getSeriesCount(); ++i) {
        //    chart.getXYPlot().getRenderer().setSeriesShape(i, new Ellipse2D.Float(-0.5f, -0.5f, 1, 1));
        //}

        chart.getXYPlot().getDomainAxis().setLowerBound(BV_COLOR_LOWER_BOUND);
        chart.getXYPlot().getDomainAxis().setUpperBound(BV_COLOR_UPPER_BOUND);
        chart.getXYPlot().getRangeAxis().setLowerBound(ABSOLUTE_MAGNITUDE_LOWER_BOUND);
        chart.getXYPlot().getRangeAxis().setUpperBound(ABSOLUTE_MAGNITUDE_UPPER_BOUND);

        final ChartFrame frame = new ChartFrame("Hershprung-Russel diagram", chart);
        frame.pack();
        frame.setVisible(true);

//        ChartUtilities.saveChartAsPNG(new File("documents/presentation/buffer.png"), chart, 1200, 800);
    }

    public static void main(@NotNull final String[] args) throws IOException {
        /*catalogue = new StarSelector(catalogue)
                .selectByBVColor(1.525, 1.95)
                .selectByAbsoluteMagnitude(4.5, 9.5)
                .getCatalogue();*/

        System.out.println(SCALE * 0.01);

        new HRDiagram(new StarFilter(Catalogues.HIPPARCOS_2007)
                .mainLuminosityClasses()
                .absoluteMagnitudeError(SCALE * 0.01)
                .bvColorError(0.01).getStars());
    }
}
