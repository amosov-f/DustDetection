package ru.spbu.astro.dust.algo;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.core.Cloud;
import ru.spbu.astro.core.Star;
import ru.spbu.astro.core.StarFilter;
import ru.spbu.astro.core.func.CloudDistribution;
import ru.spbu.astro.core.graph.HammerProjection;
import ru.spbu.astro.dust.DustCatalogues;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * User: amosov-f
 * Date: 16.11.14
 * Time: 14:58
 */
public final class DustCloudDetector {
    private static final Logger LOGGER = Logger.getLogger(DustCloudDetector.class.getName());

    private static final double EPS = 450;
    private static final double MIN_PTS_PART = 0.004;

    @NotNull
    private final List<Cloud> clouds;

    public DustCloudDetector(@NotNull final List<Star> stars) {
        final List<Vector3D> dust = new DustDetector(stars).getDust();
        final double eps = EPS / Math.cbrt(dust.size());
        final int minPts = (int) (MIN_PTS_PART * dust.size());
        LOGGER.info("eps: " + eps + ", minPts: " + minPts);
        clouds = new DBSCANClusterer<DoublePoint>(eps, minPts)
                .cluster(dust.stream()
                        .map(p -> new DoublePoint(p.toArray())).collect(Collectors.toList())).stream()
                .map(cluster -> new Cloud(cluster.getPoints().stream()
                        .map(p -> new Vector3D(p.getPoint())).collect(Collectors.toList()))).collect(Collectors.toList());
    }

    public static void main(@NotNull final String[] args) {
        final DustCloudDetector detector = new DustCloudDetector(
                new StarFilter(DustCatalogues.HIPPARCOS_UPDATED.getStars()).parallaxRelativeError(0.35).getStars()
        );
        final List<Cloud> clouds = detector.getClouds();
        LOGGER.info("#clouds: " + clouds.size());
        clouds.forEach(cloud -> LOGGER.info(cloud.toString()));
        final HammerProjection hammerProjection = new HammerProjection(new CloudDistribution(detector.getClouds()));
        hammerProjection.setVisible(true);
    }

    @NotNull
    public List<Cloud> getClouds() {
        return clouds;
    }
}
