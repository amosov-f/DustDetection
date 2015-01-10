package ru.spbu.astro.dust.graph;

import org.jetbrains.annotations.NotNull;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;
import ru.spbu.astro.core.Star;
import ru.spbu.astro.core.spect.LuminosityClass;
import ru.spbu.astro.dust.model.Catalogue;
import ru.spbu.astro.dust.util.StarSelector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;


public final class HRDiagram {
    private static final double PARALLAX_RELATIVE_ERROR_LIMIT = 0.20;
    private static final double ERROR = 2.5 * Math.log10((1 + PARALLAX_RELATIVE_ERROR_LIMIT) / (1 - PARALLAX_RELATIVE_ERROR_LIMIT));
    private static final double BV_COLOR_ERROR_LIMIT = Double.MAX_VALUE;

    private static final double ERROR_VIEW_SHARE = 0;

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
                double bv = star.getBVColor().getValue();
                double dbv = star.getBVColor().getError();
                double M = star.getAbsoluteMagnitude().getValue();
                double dM = star.getAbsoluteMagnitude().getError();

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
                String.format("%d звезд", lumin2stars.values().stream().mapToInt(List::size).sum()),
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

        chart.getXYPlot().getDomainAxis().setLowerBound(-0.5);
        chart.getXYPlot().getDomainAxis().setUpperBound(2.0);
        chart.getXYPlot().getRangeAxis().setLowerBound(-5);
        chart.getXYPlot().getRangeAxis().setUpperBound(15);

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

        new HRDiagram(new StarSelector(Catalogue.HIPPARCOS_2007)
                .luminosityClasses(LuminosityClass.MAIN)
                .parallaxRelativeError(0.1)
                        //.selectByBVColorError(BV_COLOR_ERROR_LIMIT)
                        //.selectBySpectralType(SpectralType.TypeSymbol.M, 5, 9)
                .getStars());
    }

}
