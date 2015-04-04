package ru.spbu.astro.dust.algo.classify;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.commons.spect.LuminosityClass;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Collectors;

final class SVMLuminosityClassifier implements LuminosityClassifier {
    private static final Logger LOGGER = Logger.getLogger(SVMLuminosityClassifier.class.getName());

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
    @NotNull
    private final Star[] stars;

    SVMLuminosityClassifier(@NotNull final Star[] stars) {
        this(stars, Mode.DEFAULT);
    }

    SVMLuminosityClassifier(@NotNull final Star[] stars, @NotNull final Mode mode) {
        final Instances dataset = toInstances("dataset", this.stars = stars);

        classifier = new SMO();
//        classifier.setFilterType(new SelectedTag(SMO.FILTER_NONE, SMO.TAGS_FILTER));
        try {
            classifier.buildClassifier(dataset);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        LOGGER.info(classifier.toString());

        if (mode == Mode.DEFAULT) {
            return;
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

    double getA() {
        return classifier.sparseWeights()[0][1][0];
    }
    
    double getB() {
        return classifier.sparseWeights()[0][1][1];
    }
    
    double getC() {
        return -classifier.bias()[0][1];
    }

    @NotNull
    private static Instances toInstances(@NotNull final String name, @NotNull final Star[] stars) {
        return new Instances(name, ATTRIBUTES, stars.length) {{
            setClassIndex(ATTRIBUTES.size() - 1);
            addAll(Arrays.stream(stars).map(star -> new DenseInstance(ATTRIBUTES.size()) {{
                setValue(ATTRIBUTES.get(0), star.getBVColor().getValue());
                setValue(ATTRIBUTES.get(1), star.getAbsMag().getValue());
                setValue(ATTRIBUTES.get(ATTRIBUTES.size() - 1), ArrayUtils.indexOf(LuminosityClass.MAIN, star.getSpectType().getLumin()));
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
}