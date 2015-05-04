package ru.spbu.astro.dust.algo;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.commons.Spheric;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.commons.Stars;
import ru.spbu.astro.healpix.Healpix;
import ru.spbu.astro.util.Filter;
import ru.spbu.astro.util.Point;
import ru.spbu.astro.util.Value;
import ru.spbu.astro.util.ml.RansacLinearRegression;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class DustTrendCalculator {
    private static final Logger LOGGER = Logger.getLogger(DustTrendCalculator.class.getName());

    private static final int MIN_FOR_TREND = 5;

    @NotNull
    private final Star[][] rings;
    @NotNull
    private final Regression[] regressions;
    @NotNull
    private final Healpix healpix;

    public DustTrendCalculator(@NotNull final Star[] stars, final int nSide) {
        this(stars, nSide, Star::getExtinction, false);
    }

    public DustTrendCalculator(@NotNull final Star[] stars, final int nSide, @NotNull final Function<Star, Value> f, final boolean includeIntercept) {
        LOGGER.info("#stars = " + stars.length);

        healpix = new Healpix(nSide);
        rings = healpix.split(stars);
        regressions = new Regression[rings.length];

        for (int pix = 0; pix < rings.length; pix++) {
            final Star[] ring = rings[pix];
            if (ring.length >= MIN_FOR_TREND) {
                regressions[pix] = new Regression(ring, f, includeIntercept);
            }

        }
    }

    public int getNSide() {
        return healpix.getNSide();
    }

    @Nullable
    public Regression getRegression(@NotNull final Spheric dir) {
        return regressions[healpix.getPix(dir)];
    }

    @NotNull
    public Value[] getSlopes() {
        return Arrays.stream(regressions)
                .map(regression -> regression != null ? regression.getSlope() : null)
                .toArray(Value[]::new);
    }

    @NotNull
    public Value[] getIntercepts() {
        return Arrays.stream(regressions)
                .map(regression -> regression != null ? regression.getIntercept() : null)
                .toArray(Value[]::new);
    }

    @NotNull
    public Star[] getInliers() {
        final List<Star> stars = new ArrayList<>();
        Arrays.stream(regressions).forEach(regression -> stars.addAll(Arrays.asList(regression.getInliers())));
        return stars.toArray(new Star[stars.size()]);
    }

    @Nullable
    public Value getSlope(@NotNull final Spheric dir) {
        return regressions[healpix.getPix(dir)].getSlope();
    }

    @Override
    public String toString() {
        return toString(null);
    }

    @NotNull
    public String toString(@Nullable final Filter<Value> slopeFilter) {
        final StringBuilder sb = new StringBuilder();
        sb.append("n_side = ").append(healpix.getNSide()).append(", n_pix = ").append(healpix.getNPix()).append('\n');
        if (slopeFilter != null) {
            final int acceptedSlopesCount = (int) Arrays.stream(regressions)
                    .filter(((Predicate<Regression>) Objects::nonNull)
                            .and(regression -> slopeFilter.getPredicate().test(regression.getSlope())))
                    .count();
            sb.append("n_accepted_pix = ").append(acceptedSlopesCount);
            sb.append(" (").append(100 * acceptedSlopesCount / healpix.getNPix()).append("%)\n");
            sb.append("accept if ").append(slopeFilter).append('\n');
        }
        sb.append("№\tl\t\t\tb\t\t\tn\tk\n");
        for (int pix = 0; pix < rings.length; ++pix) {
            final Spheric dir = healpix.getCenter(pix);
            final Value k = regressions[pix] != null ? regressions[pix].getSlope() : null;
            if (k != null && (slopeFilter == null || slopeFilter.getPredicate().test(k))) {
                sb.append(String.format(
                        "%d\t%f\t%f\t%d\t%.2f ± %.2f\n",
                        pix, Math.toDegrees(dir.getL()), Math.toDegrees(dir.getB()), rings[pix].length, 1000 * k.val(), 1000 * k.err()
                ));
            }
        }
        return sb.toString();
    }

    @NotNull
    public Object[][] toTable(@Nullable final Filter<Value> slopeFilter) {
        final List<Object[]> table = new ArrayList<>();
        for (int pix = 0; pix < rings.length; ++pix) {
            final Spheric dir = healpix.getCenter(pix);
            final Value k = regressions[pix] != null ? regressions[pix].getSlope().multiply(1000) : null;
            if (k != null && (slopeFilter == null || slopeFilter.getPredicate().test(k))) {
                table.add(new Object[]{
                        pix,
                        String.format(Locale.US, "(%.2f, %.2f)", Math.toDegrees(dir.getL()), Math.toDegrees(dir.getB())),
                        rings[pix].length,
                        String.format(Locale.US, "$%.2f \\pm %.2f$", k.val(), k.err())
                });
            }
        }
        return table.toArray(new Object[table.size()][]);
    }

    public static final class Regression {
        @NotNull
        private final Value slope;
        @NotNull
        private final Value intercept;
        @NotNull
        private final Star[] inliers;
        @NotNull
        private final Star[] outliers;
        @NotNull
        private final Function<Star, Value> f;

        public Regression(@NotNull final Star[] stars) {
            this(stars, Star::getExtinction, false);
        }

        public Regression(@NotNull final Star[] stars, @NotNull final Function<Star, Value> f, final boolean includeIntercept) {
            this.f = f;
            final RansacLinearRegression regression = RansacLinearRegression.train(
                    Arrays.stream(stars).collect(Collectors.toMap(Star::getId, this::toPoint)), includeIntercept
            );
            slope = regression.getSlope();
            intercept = regression.getIntercept();
            final Map<Integer, Star> id2star = Stars.map(stars);
            inliers = Arrays.stream(regression.getInliers()).mapToObj(id2star::get).toArray(Star[]::new);
            outliers = Arrays.stream(regression.getOutliers()).mapToObj(id2star::get).toArray(Star[]::new);
        }

        @NotNull
        public Value getSlope() {
            return slope;
        }

        @NotNull
        public Value getIntercept() {
            return intercept;
        }

        @NotNull
        public Star[] getStars() {
            return ArrayUtils.addAll(inliers, outliers);
        }

        @NotNull
        public Star[] getInliers() {
            return inliers;
        }

        @NotNull
        public Star[] getOutliers() {
            return outliers;
        }

        @NotNull
        private Point toPoint(@NotNull final Star star) {
            return new Point(star.getR(), f.apply(star));
        }
    }
}
