package ru.spbu.astro.dust.algo;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.dust.model.Catalogue;
import ru.spbu.astro.dust.model.Star;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public final class LuminosityClassifier {
    private static final double RELATIVE_ERROR_LIMIT = 0.10;
    private static final List<String> luminosityClasses = Arrays.asList("III", "V");

    @NotNull
    private final Classifier classifier;

    public LuminosityClassifier(@NotNull final Catalogue catalogue) {
        this(catalogue, Mode.DEFAULT);
    }

    public LuminosityClassifier(@NotNull final Catalogue catalogue, @NotNull final Mode mode) {
        final List<Star> learnStars = new ArrayList<>();

        for (final Star s : catalogue.getStars()) {
            if (s.parallax.getRelativeError() < RELATIVE_ERROR_LIMIT) {
                String luminosityClass = s.spectralType.getLuminosityClass();
                if (luminosityClasses.contains(luminosityClass)) {
                    learnStars.add(s);
                }
            }
        }

        Instances learn = toInstances("learn", learnStars);
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
    public String getLuminosityClass(Star s) {
        if (s.spectralType.getLuminosityClass() != null) {
            return s.spectralType.getLuminosityClass();
        }

        try {
            int index = (int) classifier.classifyInstance(toInstances(s).get(0));
            return luminosityClasses.get(index);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final ArrayList<Attribute> attributes = new ArrayList<>();
    static {
        attributes.add(new Attribute("bvColor"));
        attributes.add(new Attribute("mag"));
        attributes.add(new Attribute("luminosityClasses", luminosityClasses));
    }

    private static Instance toInstance(Star s) {
        Instance instance = new DenseInstance(attributes.size());
        instance.setValue(attributes.get(0), s.bvColor.value);
        instance.setValue(attributes.get(1), s.getAbsoluteMagnitude().value);
        instance.setValue(attributes.get(2), luminosityClasses.indexOf(s.spectralType.getLuminosityClass()));
        return instance;
    }

    private static Instances toInstances(String name, List<Star> stars) {
        Instances instances = new Instances(name, attributes, stars.size());
        instances.setClassIndex(2);

        for (Star s : stars) {
            instances.add(toInstance(s));
        }

        return instances;
    }

    private static Instances toInstances(Star s) {
        Instances instances = new Instances("predicted", attributes, 1);
        instances.setClassIndex(2);

        instances.add(toInstance(s));

        return instances;
    }

    public static enum Mode {
        DEFAULT, TEST
    }

    public static void main(String[] args) throws FileNotFoundException {
        Catalogue catalogue = new Catalogue("datasets/hipparcos1997.txt");
        catalogue.updateBy(new Catalogue("datasets/hipparcos2007.txt"));
        new LuminosityClassifier(catalogue, Mode.TEST);
    }

}
