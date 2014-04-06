package ru.spbu.astro.dust.algo;

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

public final class LuminosityClassifier {

    private static final double LEARN_SHARE = 0.8;
    private static final double RELATIVE_ERROR_LIMIT = 0.10;


    private static final List<String> luminosityClasses = Arrays.asList("III", "V");
    private final Classifier classifier;

    public LuminosityClassifier(final Catalogue catalogue) {
        final List<Star> learnStars = new ArrayList<>();
        final List<Star> testStars = new ArrayList<>();

        for (final Star s : catalogue.getStars()) {
            if (s.parallax.getRelativeError() < RELATIVE_ERROR_LIMIT) {
                final String luminosityClass = s.spectralType.getLuminosityClass();
                if (luminosityClass != null && luminosityClasses.contains(luminosityClass)) {
                    if (Math.random() < LEARN_SHARE) {
                        learnStars.add(s);
                    } else {
                        testStars.add(s);
                    }
                }
            }
        }

        Instances learn = toInstances("learn", learnStars);
        Instances test = toInstances("test", testStars);

        classifier = new SMO();
        try {
            classifier.buildClassifier(learn);

            Evaluation evaluation = new Evaluation(learn);
            evaluation.evaluateModel(classifier, test);
            System.out.println(evaluation.toSummaryString());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        System.out.println("learn: " + learnStars.size());
        System.out.println("test: " + testStars.size());
    }

    public String getLuminosityClass(final Star s) {
        if (s.spectralType.getLuminosityClass() != null) {
            return s.spectralType.getLuminosityClass();
        }

        try {
            int indexUp = (int) classifier.classifyInstance(toInstances("predicted", Arrays.asList(s), -2).get(0));
            int indexDown = (int) classifier.classifyInstance(toInstances("predicted", Arrays.asList(s), 2).get(0));

            if (indexUp == indexDown) {
                return luminosityClasses.get(indexUp);
            }
            return null;

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

    private static Instance toInstance(final Star s) {
        return toInstance(s, 0);
    }

    private static Instance toInstance(final Star s, int shift) {
        Instance instance = new DenseInstance(attributes.size());
        instance.setValue(attributes.get(0), s.bvColor.value);
        instance.setValue(attributes.get(1), s.getAbsoluteMagnitude().value + shift * s.getAbsoluteMagnitude().error);
        instance.setValue(attributes.get(2), luminosityClasses.indexOf(s.spectralType.getLuminosityClass()));
        return instance;
    }

    private static Instances toInstances(final String name, final List<Star> stars) {
        return toInstances(name, stars, 0);
    }

    private static Instances toInstances(final String name, final List<Star> stars, int shift) {
        Instances instances = new Instances(name, attributes, stars.size());
        instances.setClassIndex(2);

        for (final Star s : stars) {
            instances.add(toInstance(s, shift));
        }

        return instances;
    }

    public static void main(final String[] args) throws FileNotFoundException {
        final Catalogue catalogue = new Catalogue("datasets/hipparcos1997.txt");
        catalogue.updateBy(new Catalogue("datasets/hipparcos2007.txt"));
        new LuminosityClassifier(catalogue);
    }

}
