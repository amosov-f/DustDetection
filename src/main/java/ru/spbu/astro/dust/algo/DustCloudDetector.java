package ru.spbu.astro.dust.algo;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import ru.spbu.astro.dust.func.HealpixCounter;
import ru.spbu.astro.dust.graph.HammerProjection;
import ru.spbu.astro.dust.model.Catalogue;
import ru.spbu.astro.dust.model.Spheric;
import ru.spbu.astro.dust.model.Star;
import ru.spbu.astro.dust.util.StarSelector;
import weka.classifiers.functions.LinearRegression;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.neighboursearch.KDTree;
import weka.core.neighboursearch.NearestNeighbourSearch;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ru.spbu.astro.dust.util.Geom.abs;
import static ru.spbu.astro.dust.util.Geom.dotProduct;

public class DustCloudDetector {

    private static final int K = 25;

    private static final double DUST_THRESHOLD = 0.005;

    private final List<double[]> dust = new ArrayList<>();

    public DustCloudDetector(List<Star> stars) {

        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("x"));
        attributes.add(new Attribute("y"));
        attributes.add(new Attribute("z"));
        attributes.add(new Attribute("ext"));


        Instances instances = new Instances("knn", attributes, stars.size());
        instances.setClassIndex(3);

        for (Star s : stars) {
            Instance instance = toInstance(s.getCartesian());
            instance.setValue(3, s.getExtinction().getValue());

            instances.add(instance);
        }

        NearestNeighbourSearch search = new KDTree();
        try {
            search.setInstances(instances);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        for (int t = 0; t < stars.size(); ++t) {
            double[] r = stars.get(t).getCartesian();

            OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
            Instances knn;

            LinearRegression linearRegression = new LinearRegression();
            try {
                knn = search.kNearestNeighbours(instances.get(t), K);
                linearRegression.buildClassifier(knn);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            double[] y = new double[K];
            double[][] x = new double[K][3];
            for (int i = 0; i < K; ++i) {
                y[i] = knn.get(i).classValue();
                for (int j = 0; j < 3; ++j) {
                    x[i][j] = knn.get(i).value(j);
                }
            }

            regression.newSampleData(y, x);

            //System.out.println(Arrays.toString(regression.estimateRegressionParameters()));

            double[] w = Arrays.copyOfRange(regression.estimateRegressionParameters(), 1, 4);

            double scale = dotProduct(w, r) / abs(r);

            if (scale > DUST_THRESHOLD) {
                dust.add(r);
            }
        }
    }

    private static Instance toInstance(double[] p) {
        Instance instance = new DenseInstance(4);
        for (int i = 0; i < p.length; ++i) {
            instance.setValue(i, p[i]);
        }
        return instance;
    }

    public static void main(String[] args) throws FileNotFoundException {
        Catalogue catalogue = Catalogue.HIPPARCOS_UPDATED;

        DustCloudDetector detector = new DustCloudDetector(new StarSelector(catalogue).selectByParallaxRelativeError(0.25).getStars());

        List<Spheric> dirs = new ArrayList<>();
        for (double[] p : detector.dust) {
            dirs.add(new Spheric(p));
        }

        new HammerProjection(new HealpixCounter(dirs, 18)).setVisible(true);
    }

}