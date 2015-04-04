package ru.spbu.astro.dust.algo.classify;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;
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

    @NotNull
    private final ArrayList<Attribute> attributes = new ArrayList<Attribute>() {{
        add(new Attribute("bv color"));
        add(new Attribute("mag"));
    }};

    @NotNull
    private final LuminosityClass[] lumins;

    @NotNull
    private final SMO classifier;
    @NotNull
    private final Star[] stars;

    SVMLuminosityClassifier(@NotNull final Star[] stars) {
        this(stars, Mode.DEFAULT);
    }

    SVMLuminosityClassifier(@NotNull final Star[] stars, @NotNull final Mode mode) {
        lumins = Arrays.stream(stars)
                .map(star -> star.getSpectType().getLumin())
                .collect(Collectors.toSet()).stream().sorted().toArray(LuminosityClass[]::new);
        attributes.add(new Attribute("lumin", Arrays.stream(lumins).map(LuminosityClass::name).collect(Collectors.toList())));

        final Instances dataset = toInstances("dataset", this.stars = stars);
        classifier = new SMO();

        try {
            classifier.buildClassifier(dataset);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (mode == Mode.DEFAULT) {
            return;
        }

        LOGGER.info(classifier.toString());

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
    private Instances toInstances(@NotNull final String name, @NotNull final Star[] stars) {
        return new Instances(name, attributes, stars.length) {{
            setClassIndex(attributes.size() - 1);
            addAll(Arrays.stream(stars).map(star -> new DenseInstance(attributes.size()) {{
                setValue(attributes.get(0), star.getBVColor().getValue());
                setValue(attributes.get(1), star.getAbsoluteMagnitude().getValue());
                setValue(attributes.get(attributes.size() - 1), ArrayUtils.indexOf(lumins, star.getSpectType().getLumin()));
            }}).collect(Collectors.toList()));
        }};
    }

    @NotNull
    @TestOnly
    Star[] getStars() {
        return stars;
    }

    @TestOnly
    double getA() {
        return classifier.sparseWeights()[0][1][0];
    }

    @TestOnly
    double getB() {
        return classifier.sparseWeights()[0][1][1];
    }

    @TestOnly
    double getC() {
        return -classifier.bias()[0][1];
    }

    @NotNull
    @Override
    public LuminosityClass classify(@NotNull final Star star) {
        try {
            final int index = (int) classifier.classifyInstance(toInstances("predicted", new Star[]{star}).get(0));
            return lumins[index];
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public enum Mode {
        DEFAULT, TEST
    }
}