package ru.spbu.astro.commons.func;

import org.apache.commons.math3.geometry.enclosing.EnclosingBall;
import org.apache.commons.math3.geometry.spherical.twod.S2Point;
import org.apache.commons.math3.geometry.spherical.twod.Sphere2D;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.commons.Cloud;
import ru.spbu.astro.commons.Spheric;
import ru.spbu.astro.util.Value;

import java.util.List;
import java.util.stream.Collectors;

/**
 * User: amosov-f
 * Date: 16.11.14
 * Time: 16:55
 */
public final class CloudDistribution implements SphericDistribution {
    @NotNull
    private final List<EnclosingBall<Sphere2D, S2Point>> sphericBalls;
    @NotNull
    private final List<Cloud> clouds;

    public CloudDistribution(@NotNull final List<Cloud> clouds) {
        this.clouds = clouds;
        sphericBalls = clouds.stream()
                .map(ball -> new EnclosingBall<>(
                        new S2Point(ball.getCenter()),
                        Math.asin(ball.getRadius() / ball.getCenter().getNorm())
                )).collect(Collectors.toList());
    }

    @Nullable
    @Override
    public Value get(@NotNull final Spheric dir) {
        for (int i = 0; i < clouds.size(); i++) {
            if (sphericBalls.get(i).contains(dir)) {
                return Value.of(clouds.get(i).getNumPoints());
            }
        }
        return Value.ZERO;
    }
}
