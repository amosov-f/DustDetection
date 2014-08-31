package ru.spbu.astro.dust.algo;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.dust.func.HealpixCounter;
import ru.spbu.astro.dust.graph.HammerProjection;
import ru.spbu.astro.dust.model.Catalogue;
import ru.spbu.astro.dust.model.Spheric;
import ru.spbu.astro.dust.model.Star;
import ru.spbu.astro.dust.util.StarSelector;
import weka.classifiers.functions.LinearRegression;
import weka.core.*;
import weka.core.neighboursearch.KDTree;
import weka.core.neighboursearch.NearestNeighbourSearch;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ru.spbu.astro.dust.util.Geom.abs;
import static ru.spbu.astro.dust.util.Geom.dotProduct;

public final class DustCloudDetector {
    private static final int K = 25;
    private static final double DUST_THRESHOLD = 0.008;
    private static final double MAX_RANGE = 81.37697923079452;

    @NotNull
    private final List<double[]> dust = new ArrayList<>();

    public DustCloudDetector(@NotNull final Catalogue catalogue) {
        final ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("x"));
        attributes.add(new Attribute("y"));
        attributes.add(new Attribute("z"));
        attributes.add(new Attribute("ext"));

        final List<Star> stars = catalogue.getStars();

        final Instances instances = new Instances("knn", attributes, stars.size());
        instances.setClassIndex(3);

        for (final Star s : stars) {
            final Instance instance = toInstance(s.getCartesian());
            instance.setValue(3, s.getExtinction().value);

            instances.add(instance);
        }

        final NearestNeighbourSearch search = new KDTree();
        final EuclideanDistance d = new EuclideanDistance();
        d.setDontNormalize(true);
        try {
            search.setDistanceFunction(d);
            search.setInstances(instances);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

//        final List<Double> ranges = new ArrayList<>();


        for (int t = 0; t < stars.size(); ++t) {
            final double[] r = stars.get(t).getCartesian();

            final OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
            final Instances knn;

            final LinearRegression linearRegression = new LinearRegression();
            try {
                knn = search.kNearestNeighbours(instances.get(t), K);
                final double[] distances = search.getDistances();
                final double range = distances[distances.length - 1];
                //ranges.add(range);
                if (range > MAX_RANGE) {
                    continue;
                }
                linearRegression.buildClassifier(knn);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }


            final double[] y = new double[K];
            final double[][] x = new double[K][3];
            for (int i = 0; i < K; ++i) {
                y[i] = knn.get(i).classValue();
                for (int j = 0; j < 3; ++j) {
                    x[i][j] = knn.get(i).value(j);
                }
            }

            regression.newSampleData(y, x);

            //System.out.println(Arrays.toString(regression.estimateRegressionParameters()));

            final double[] w = Arrays.copyOfRange(regression.estimateRegressionParameters(), 1, 4);

            final double scale = dotProduct(w, r) / abs(r);

            if (scale > DUST_THRESHOLD) {
                dust.add(r);
            }
        }

        //System.out.println(new Mean().evaluate(Doubles.toArray(ranges)) + 3 * new StandardDeviation().evaluate(Doubles.toArray(ranges)));
    }

    @NotNull
    private static Instance toInstance(double[] p) {
        final Instance instance = new DenseInstance(4);
        for (int i = 0; i < p.length; ++i) {
            instance.setValue(i, p[i]);
        }
        return instance;
    }

    public static void main(final String[] args) throws FileNotFoundException {
        final Catalogue catalogue = new Catalogue("datasets/hipparcos1997.txt");
        catalogue.updateBy(new Catalogue("datasets/hipparcos2007.txt"));
        catalogue.updateBy(new LuminosityClassifier(catalogue));

        final DustCloudDetector detector = new DustCloudDetector(new StarSelector(catalogue).selectByParallaxRelativeError(0.25).getCatalogue());

        final PrintWriter fout = new PrintWriter("src/main/resources/resources/clouds.txt");

        final List<Spheric> dirs = new ArrayList<>();
        for (final double[] p : detector.dust) {
            dirs.add(new Spheric(p));
            for (double coordinate : p) {
                fout.print(coordinate + "\t");
            }
            fout.println();
            fout.flush();
        }

        new HammerProjection(new HealpixCounter(dirs, 18));
    }
}
