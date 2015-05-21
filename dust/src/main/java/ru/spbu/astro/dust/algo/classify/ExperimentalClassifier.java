package ru.spbu.astro.dust.algo.classify;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.commons.StarFilter;
import ru.spbu.astro.commons.Stars;
import ru.spbu.astro.commons.spect.LuminosityClass;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.Bagging;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.ToDoubleFunction;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * User: amosov-f
 * Date: 03.05.15
 * Time: 1:23
 */
final class ExperimentalClassifier implements LuminosityClassifier {
    private static final Logger LOGGER = Logger.getLogger(ExperimentalClassifier.class.getName());


    private static final List<StarAttribute> STAR_ATTRIBUTES = Arrays.asList(
            new StarAttribute("bv color", star -> star.getBVColor().val()),
            new StarAttribute("mag", star -> star.getAbsMag().val()),
//            new StarAttribute("res", star -> star.getBVColor().val() - calculator.getSlope(star.getDir()).val() * star.getR().val())
//            new StarAttribute("r", star -> star.getR().val()),
//            new StarAttribute("p", star -> star.getParallax().val()),
//            new StarAttribute("dr", star -> star.getR().err()),
            new StarAttribute("vmag", Star::getVMag)
    );

    private static final ArrayList<Attribute> ATTRIBUTES = new ArrayList<Attribute>() {{
        addAll(STAR_ATTRIBUTES.stream().map(StarAttribute::toAttribute).collect(Collectors.toList()));
        add(new Attribute(
                "luminosity classes",
                Arrays.stream(LuminosityClass.MAIN).map(LuminosityClass::name).collect(Collectors.toList())
        ));
    }};

    @NotNull
    private final Classifier classifier;
    @NotNull
    private final Star[] stars;

    ExperimentalClassifier(@NotNull final Star[] stars) {
        this(stars, Mode.DEFAULT);
    }

    ExperimentalClassifier(@NotNull final Star[] stars, @NotNull final Mode mode) {
        final Instances dataset = toInstances("dataset", this.stars = stars);

        classifier = new Bagging()  ;
//        classifier.setFilterType(new SelectedTag(SMO.FILTER_NONE, SMO.TAGS_FILTER));
        try {
            classifier.buildClassifier(dataset);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        LOGGER.info(classifier.toString());

        if (mode == Mode.DEFAULT) {
//            return;
        }

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

    @NotNull
    Star[] getStars() {
        return stars;
    }

//    double getA() {
//        return classifier.sparseWeights()[0][1][0];
//    }
//
//    double getB() {
//        return classifier.sparseWeights()[0][1][1];
//    }
//
//    double getC() {
//        return -classifier.bias()[0][1];
//    }

    @NotNull
    private static Instances toInstances(@NotNull final String name, @NotNull final Star[] stars) {
        final Instances instances = new Instances(name, ATTRIBUTES, stars.length);
        instances.setClassIndex(ATTRIBUTES.size() - 1);
        instances.addAll(Arrays.stream(stars).map(star -> {
            final Instance instance = new DenseInstance(ATTRIBUTES.size());
            for (int i = 0; i < ATTRIBUTES.size() - 1; i++) {
                instance.setValue(ATTRIBUTES.get(i), STAR_ATTRIBUTES.get(i).apply(star));
            }
            instance.setValue(
                    ATTRIBUTES.get(ATTRIBUTES.size() - 1),
                    ArrayUtils.indexOf(LuminosityClass.MAIN, star.getSpectType().getLumin())
            );
            return instance;
        }).collect(Collectors.toList()));
        return instances;
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

    private static class StarAttribute {
        @NotNull
        private final String name;
        @NotNull
        private final ToDoubleFunction<Star> f;

        StarAttribute(@NotNull final String name, @NotNull final ToDoubleFunction<Star> f) {
            this.name = name;
            this.f = f;
        }

        @NotNull
        Attribute toAttribute() {
            return new Attribute(name);
        }

        double apply(@NotNull final Star star) {
            return f.applyAsDouble(star);
        }
    }

    public static void main(String[] args) {
        new ExperimentalClassifier(StarFilter.of(Stars.ALL).mainLumin().bv(0.3, 0.6).stars(), Mode.TEST);
    }
}
