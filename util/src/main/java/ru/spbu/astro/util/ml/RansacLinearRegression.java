package ru.spbu.astro.util.ml;

import com.google.common.primitives.Ints;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.util.ArrayTools;
import ru.spbu.astro.util.Value;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * User: amosov-f
 * Date: 16.11.14
 * Time: 3:06
 */
public final class RansacLinearRegression implements SimpleRegression {
    private static final double OUTLIERS_PART = 0.1;
    private static final int MIN_FOR_TREND = 3;

    @NotNull
    private final org.apache.commons.math3.stat.regression.SimpleRegression regression;
    @NotNull
    private final Map<Integer, Vector2D> points = new HashMap<>();

    private int[] inliers;
    private int[] outliers;

    public RansacLinearRegression(final boolean includeIntercept) {
        regression = new org.apache.commons.math3.stat.regression.SimpleRegression(includeIntercept);
    }

    @Override
    public void add(final int id, @NotNull final Value x, @NotNull final Value y) {
        points.put(id, new Vector2D(x.getValue(), y.getValue()));
    }

    @Override
    public boolean train() {
        final int[] ids = Ints.toArray(points.keySet());
        if (!train(ids)) {
            return false;
        }
        ArrayTools.sort(ids, Comparator.comparingDouble(id -> target(points.get(id))));
        final int split = (int) ((1 - OUTLIERS_PART) * ids.length);
        inliers = Arrays.copyOf(ids, split);
        outliers = Arrays.copyOfRange(ids, split, ids.length);
        return train(inliers);
    }

    @NotNull
    @Override
    public int[] getInliers() {
        return inliers;
    }

    @NotNull
    @Override
    public int[] getOutliers() {
        return outliers;
    }

    @NotNull
    @Override
    public Value getSlope() {
        return new Value(regression.getSlope(), regression.getSlopeStdErr());
    }

    @NotNull
    @Override
    public Value getIntercept() {
        return new Value(regression.getIntercept(), regression.getInterceptStdErr());
    }

    private double target(@NotNull final Vector2D p) {
        return Math.pow(regression.predict(p.getX()) - p.getY(), 2);
    }

    private boolean train(@NotNull final int[] ids) {
        if (ids.length < MIN_FOR_TREND) {
            return false;
        }
        regression.clear();
        for (final int id : ids) {
            regression.addData(points.get(id).getX(), points.get(id).getY());
        }
        regression.regress();
        return true;
    }
}
