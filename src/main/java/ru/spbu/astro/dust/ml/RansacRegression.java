package ru.spbu.astro.dust.ml;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.dust.model.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: amosov-f
 * Date: 16.11.14
 * Time: 3:06
 */
public class RansacRegression {
    private static final double OUTLIERS_PART = 0.1;
    private static final int MIN_FOR_TREND = 3;

    @NotNull
    private final SimpleRegression regression;
    @NotNull
    private final Map<Integer, Vector2D> points = new HashMap<>();
    @NotNull
    private final List<Integer> bases = new ArrayList<>();
    @NotNull
    private final List<Integer> outliers = new ArrayList<>();

    public RansacRegression(final boolean includeIntercept) {
        regression = new SimpleRegression(includeIntercept);
    }

    public void add(int id, @NotNull final Vector2D p) {
        points.put(id, p);
    }

    public boolean train() {
        final List<Integer> ids = new ArrayList<>(points.keySet());
        if (!train(ids)) {
            return false;
        }
        ids.sort((id1, id2) -> Double.compare(target(points.get(id1)), target(points.get(id2))));
        final int split = (int) ((1 - OUTLIERS_PART) * ids.size());
        bases.addAll(ids.subList(0, split));
        outliers.addAll(ids.subList(split, ids.size()));
        return train(bases);
    }

    @NotNull
    public List<Integer> getBases() {
        return bases;
    }

    @NotNull
    public List<Integer> getOutliers() {
        return outliers;
    }

    @NotNull
    public Value getSlope() {
        return new Value(regression.getSlope(), regression.getSlopeStdErr());
    }

    @NotNull
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
