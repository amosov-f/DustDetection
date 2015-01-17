package ru.spbu.astro.dust.algo;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.core.func.HealpixCounter;
import ru.spbu.astro.core.graph.HammerProjection;
import ru.spbu.astro.util.ml.RansacLinearRegression;
import ru.spbu.astro.core.Spheric;
import ru.spbu.astro.core.Star;
import ru.spbu.astro.util.ml.SimpleRegression;
import ru.spbu.astro.dust.DustCatalogues;
import ru.spbu.astro.util.PointsDistribution;
import ru.spbu.astro.core.StarFilter;
import ru.spbu.astro.util.Value;
import weka.core.*;
import weka.core.neighboursearch.KDTree;
import weka.core.neighboursearch.NearestNeighbourSearch;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: amosov-f
 * Date: 15.11.14
 * Time: 20:39
 */
public final class DustDetector {
    private static final int K = 25;
    private static final double THRESHOLD = 0.008;
    private static final double MAX_RANGE = 100;
    private static final int LIM = 1000000;

    @NotNull
    private final List<Vector3D> dust = new ArrayList<>();

    public DustDetector(@NotNull final List<Star> stars) {
        System.out.println(stars.size());

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

            final SimpleRegression regression = new RansacLinearRegression(true);
            for (final Instance instance : knn) {
                regression.add(instance.hashCode(), new Value(point(instance).getNorm()), new Value(instance.value(3)));
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
    private static Vector3D point(@NotNull final Instance instance) {
        return new Vector3D(instance.value(0), instance.value(1), instance.value(2));
    }

    public static void main(final String[] args) throws FileNotFoundException {
        final DustDetector detector = new DustDetector(
                new StarFilter(DustCatalogues.HIPPARCOS_UPDATED.getStars()).parallaxRelativeError(0.35).getStars()
        );

        final List<Spheric> dirs = new ArrayList<>();
        for (final Vector3D p : detector.getDust()) {
            dirs.add(new Spheric(p));
        }

        final HammerProjection hammerProjection = new HammerProjection(new HealpixCounter(dirs, 30));
        hammerProjection.setVisible(true);
    }
}
