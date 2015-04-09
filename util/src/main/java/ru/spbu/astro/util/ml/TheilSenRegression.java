package ru.spbu.astro.util.ml;

import com.google.common.primitives.Ints;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.util.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: amosov-f
 * Date: 09.04.15
 * Time: 3:08
 */
public class TheilSenRegression implements SimpleRegression {
    @NotNull
    private final List<Integer> ids = new ArrayList<>();
    @NotNull
    private final List<Double> ks = new ArrayList<>();

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
        if (ks.size() < 5) {
            return false;
        }
        Collections.sort(ks);
        k = ks.get(ks.size() / 2);
        return true;
    }

    @Override
    public void add(final int id, @NotNull final Value x, @NotNull final Value y) {
        ids.add(id);
        ks.add(y.getValue() / x.getValue());
    }
}
