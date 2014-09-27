package ru.spbu.astro.dust.func;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.dust.graph.HammerProjection;
import ru.spbu.astro.dust.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ru.spbu.astro.dust.model.Catalogue.HIPPARCOS_UPDATED;

/**
 * User: amosov-f
 * Date: 22.09.14
 * Time: 22:27
 */
public final class ScatterDistribution implements SphericDistribution {
    private static final double RADIUS = 0.1;

    @NotNull
    private final List<Spheric> dirs = new ArrayList<>();

    public ScatterDistribution(@NotNull final List<Spheric> dirs) {
        this.dirs.addAll(dirs);
    }

    @NotNull
    @Override
    public Value get(@NotNull Spheric dir) {
        for (final Spheric markedDir : dirs) {
            if (markedDir.distanceTo(dir) < RADIUS) {
                return new Value(1);
            }
        }
        return new Value(0);
    }

    public static void main(String[] args) {
        final Catalogue catalogue = HIPPARCOS_UPDATED;

        final List<Spheric> dirs = catalogue.getStars().stream().map(Star::getDir).collect(Collectors.toList());

        System.out.println(dirs.size());

        new HammerProjection(new ScatterDistribution(dirs));
    }
}
