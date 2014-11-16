package ru.spbu.astro.dust.algo;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.dust.func.HealpixCounter;
import ru.spbu.astro.dust.graph.HammerProjection;
import ru.spbu.astro.dust.ml.RansacRegression;
import ru.spbu.astro.dust.model.Catalogue;
import ru.spbu.astro.dust.model.Spheric;
import ru.spbu.astro.dust.model.Star;
import ru.spbu.astro.dust.util.Geom;
import ru.spbu.astro.dust.util.PointsDistribution;
import ru.spbu.astro.dust.util.StarSelector;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LeastMedSq;
import weka.classifiers.functions.LinearRegression;
import weka.core.*;
import weka.core.neighboursearch.KDTree;
import weka.core.neighboursearch.NearestNeighbourSearch;

import java.awt.geom.Point2D;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static ru.spbu.astro.dust.util.Geom.abs;
import static ru.spbu.astro.dust.util.Geom.dotProduct;

/**
 * User: amosov-f
 * Date: 15.11.14
 * Time: 20:39
 */
public class SimpleDustCloudDetector {
    private static final int K = 25;
    private static final double THRESHOLD = 0.006;
    private static final double MAX_RANGE = 100;//81.37697923079452;
    private static final int LIM = 10000000;

    @NotNull
    private final List<double[]> dust = new ArrayList<>();

    public SimpleDustCloudDetector(@NotNull final List<Star> stars) throws Exception {
        final Instances instances = new Instances("knn", new ArrayList<Attribute>() {{
            add(new Attribute("x"));
            add(new Attribute("y"));
            add(new Attribute("z"));
            add(new Attribute("ext"));
        }}, stars.size()) {{
            setClassIndex(3);
        }};

        final List<double[]> points = new ArrayList<>();
        for (final Star star : stars) {
            final double[] p = star.getCartesian();
            points.add(p);
            instances.add(instance(p, star.getExtinction().getValue(), instances));
        }

        final NearestNeighbourSearch search = new KDTree() {{
            setDistanceFunction(new EuclideanDistance() {{
                setDontNormalize(true);
            }});
            setInstances(instances);
        }};

        final PointsDistribution distribution = new PointsDistribution(points);
        for (int t = 0; t < LIM; t++) {
            if (t % 1000 == 0) {
                System.out.println(t);
            }
            final double[] p = distribution.next();
            final Instances knn = search.kNearestNeighbours(instance(p, instances), K);
            final double[] distances = search.getDistances();
            if (distances[distances.length - 1] > MAX_RANGE) {
                continue;
            }

            final RansacRegression regression = new RansacRegression(true);
            for (final Instance instance : knn) {
                regression.add(instance.hashCode(), new Point2D.Double(Geom.abs(point(instance)), instance.value(3)));
            }
            if (!regression.train()) {
                continue;
            }
            double slope = regression.getSlope().getValue();
            if (slope > THRESHOLD) {
                dust.add(p);
            }
        }
        System.out.println(dust.size());
    }

    @NotNull
    public List<double[]> getDust() {
        return dust;
    }

    public double getSlope(@NotNull final Classifier regression, @NotNull final Instances instances) throws Exception {
        final double b = regression.classifyInstance(instance(new double[]{0}, instances));
        return regression.classifyInstance(instance(new double[]{1}, instances)) - b;
    }

    @NotNull
    private static Instance instance(@NotNull final double[] p, final double ext, @NotNull final Instances instances) {
        return new DenseInstance(p.length + 1) {{
            for (int i = 0; i < p.length; i++) {
                setValue(i, p[i]);
            }
            setValue(p.length, ext);
            setDataset(instances);
        }};
    }

    @NotNull
    private static Instance instance(@NotNull final double[] p, @NotNull final Instances instances) {
        return instance(p, Double.NaN, instances);
    }

    @NotNull
    private static double[] point(@NotNull final Instance instance) {
        return new double[]{instance.value(0), instance.value(1), instance.value(2)};
    }

    public static void main(final String[] args) throws Exception {
        final SimpleDustCloudDetector detector = new SimpleDustCloudDetector(
                new StarSelector(Catalogue.HIPPARCOS_UPDATED).selectByParallaxRelativeError(0.35).getStars()
        );

        final PrintWriter fout = new PrintWriter("src/main/resources/resources/clouds.txt");

        final List<Spheric> dirs = new ArrayList<>();
        for (final double[] p : detector.getDust()) {
            dirs.add(new Spheric(p));
            for (double coordinate : p) {
                fout.print(coordinate + "\t");
            }
            fout.println();
            fout.flush();
        }

        final HammerProjection hammerProjection = new HammerProjection(new HealpixCounter(dirs, 25));
        hammerProjection.setVisible(true);
    }
}
