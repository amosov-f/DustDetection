package ru.spbu.astro.dust.graph;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;
import ru.spbu.astro.dust.util.StarSelector;
import ru.spbu.astro.dust.model.Catalogue;
import ru.spbu.astro.dust.model.SpectralType;
import ru.spbu.astro.dust.model.Star;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class HRDiagram {

    private static final double PARALLAX_RELATIVE_ERROR_LIMIT = 0.10;
    private static final double ERROR = 2.5 * Math.log10((1 + PARALLAX_RELATIVE_ERROR_LIMIT) / (1 - PARALLAX_RELATIVE_ERROR_LIMIT));

    private static final double BV_COLOR_ERROR_LIMIT = Double.MAX_VALUE;

    private static final double ERROR_VIEW_SHARE = 0.0;

    public HRDiagram(final Catalogue catalogue) throws IOException {
        Map<String, List<Star>> class2stars = new HashMap<>();
        for (String luminosityClass : SpectralType.parseLuminosityClasses) {
            class2stars.put(luminosityClass, new ArrayList<>());
        }

        Catalogue selection = new StarSelector(catalogue)
                .selectByParallaxRelativeError(PARALLAX_RELATIVE_ERROR_LIMIT)
                .selectByBVColorError(BV_COLOR_ERROR_LIMIT)
                .selectByExistLuminosityClass()
                .getCatalogue();

        int starsCount = 0;
        for (Star s : selection.getStars()) {
            class2stars.get(s.spectralType.getLuminosityClass()).add(s);
            starsCount++;
        }

        XYIntervalSeriesCollection dataset = new XYIntervalSeriesCollection();
        for (final List<Star> stars : class2stars.values()) {
            XYIntervalSeries series = getLuminosityClassSeries(stars);
            if (series != null) {
                dataset.addSeries(series);
            }
        }

        JFreeChart chart = ChartFactory.createScatterPlot(
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



        //ChartFrame frame = new ChartFrame("Hershprung-Russel diagram", chart);
        //frame.pack();
        //frame.setVisible(true);

        ChartUtilities.saveChartAsPNG(new File("documents/presentation/ml-1.png"), chart, 1200, 800);
    }

    private XYIntervalSeries getLuminosityClassSeries(List<Star> stars) {
        if (stars.isEmpty()) {
            return null;
        }
        String luminosityClass = stars.get(0).spectralType.getLuminosityClass();

        XYIntervalSeries series = new XYIntervalSeries(luminosityClass);

        for (Star s : stars) {
            double bv = s.bvColor.value;
            double dbv = s.bvColor.error;
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
        Catalogue catalogue = new Catalogue("datasets/hipparcos1997.txt");
        catalogue.updateBy(new Catalogue("datasets/hipparcos2007.txt"));
        //catalogue.updateBy(new LuminosityClassifier(catalogue));

        /*catalogue = new StarSelector(catalogue)
                .selectByBVColor(1.525, 1.95)
                .selectByAbsoluteMagnitude(4.5, 9.5)
                .getCatalogue();*/

        new HRDiagram(catalogue);
    }

}
