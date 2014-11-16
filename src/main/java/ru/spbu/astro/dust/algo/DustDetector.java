package ru.spbu.astro.dust.algo;

import com.google.common.collect.Iterables;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
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
import weka.core.*;
import weka.core.neighboursearch.KDTree;
import weka.core.neighboursearch.NearestNeighbourSearch;

import java.awt.geom.Point2D;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * User: amosov-f
 * Date: 15.11.14
 * Time: 20:39
 */
public class DustDetector {
    private static final int K = 25;
    private static final double THRESHOLD = 0.006;
    private static final double MAX_RANGE = 100;//81.37697923079452;
    private static final int LIM = 1000000;

    @NotNull
    private final List<Vector3D> dust = new ArrayList<>();

    public DustDetector(@NotNull final List<Star> stars) {
        final Instances instances = new Instances("knn", new ArrayList<Attribute>() {{
            add(new Attribute("x"));
            add(new Attribute("y"));
            add(new Attribute("z"));
            add(new Attribute("ext"));
        }}, stars.size()) {{
            setClassIndex(3);
        }};

        final List<Vector3D> points = new ArrayList<>();
        for (final Star star : stars) {
            final Vector3D p = star.getCartesian();
            points.add(p);
            instances.add(instance(p, star.getExtinction().getValue(), instances));
        }

        final NearestNeighbourSearch search;
        try {
            search = new KDTree() {{
                setDistanceFunction(new EuclideanDistance() {{
                    setDontNormalize(true);
                }});
                setInstances(instances);
            }};
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        final PointsDistribution distribution = new PointsDistribution(points);
        for (int t = 0; t < LIM; t++) {
            if (t % 1000 == 0) {
                System.out.println(t);
            }
            final Vector3D p = distribution.next();

            final Instances knn;
            final double r;
            try {
                knn = search.kNearestNeighbours(instance(p, instances), K);
                final double[] distances = search.getDistances();
                r = distances[distances.length - 1];
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (r > MAX_RANGE) {
                continue;
            }
            if (r >= p.getNorm()) {
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
    public List<Vector3D> getDust() {
        return dust;
    }

    @NotNull
    private static Instance instance(@NotNull final Vector3D p, final double ext, @NotNull final Instances instances) {
        return new DenseInstance(4) {{
            setValue(0, p.getX());
            setValue(1, p.getY());
            setValue(2, p.getZ());
            setValue(3, ext);
            setDataset(instances);
        }};
    }

    @NotNull
    private static Instance instance(@NotNull final Vector3D p, @NotNull final Instances instances) {
        return instance(p, Double.NaN, instances);
    }

    @NotNull
    private static double[] point(@NotNull final Instance instance) {
        return new double[]{instance.value(0), instance.value(1), instance.value(2)};
    }

    public static void main(final String[] args) throws FileNotFoundException {
        final DustDetector detector = new DustDetector(
                new StarSelector(Catalogue.HIPPARCOS_UPDATED).selectByParallaxRelativeError(0.35).getStars()
        );

        final List<Spheric> dirs = new ArrayList<>();
        for (final Vector3D p : detector.getDust()) {
            dirs.add(new Spheric(p));
        }

        final HammerProjection hammerProjection = new HammerProjection(new HealpixCounter(dirs, 25));
        hammerProjection.setVisible(true);
    }
}
