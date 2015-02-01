package ru.spbu.astro.util.ml;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.util.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    @NotNull
    private final List<Integer> inliers = new ArrayList<>();
    @NotNull
    private final List<Integer> outliers = new ArrayList<>();

    public RansacLinearRegression(final boolean includeIntercept) {
        regression = new org.apache.commons.math3.stat.regression.SimpleRegression(includeIntercept);
    }

    @Override
    public void add(final int id, @NotNull final Value x, @NotNull final Value y) {
        points.put(id, new Vector2D(x.getValue(), y.getValue()));
    }

    @Override
    public boolean train() {
        final List<Integer> ids = new ArrayList<>(points.keySet());
        if (!train(ids)) {
            return false;
        }
        ids.sort((id1, id2) -> Double.compare(target(points.get(id1)), target(points.get(id2))));
        final int split = (int) ((1 - OUTLIERS_PART) * ids.size());
        inliers.addAll(ids.subList(0, split));
        outliers.addAll(ids.subList(split, ids.size()));
        return train(inliers);
    }

    @NotNull
    @Override
    public List<Integer> getInliers() {
        return inliers;
    }

    @NotNull
    @Override
    public List<Integer> getOutliers() {
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

    private boolean train(@NotNull final List<Integer> ids) {
        if (ids.size() < MIN_FOR_TREND) {
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
