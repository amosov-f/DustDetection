package ru.spbu.astro.dust.algo;

import ru.spbu.astro.dust.model.Catalogue;
import ru.spbu.astro.dust.model.SpectralType;
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

import static ru.spbu.astro.dust.model.Catalogue.Row;

public class LuminosityClassifier {

    private static final double LEARN_SHARE = 0.8;
    private static final List<String> luminosityClasses = Arrays.asList("III", "IV", "V");

    public static class Star {
        public final int id;
        public final double bvColor;
        public final String luminosityClass;
        public final double parallax;
        public final double mag;

        public Star(final Row row) {
            id = row.id;
            bvColor = new Double(row.get("bv_color"));

            String luminosityClass;
            try {
                luminosityClass = new SpectralType(row.get("spect_type")).getLuminosityClass();
            } catch (IllegalArgumentException e) {
                luminosityClass = null;
            }
            this.luminosityClass = luminosityClass;

            double vMag = new Double(row.get("vmag"));
            parallax = new Double(row.get("parallax"));
            mag = vMag + 5 * Math.log10(parallax) - 10;
        }

        /*private Instance toInstance() {
            Instance instance = new DenseInstance(3);
            instance.setValue(0, bvColor);
            instance.setValue(1, mag);
            if (luminosityClass != null) {
                instance.setValue(2, luminosityClass);
            }
            return instance;
        }   */
    }


    public LuminosityClassifier(final Catalogue catalogue) {
        final List<Star> learnStars = new ArrayList<>();
        final List<Star> validateStars = new ArrayList<>();
        final List<Star> testStars = new ArrayList<>();

        for (final Row row : catalogue) {
            final Star s = new Star(row);
            if (s.parallax > 10) {
                if (s.luminosityClass != null && luminosityClasses.contains(s.luminosityClass)) {
                    if (Math.random() < LEARN_SHARE) {
                        learnStars.add(s);

                    } else {
                        validateStars.add(s);
                    }
                }
                if (s.luminosityClass == null) {
                    testStars.add(s);
                }
            }

        }




        Instances learn = toInstances("learn", learnStars);
        Instances validate = toInstances("validate", validateStars);
        Instances test = toInstances("test", testStars);

        Classifier classifier = new SMO();
        try {
            classifier.buildClassifier(learn);

            Evaluation evaluation = new Evaluation(learn);
            evaluation.evaluateModel(classifier, validate);
            System.out.println(evaluation.toSummaryString());

            /*for (final Instance instance : test) {
                System.out.println(
                        //SpectralType.parseLuminosityClasses.get((int) instance.classValue()) + " " +
                        SpectralType.parseLuminosityClasses.get((int) classifier.classifyInstance(instance))
                );
            } */

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        System.out.println("learn: " + learnStars.size());
        System.out.println("validate: " + validateStars.size());
        System.out.println("test: " + testStars.size());
    }


    private static final ArrayList<Attribute> attributes = new ArrayList<>();
    static {
        attributes.add(new Attribute("bvColor"));
        attributes.add(new Attribute("mag"));
        attributes.add(new Attribute("luminosityClasses", luminosityClasses));
    }

    Instances toInstances(final String name, final List<Star> stars) {
        Instances instances = new Instances(name, attributes, stars.size());
        instances.setClassIndex(2);

        for (final Star s : stars) {
            Instance instance = new DenseInstance(instances.numAttributes());
            instance.setValue(attributes.get(0), s.bvColor);
            instance.setValue(attributes.get(1), s.mag);
            instance.setValue(attributes.get(2), luminosityClasses.indexOf(s.luminosityClass));

            instances.add(instance);
        }

        return instances;
    }

    public static void main(final String[] args) throws FileNotFoundException {
        final Catalogue catalogue = new Catalogue("datasets/hipparcos1997.txt").updateBy(new Catalogue("datasets/hipparcos2007.txt"));
        new LuminosityClassifier(catalogue);
    }

}
