package ru.spbu.astro.dust.algo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.commons.Spheric;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.healpix.Healpix;
import ru.spbu.astro.util.Value;
import ru.spbu.astro.util.ml.RansacLinearRegression;
import ru.spbu.astro.util.ml.SimpleRegression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class DustTrendCalculator {
    private static final Logger LOGGER = Logger.getLogger(DustTrendCalculator.class.getName());

    public static final int N_SIDE = 18;

    @NotNull
    private final List<List<Star>> inliers = new ArrayList<>();
    @NotNull
    private final List<List<Star>> outliers = new ArrayList<>();

    @NotNull
    private final Value[] slopes;
    @NotNull
    private final Value[] intercepts;

    @NotNull
    private final Healpix healpix = new Healpix(N_SIDE);

    public DustTrendCalculator(@NotNull final List<Star> stars) {
        this(stars, false);
    }

    public DustTrendCalculator(@NotNull final List<Star> stars, final boolean includeIntercept) {
        LOGGER.info("#stars = " + stars.size());

        final List<List<Star>> rings = new ArrayList<>();
        for (int i = 0; i < Healpix.nPix(N_SIDE); i++) {
            rings.add(new ArrayList<>());
        }

        final Map<Integer, Star> id2star = new HashMap<>();
        for (final Star star : stars) {
            id2star.put(star.getId(), star);
            rings.get(healpix.getPix(star.getDir())).add(star);
        }

        slopes = new Value[rings.size()];
        intercepts = new Value[rings.size()];

        for (int pix = 0; pix < rings.size(); pix++) {
            final SimpleRegression regression = new RansacLinearRegression(includeIntercept);
            for (final Star star : rings.get(pix)) {
                regression.add(star.getId(), star.getR(), star.getExtinction());
            }
            if (regression.train()) {
                slopes[pix] = regression.getSlope();
                intercepts[pix] = regression.getIntercept();
                inliers.add(regression.getInliers().stream().map(id2star::get).collect(Collectors.toList()));
                outliers.add(regression.getOutliers().stream().map(id2star::get).collect(Collectors.toList()));
            } else {
                inliers.add(null);
                outliers.add(null);
            }
        }
    }

    @NotNull
    public Value[] getSlopes() {
        return slopes;
    }

    @NotNull
    public Value[] getIntercepts() {
        return intercepts;
    }

    @Nullable
    public List<Star> getInlierStars(@NotNull final Spheric dir) {
        return inliers.get(healpix.getPix(dir));
    }

    @Nullable
    public List<Star> getOutlierStars(@NotNull final Spheric dir) {
        return outliers.get(healpix.getPix(dir));
    }

    @Nullable
    public Value getSlope(@NotNull final Spheric dir) {
        return slopes[healpix.getPix(dir)];
    }

    @Nullable
    public Value getIntercept(@NotNull final Spheric dir) {
        return intercepts[healpix.getPix(dir)];
    }

    @NotNull
    @Override
    public String toString() {
        double dr = 0;
        for (final List<Star> ring : inliers) {
            for (final Star star : ring) {
                dr = Math.max(dr, star.getR().getRelativeError());
            }
        }

        final StringBuilder sb = new StringBuilder();
        sb.append("dr <= ").append((int) (100 * dr)).append("%, n_side = ").append(N_SIDE).append("\n");
        sb.append("№\tl\t\t\tb\t\t\tk\t\tsigma_k\tn\n");
        for (int i = 0; i < inliers.size(); ++i) {
            final Spheric dir = healpix.getCenter(i);
            final Value k = slopes[i];
            sb.append(String.format(
                    "%d\t%f\t%f\t%.2f\t%.2f\t%d\n",
                    i, dir.getL(), dir.getB(), 1000 * k.getValue(), 1000 * k.getError(), inliers.get(i).size()
            ));
        }
        return sb.toString();
    }

}
