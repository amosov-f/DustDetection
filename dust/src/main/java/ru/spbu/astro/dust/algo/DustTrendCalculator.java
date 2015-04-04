package ru.spbu.astro.dust.algo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.commons.Spheric;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.commons.Stars;
import ru.spbu.astro.healpix.Healpix;
import ru.spbu.astro.util.Value;
import ru.spbu.astro.util.ml.RansacLinearRegression;
import ru.spbu.astro.util.ml.SimpleRegression;

import java.util.Arrays;
import java.util.Map;
import java.util.logging.Logger;

public final class DustTrendCalculator {
    private static final Logger LOGGER = Logger.getLogger(DustTrendCalculator.class.getName());

    @NotNull
    private final Value[] slopes;
    @NotNull
    private final Value[] intercepts;

    @NotNull
    private final Healpix healpix;

    @NotNull
    private final Star[][] inliers;
    @NotNull
    private final Star[][] outliers;

    public DustTrendCalculator(@NotNull final Star[] stars, final int nSide) {
        this(stars, nSide, false);
    }

    public DustTrendCalculator(@NotNull final Star[] stars, final int nSide, final boolean includeIntercept) {
        LOGGER.info("#stars = " + stars.length);

        healpix = new Healpix(nSide);
        inliers = new Star[healpix.getNPix()][];
        outliers = new Star[healpix.getNPix()][];

        final Star[][] rings = healpix.split(stars);

        slopes = new Value[rings.length];
        intercepts = new Value[rings.length];

        final Map<Integer, Star> id2star = Stars.map(stars);

        for (int pix = 0; pix < rings.length; pix++) {
            final SimpleRegression regression = new RansacLinearRegression(includeIntercept);
            for (final Star star : rings[pix]) {
                regression.add(star.getId(), star.getR(), star.getExtinction());
            }
            if (regression.train()) {
                slopes[pix] = regression.getSlope();
                intercepts[pix] = regression.getIntercept();
                inliers[pix] = Arrays.stream(regression.getInliers()).mapToObj(id2star::get).toArray(Star[]::new);
                outliers[pix] = Arrays.stream(regression.getOutliers()).mapToObj(id2star::get).toArray(Star[]::new);
            }
        }
    }

    public int getNSide() {
        return healpix.getNSide();
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
    public Star[] getInlierStars(@NotNull final Spheric dir) {
        return inliers[healpix.getPix(dir)];
    }

    @Nullable
    public Star[] getOutlierStars(@NotNull final Spheric dir) {
        return outliers[healpix.getPix(dir)];
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
        for (final Star[] ring : inliers) {
            for (final Star star : ring) {
                dr = Math.max(dr, star.getR().getRelativeError());
            }
        }

        final StringBuilder sb = new StringBuilder();
        sb.append("dr <= ").append((int) (100 * dr)).append("%, n_side = ").append(healpix.getNSide()).append("\n");
        sb.append("№\tl\t\t\tb\t\t\tk\t\tsigma_k\tn\n");
        for (int i = 0; i < inliers.length; ++i) {
            final Spheric dir = healpix.getCenter(i);
            final Value k = slopes[i];
            sb.append(String.format(
                    "%d\t%f\t%f\t%.2f\t%.2f\t%d\n",
                    i, dir.getL(), dir.getB(), 1000 * k.getValue(), 1000 * k.getError(), inliers[i].length
            ));
        }
        return sb.toString();
    }
}
