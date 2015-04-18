package ru.spbu.astro.util.ml;

import com.google.common.primitives.Ints;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.util.ArrayTools;
import ru.spbu.astro.util.Point;
import ru.spbu.astro.util.Value;
import ru.spbu.astro.util.ml.target.MeanSquareError;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

/**
 * User: amosov-f
 * Date: 16.11.14
 * Time: 3:06
 */
public final class RansacLinearRegression implements LinearRegression {
    private static final double OUTLIERS_PART = 0.1;

    @NotNull
    private final SimpleRegression regression;
    @NotNull
    private final int[] inliers;
    @NotNull
    private final int[] outliers;

    public RansacLinearRegression(@NotNull final SimpleRegression regression, @NotNull final int[] inliers, @NotNull final int[] outliers) {
        this.regression = regression;
        this.inliers = inliers;
        this.outliers = outliers;
    }

    @NotNull
    public static RansacLinearRegression train(@NotNull final Map<Integer, Point> points, final boolean includeIntercept) {
        final int[] ids = Ints.toArray(points.keySet());
        final SimpleRegression regression = new SimpleRegression(includeIntercept);
        for (final int id : points.keySet()) {
            regression.addData(points.get(id).x().val(), points.get(id).y().val());
        }
        regression.regress();
        ArrayTools.sort(ids, Comparator.comparingDouble(id -> new MeanSquareError().value(
                        new RansacLinearRegression(regression, ids, ArrayUtils.EMPTY_INT_ARRAY),
                        points.get(id))
        ));
        final int split = (int) Math.ceil((1 - OUTLIERS_PART) * ids.length);
        final int[] inliers = Arrays.copyOf(ids, split);
        final int[] outliers = Arrays.copyOfRange(ids, split, ids.length);
        regression.clear();
        for (final int id : inliers) {
            regression.addData(points.get(id).x().val(), points.get(id).y().val());
        }
        return new RansacLinearRegression(regression, inliers, outliers);
    }

    @NotNull
    @Override
    public Value getSlope() {
        return Value.of(regression.getSlope(), regression.getSlopeStdErr());
    }

    @NotNull
    @Override
    public Value getIntercept() {
        return Value.of(regression.getIntercept(), regression.getInterceptStdErr());
    }

    @NotNull
    public int[] getInliers() {
        return inliers;
    }

    @NotNull
    public int[] getOutliers() {
        return outliers;
    }
}
