package ru.spbu.astro.util.ml;

import com.google.common.primitives.Ints;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.util.MathTools;
import ru.spbu.astro.util.Value;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * User: amosov-f
 * Date: 09.04.15
 * Time: 3:40
 */
public class TheilSenWeightedRegression implements SimpleRegression {
    @NotNull
    private final List<Integer> ids = new ArrayList<>();
    @NotNull
    private final List<Pair<Double, Double>> ks = new ArrayList<>();

    private double k;

    @NotNull
    @Override
    public Value getSlope() {
        return Value.of(k);
    }

    @NotNull
    @Override
    public Value getIntercept() {
        return Value.ZERO;
    }

    @NotNull
    @Override
    public int[] getInliers() {
        return Ints.toArray(ids);
    }

    @NotNull
    @Override
    public int[] getOutliers() {
        return ArrayUtils.EMPTY_INT_ARRAY;
    }

    @Override
    public boolean train() {
        final int n = ks.size();
        if (n < 5) {
            return false;
        }
        ks.sort(Comparator.comparingDouble(Pair::getLeft));
        final double[] v = ks.stream().mapToDouble(Pair::getLeft).toArray();
        final double[] w = ks.stream().mapToDouble(Pair::getRight).toArray();
        k = MathTools.weightedMedian(v, w);
        return true;
    }

    @Override
    public void add(final int id, @NotNull final Value x, @NotNull final Value y) {
        ids.add(id);
        System.out.println(x.getError() + " " + y.getError() + " -> " +  1 / Math.pow(1 + x.getError() * y.getError(), 0.5));
        ks.add(Pair.of(y.getValue() / x.getValue(), 1 / Math.pow(1 + x.getError() * y.getError(), 0.5)));
    }
}
