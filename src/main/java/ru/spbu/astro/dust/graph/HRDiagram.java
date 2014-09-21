package ru.spbu.astro.dust.graph;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;
import ru.spbu.astro.dust.model.Catalogue;
import ru.spbu.astro.dust.model.Star;
import ru.spbu.astro.dust.util.StarSelector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import static ru.spbu.astro.dust.model.SpectralType.LuminosityClass;

public final class HRDiagram {
    private static final double PARALLAX_RELATIVE_ERROR_LIMIT = 0.10;
    private static final double ERROR = 2.5 * Math.log10((1 + PARALLAX_RELATIVE_ERROR_LIMIT) / (1 - PARALLAX_RELATIVE_ERROR_LIMIT));

    private static final double BV_COLOR_ERROR_LIMIT = Double.MAX_VALUE;

    private static final double ERROR_VIEW_SHARE = 0.0;

    public HRDiagram(final Catalogue catalogue) throws IOException {
        final EnumMap<LuminosityClass, List<Star>> class2stars = new EnumMap<>(LuminosityClass.class);
        for (final LuminosityClass luminosityClass : LuminosityClass.values()) {
            class2stars.put(luminosityClass, new ArrayList<>());
        }

        final Catalogue selection = new StarSelector(catalogue)
                .selectByParallaxRelativeError(PARALLAX_RELATIVE_ERROR_LIMIT)
                .selectByBVColorError(BV_COLOR_ERROR_LIMIT)
                .selectByExistLuminosityClass().getCatalogue();

        int starsCount = 0;
        for (final Star star : selection.getStars()) {
            class2stars.get(star.getSpectralType().getLuminosityClass()).add(star);
            starsCount++;
        }

        final XYIntervalSeriesCollection dataset = new XYIntervalSeriesCollection();
        for (final List<Star> stars : class2stars.values()) {
            final XYIntervalSeries series = getLuminosityClassSeries(stars);
            if (series != null) {
                dataset.addSeries(series);
            }
        }

        final JFreeChart chart = ChartFactory.createScatterPlot(
                String.format("%d (dr < %d%%), ±%.2f mag", starsCount, (int) (100 * PARALLAX_RELATIVE_ERROR_LIMIT), ERROR),
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

        ChartUtilities.saveChartAsPNG(new File("documents/presentation/buffer.png"), chart, 1200, 800);
    }

    @Nullable
    private XYIntervalSeries getLuminosityClassSeries(@NotNull final List<Star> stars) {
        if (stars.isEmpty()) {
            return null;
        }
        final LuminosityClass luminosityClass = stars.get(0).getSpectralType().getLuminosityClass();

        final XYIntervalSeries series = new XYIntervalSeries(luminosityClass);

        for (final Star s : stars) {
            double bv = s.getBVColor().value;
            double dbv = s.getBVColor().error;
            double M = s.getAbsoluteMagnitude().value;
            double dM = s.getAbsoluteMagnitude().error;

            if (Math.random() < ERROR_VIEW_SHARE) {
                series.add(bv, bv - dbv, bv + dbv, M, M - dM, M + dM);
            } else {
                series.add(bv, bv, bv, M, M, M);
            }
        }

        System.out.println("#" + luminosityClass + ": " + stars.size());

        return series;
    }

    public static void main(String[] args) throws IOException {
        Catalogue catalogue = new Catalogue("/catalogues/hipparcos1997.txt");
        catalogue.updateBy(new Catalogue("/catalogues/hipparcos2007.txt"));
        //catalogue.updateBy(new LuminosityClassifier(catalogue, LuminosityClassifier.Mode.TEST));

        /*catalogue = new StarSelector(catalogue)
                .selectByBVColor(1.525, 1.95)
                .selectByAbsoluteMagnitude(4.5, 9.5)
                .getCatalogue();*/

        new HRDiagram(catalogue);
    }

}
