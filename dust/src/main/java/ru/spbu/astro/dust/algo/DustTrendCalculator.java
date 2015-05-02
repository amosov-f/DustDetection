package ru.spbu.astro.dust.algo;

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
    private final Star[][] inliers;
    @NotNull
    private final Star[][] outliers;
    @NotNull
    private final Value[] slopes;
    @NotNull
    private final Value[] intercepts;
    @NotNull
    private final Healpix healpix;
    @NotNull
    private final Function<Star, Value> f;

    public DustTrendCalculator(@NotNull final Star[] stars, final int nSide) {
        this(stars, nSide, Star::getExtinction, false);
    }

    public DustTrendCalculator(@NotNull final Star[] stars, final int nSide, @NotNull final Function<Star, Value> f, final boolean includeIntercept) {
        LOGGER.info("#stars = " + stars.length);

        healpix = new Healpix(nSide);
        rings = healpix.split(stars);
        inliers = new Star[rings.length][];
        outliers = new Star[rings.length][];
        slopes = new Value[rings.length];
        intercepts = new Value[rings.length];
        this.f = f;

        for (int pix = 0; pix < rings.length; pix++) {
            final Star[] ring = rings[pix];
            if (ring.length < MIN_FOR_TREND) {
                continue;
            }
            final RansacLinearRegression regression = RansacLinearRegression.train(
                    Arrays.stream(ring).collect(Collectors.toMap(Star::getId, this::toPoint)), includeIntercept
            );
            slopes[pix] = regression.getSlope();
            intercepts[pix] = regression.getIntercept();
            final Map<Integer, Star> id2star = Stars.map(ring);
            inliers[pix] = Arrays.stream(regression.getInliers()).mapToObj(id2star::get).toArray(Star[]::new);
            outliers[pix] = Arrays.stream(regression.getOutliers()).mapToObj(id2star::get).toArray(Star[]::new);
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
    public Star[] getInliers(@NotNull final Spheric dir) {
        return inliers[healpix.getPix(dir)];
    }

    @NotNull
    public Star[] getInliers() {
        final List<Star> stars = new ArrayList<>();
        Arrays.stream(inliers).forEach(inliers -> stars.addAll(Arrays.asList(inliers)));
        return stars.toArray(new Star[stars.size()]);
    }

    @Nullable
    public Star[] getOutliers(@NotNull final Spheric dir) {
        return outliers[healpix.getPix(dir)];
    }

    @Nullable
    public Value getSlope(@NotNull final Spheric dir) {
        return slopes[healpix.getPix(dir)];
    }

    @NotNull
    private Point toPoint(@NotNull final Star star) {
        return new Point(star.getR(), f.apply(star));
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
            final int acceptedSlopesCount = (int) Arrays.stream(slopes)
                    .filter(((Predicate<Value>) Objects::nonNull).and(slopeFilter.getPredicate()))
                    .count();
            sb.append("n_accepted_pix = ").append(acceptedSlopesCount);
            sb.append(" (").append(100 * acceptedSlopesCount / healpix.getNPix()).append("%)\n");
            sb.append("accept if ").append(slopeFilter).append('\n');
        }
        sb.append("№\tl\t\t\tb\t\t\tn\tk\n");
        for (int pix = 0; pix < rings.length; ++pix) {
            final Spheric dir = healpix.getCenter(pix);
            final Value k = slopes[pix];
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
            final Value k = slopes[pix] != null ? slopes[pix].multiply(1000) : null;
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
}
