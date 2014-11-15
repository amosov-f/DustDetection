package ru.spbu.astro.dust.algo;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.dust.func.HealpixCounter;
import ru.spbu.astro.dust.graph.HammerProjection;
import ru.spbu.astro.dust.model.Catalogue;
import ru.spbu.astro.dust.model.Spheric;
import ru.spbu.astro.dust.model.Star;
import ru.spbu.astro.dust.util.Geom;
import ru.spbu.astro.dust.util.PointsDistribution;
import ru.spbu.astro.dust.util.StarSelector;
import weka.core.*;
import weka.core.neighboursearch.KDTree;
import weka.core.neighboursearch.NearestNeighbourSearch;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * User: amosov-f
 * Date: 15.11.14
 * Time: 20:39
 */
public class SimpleDustCloudDetector {
    private static final int K = 25;
    private static final double THRESHOLD = 0.008;
    private static final double MAX_RANGE = 81.37697923079452;
    private static final int LIM = 100000;

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
            final Instances knn = search.kNearestNeighbours(instance(p, Double.NaN, instances), K);
            final double[] distances = search.getDistances();
            if (distances[distances.length - 1] > MAX_RANGE) {
                continue;
            }
            final SimpleRegression regression = new SimpleRegression();
            for (final Instance instance : knn) {
                regression.addData(Geom.abs(point(instance)), instance.value(3));
            }
            final double scale = regression.getSlope();
            /*final double scale = new LinearRegression() {{
                buildClassifier(new Instances("trend", new ArrayList<Attribute>() {{
                    add(new Attribute("r"));
                    add(new Attribute("ext"));
                }}, knn.size()) {{
                    addAll(knn.stream().map(instance -> new DenseInstance(2) {{
                        setValue(0, Geom.abs(point(instance)));
                        setValue(1, instance.value(3));
                    }}).collect(Collectors.toList()));
                    setClassIndex(1);
                }});
            }}.coefficients()[0];*/
            if (scale > THRESHOLD) {
                dust.add(p);
            }
        }
    }

    @NotNull
    public List<double[]> getDust() {
        return dust;
    }

    @NotNull
    private static Instance instance(@NotNull final double[] p, final double ext, @NotNull final Instances instances) {
        return new DenseInstance(4) {{
            for (int i = 0; i < p.length; ++i) {
                setValue(i, p[i]);
            }
            setValue(3, ext);
            setDataset(instances);
        }};
    }

    @NotNull
    private static double[] point(@NotNull final Instance instance) {
        return new double[]{instance.value(0), instance.value(1), instance.value(2)};
    }

    public static void main(final String[] args) throws Exception {
        final SimpleDustCloudDetector detector = new SimpleDustCloudDetector(
                new StarSelector(Catalogue.HIPPARCOS_UPDATED).selectByParallaxRelativeError(0.25).getStars()
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

        final HammerProjection hammerProjection = new HammerProjection(new HealpixCounter(dirs, 18));
        hammerProjection.setVisible(true);
    }

}
