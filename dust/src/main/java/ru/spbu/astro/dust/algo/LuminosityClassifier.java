package ru.spbu.astro.dust.algo;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.core.Star;
import ru.spbu.astro.core.spect.LuminosityClass;
import ru.spbu.astro.dust.model.Catalogues;
import ru.spbu.astro.dust.util.StarSelector;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

import java.util.*;
import java.util.stream.Collectors;

import static ru.spbu.astro.dust.graph.HRDiagram.SCALE;

public final class LuminosityClassifier {
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
    private final Classifier classifier;

    public LuminosityClassifier(@NotNull final List<Star> stars) {
        this(stars, Mode.DEFAULT);
    }

    public LuminosityClassifier(@NotNull final List<Star> stars, @NotNull final Mode mode) {
        final Instances learn = toInstances("learn", new StarSelector(stars)
                .mainLuminosityClasses()
                .absoluteMagnitudeError(SCALE * BV_COLOR_ERROR_LIMIT)
                .bvColorError(BV_COLOR_ERROR_LIMIT).getStars());

        classifier = new SMO();
        try {
            classifier.buildClassifier(learn);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (mode == Mode.DEFAULT) {
            return;
        }

        try {
            final Evaluation evaluation = new Evaluation(learn);
            evaluation.crossValidateModel(classifier, learn, 10, new Random(0));

            System.out.println(evaluation.toSummaryString());
            System.out.println(evaluation.toMatrixString());
            System.out.println(evaluation.toClassDetailsString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private static Instances toInstances(@NotNull final String name, @NotNull final List<Star> stars) {
        return new Instances(name, ATTRIBUTES, stars.size()) {{
            setClassIndex(2);
            addAll(stars.stream().map(star -> new DenseInstance(ATTRIBUTES.size()) {{
                setValue(ATTRIBUTES.get(0), star.getBVColor().getValue());
                setValue(ATTRIBUTES.get(1), star.getAbsoluteMagnitude().getValue());
                setValue(ATTRIBUTES.get(2), ArrayUtils.indexOf(LuminosityClass.MAIN, star.getSpectType().getLumin()));
            }}).collect(Collectors.toList()));
        }};
    }

    public static void main(String[] args) {
        new LuminosityClassifier(Catalogues.HIPPARCOS_2007.getStars(), Mode.TEST);
    }

    @NotNull
    public LuminosityClass classify(@NotNull final Star star) {
        try {
            final int index = (int) classifier.classifyInstance(toInstances("predicted", Collections.singletonList(star)).get(0));
            return LuminosityClass.MAIN[index];
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static enum Mode {
        DEFAULT, TEST
    }
}