package ru.spbu.astro.dust.algo.classify;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.SamplingXYLineRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.commons.StarFilter;
import ru.spbu.astro.commons.Stars;
import ru.spbu.astro.commons.graph.HRDiagram;
import ru.spbu.astro.commons.spect.LuminosityClass;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static ru.spbu.astro.commons.graph.HRDiagram.*;

final class SVMLuminosityClassifier implements LuminosityClassifier {
    private static final Logger LOGGER = Logger.getLogger(SVMLuminosityClassifier.class.getName());

    private static final double BV_COLOR_ERROR_LIMIT = 0.01;

    private static final ArrayList<Attribute> ATTRIBUTES = new ArrayList<Attribute>() {{
        add(new Attribute("bv color"));
        add(new Attribute("mag"));
        add(new Attribute(
                "luminosity classes",
                Arrays.stream(LuminosityClass.MAIN).map(LuminosityClass::name).collect(Collectors.toList())
        ));
    }};

    @NotNull
    private final SMO classifier;
    

    public SVMLuminosityClassifier(@NotNull final Star[] stars) {
        this(stars, Mode.DEFAULT);
    }

    public SVMLuminosityClassifier(@NotNull final Star[] stars, @NotNull final Mode mode) {
        final Instances dataset = toInstances("dataset", StarFilter.of(stars)
                .mainLuminosityClasses()
                .absoluteMagnitudeError(SCALE * BV_COLOR_ERROR_LIMIT)
                .bvColorError(BV_COLOR_ERROR_LIMIT).stars());

        classifier = new SMO();
//        classifier.setFilterType(new SelectedTag(SMO.FILTER_NONE, SMO.TAGS_FILTER));
        try {
            classifier.buildClassifier(dataset);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (mode == Mode.DEFAULT) {
            return;
        }

        System.out.println(classifier.toString());
        System.out.println(Arrays.deepToString(classifier.bias()));

        try {
            final Evaluation evaluation = new Evaluation(dataset);
            evaluation.crossValidateModel(classifier, dataset, 10, new Random(0));

            LOGGER.info(evaluation.toSummaryString());
            LOGGER.info(evaluation.toMatrixString());
            LOGGER.info(evaluation.toClassDetailsString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private double getA() {
        return classifier.sparseWeights()[0][1][0];
    }
    
    private double getB() {
        return classifier.sparseWeights()[0][1][1];
    }
    
    private double getC() {
        return -classifier.bias()[0][1];
    }

    @NotNull
    private static Instances toInstances(@NotNull final String name, @NotNull final Star[] stars) {
        return new Instances(name, ATTRIBUTES, stars.length) {{
            setClassIndex(2);
            addAll(Arrays.stream(stars).map(star -> new DenseInstance(ATTRIBUTES.size()) {{
                setValue(ATTRIBUTES.get(0), star.getBVColor().getValue());
                setValue(ATTRIBUTES.get(1), star.getAbsoluteMagnitude().getValue());
                setValue(ATTRIBUTES.get(2), ArrayUtils.indexOf(LuminosityClass.MAIN, star.getSpectType().getLumin()));
            }}).collect(Collectors.toList()));
        }};
    }

    @NotNull
    @Override
    public LuminosityClass classify(@NotNull final Star star) {
        try {
            final int index = (int) classifier.classifyInstance(toInstances("predicted", new Star[]{star}).get(0));
            return LuminosityClass.MAIN[index];
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public enum Mode {
        DEFAULT, TEST
    }

    public static void main(@NotNull final String[] args) {
        final Star[] allStars = StarFilter.of(Stars.ALL).mainLuminosityClasses().stars();
        final Star[] stars = StarFilter.of(allStars)
                .absoluteMagnitudeError(SCALE * BV_COLOR_ERROR_LIMIT)
                .bvColorError(BV_COLOR_ERROR_LIMIT)
                .bvColor(BV_COLOR_LOWER_BOUND, 0.6)
//                .bvColor(0.6, BV_COLOR_UPPER_BOUND)
                .stars();
        final SVMLuminosityClassifier classifier = new SVMLuminosityClassifier(stars, Mode.TEST);
        
        final HRDiagram diagram = new HRDiagram(stars);
        final XYPlot plot = diagram.getPlot();
        final XYSeriesCollection seriesCollection = new XYSeriesCollection();
        seriesCollection.addSeries(create("Разделяющая прямая", -2.8388, 0.5055, 1.0056)); // all
//        seriesCollection.addSeries(create("Разделяющая прямая", -3.9761, 0.668, 1.377)); // > 0.6
//        seriesCollection.addSeries(create("Разделяющая прямая (new)", classifier.getA(), classifier.getB(), classifier.getC()));
        plot.setDataset(1, seriesCollection);
        final XYItemRenderer renderer = new SamplingXYLineRenderer();
        renderer.setSeriesStroke(0, new BasicStroke(3));
        renderer.setSeriesStroke(1, new BasicStroke(3));
        plot.setRenderer(1, renderer);
        diagram.show();
    }
    
    private static XYSeries create(@NotNull final String name, final double a, final double b, final double c) {
        final XYSeries series = new XYSeries(name);
        final double x1 = BV_COLOR_LOWER_BOUND;
        series.add(x1, -(c + a * x1) / b);
        final double x2 = BV_COLOR_UPPER_BOUND;
        series.add(x2, -(c + a * x2) / b);
        return series;
    }
}