package ru.spbu.astro.dust.algo;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.commons.Spheric;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.commons.StarFilter;
import ru.spbu.astro.commons.graph.HammerProjection;
import ru.spbu.astro.dust.DustStars;
import ru.spbu.astro.healpix.func.HealpixCounter;
import ru.spbu.astro.util.PointsDistribution;
import ru.spbu.astro.util.Value;
import ru.spbu.astro.util.ml.RansacLinearRegression;
import ru.spbu.astro.util.ml.SimpleRegression;
import weka.core.*;
import weka.core.neighboursearch.KDTree;
import weka.core.neighboursearch.NearestNeighbourSearch;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * User: amosov-f
 * Date: 15.11.14
 * Time: 20:39
 */
public final class DustDetector {
    private static final Logger LOGGER = Logger.getLogger(DustDetector.class.getName());

    private static final int K = 25;
    private static final double THRESHOLD = 0.008;
    private static final double MAX_RANGE = 100;
    private static final int LIM = 1000000;

    @NotNull
    private final List<Vector3D> dust = new ArrayList<>();

    public DustDetector(@NotNull final Star[] stars) {
        LOGGER.info("Dust detection started!");
        LOGGER.info("#stars = " + stars.length);

        final Instances instances = new Instances("knn", new ArrayList<Attribute>() {{
            add(new Attribute("x"));
            add(new Attribute("y"));
            add(new Attribute("z"));
            add(new Attribute("ext"));
        }}, stars.length) {{
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
            if (t % 10000 == 0) {
                LOGGER.info(t + " points processed");
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
                regression.add(instance.hashCode(), Value.of(point(instance).getNorm()), Value.of(instance.value(3)));
            }
            if (!regression.train()) {
                continue;
            }
            final double slope = regression.getSlope().getValue();
            if (slope > THRESHOLD) {
                dust.add(p);
            }
        }
        LOGGER.info("#dust = " + dust.size() + " from " + LIM + " random points");
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

    public static void main(final String[] args) {
        final DustDetector detector = new DustDetector(
                StarFilter.of(DustStars.ALL).piRelErr(0.35).stars()
        );

        final Spheric[] dirs = detector.getDust().stream().map(Spheric::new).toArray(Spheric[]::new);

        new HammerProjection(new HealpixCounter(dirs, 30)).setVisible(true);
    }

    @NotNull
    public List<Vector3D> getDust() {
        return dust;
    }
}
