package ru.spbu.astro.dust.algo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.commons.Spheric;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.healpix.Healpix;
import ru.spbu.astro.util.MathTools;
import ru.spbu.astro.util.Point;
import ru.spbu.astro.util.Value;
import ru.spbu.astro.util.ml.RansacLinearRegression;
import ru.spbu.astro.util.ml.SlopeLinearRegression;
import ru.spbu.astro.util.ml.WeightedMedianRegression;

import java.util.Arrays;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class DustTrendCalculator {
    private static final Logger LOGGER = Logger.getLogger(DustTrendCalculator.class.getName());

    private static final int MIN_FOR_TREND = 3;

    @NotNull
    private final Star[][] rings;

    @NotNull
    private final Value[] slopes;
    @NotNull
    private final Value[] averages;

    @NotNull
    private final Healpix healpix;

    public DustTrendCalculator(@NotNull final Star[] stars, final int nSide) {
        LOGGER.info("#stars = " + stars.length);

        healpix = new Healpix(nSide);

        rings = healpix.split(stars);

        slopes = new Value[rings.length];
        averages = new Value[rings.length];

        for (int pix = 0; pix < rings.length; pix++) {
            final Star[] ring = rings[pix];
            if (ring.length == 0) {
                continue;
            }
            averages[pix] = MathTools.average(Arrays.stream(ring).mapToDouble(star -> star.getExtinction().val()).toArray());
            if (ring.length < MIN_FOR_TREND) {
                continue;
            }
//            final SlopeLinearRegression regression = RansacLinearRegression.train(
//                    Arrays.stream(ring).collect(Collectors.toMap(Star::getId, DustTrendCalculator::point)), false
//            );
            final SlopeLinearRegression regression = new WeightedMedianRegression(
                    Arrays.stream(ring).map(DustTrendCalculator::point).toArray(Point[]::new)
            );
            slopes[pix] = regression.getSlope();
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
    public Value[] getAverages() {
        return averages;
    }

    @Nullable
    public Star[] getInlierStars(@NotNull final Spheric dir) {
        return rings[healpix.getPix(dir)];
    }

    @Nullable
    public Star[] getOutlierStars(@NotNull final Spheric dir) {
        return rings[healpix.getPix(dir)];
    }

    @Nullable
    public Value getSlope(@NotNull final Spheric dir) {
        return slopes[healpix.getPix(dir)];
    }

    @NotNull
    private static Point point(@NotNull final Star star) {
        return new Point(star.getR(), star.getExtinction());
    }

    @NotNull
    @Override
    public String toString() {
        double dr = 0;
        for (final Star[] ring : rings) {
            for (final Star star : ring) {
                dr = Math.max(dr, star.getR().relErr());
            }
        }

        final StringBuilder sb = new StringBuilder();
        sb.append("n_side = ").append(healpix.getNSide()).append("\n");
        sb.append("№\tl\t\t\tb\t\t\tn\tk\t\tmad_k\tk(mnk)\tsigma_k\tave\t\tsigma_ave\n");
        for (int pix = 0; pix < rings.length; ++pix) {
            final Spheric dir = healpix.getCenter(pix);
            final Value k = slopes[pix];
            final Value ave = averages[pix];
            final Star[] ring = rings[pix];
            final SlopeLinearRegression regression = RansacLinearRegression.train(
                    Arrays.stream(ring).collect(Collectors.toMap(Star::getId, DustTrendCalculator::point)), false
            );
            final Value k1 = regression.getSlope();
            sb.append(String.format(
                    "%d\t%f\t%f\t%d\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f\n",
                    pix, dir.getL(), dir.getB(), rings[pix].length, 1000 * k.val(), 1000 * k.err(), 1000 * k1.val(), 1000 * k1.err(), ave.val(), ave.err()
            ));
        }
        return sb.toString();
    }
}
