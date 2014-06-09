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
import java.util.Random;

public final class LuminosityClassifier {

    private static final double RELATIVE_ERROR_LIMIT = 0.10;

    private static final List<String> luminosityClasses = Arrays.asList("III", "V");
    private final Classifier classifier;

    public LuminosityClassifier(Catalogue catalogue) {
        this(catalogue, Mode.DEFAULT);
    }

    public LuminosityClassifier(Catalogue catalogue, Mode mode) {
        List<Star> learnStars = new ArrayList<>();

        for (Star s : catalogue.getStars()) {
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
            Evaluation evaluation = new Evaluation(learn);
            evaluation.crossValidateModel(classifier, learn, 10, new Random());

            System.out.println(evaluation.toClassDetailsString());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

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

    public enum Mode {
        DEFAULT, TEST
    }

    public static void main(String[] args) throws FileNotFoundException {
        Catalogue catalogue = new Catalogue("datasets/hipparcos1997.txt");
        catalogue.updateBy(new Catalogue("datasets/hipparcos2007.txt"));
        new LuminosityClassifier(catalogue, Mode.TEST);
    }

}
