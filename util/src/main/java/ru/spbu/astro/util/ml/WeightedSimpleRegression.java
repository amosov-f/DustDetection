package ru.spbu.astro.util.ml;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.util.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * User: amosov-f
 * Date: 11.01.15
 * Time: 23:01
 */
public final class WeightedSimpleRegression implements SimpleRegression {
    @NotNull
    private final List<Integer> ids = new ArrayList<>();
    @NotNull
    private final List<Double> xs = new ArrayList<>();
    @NotNull
    private final List<Double> ys = new ArrayList<>();
    @NotNull
    private final List<Double> ws = new ArrayList<>();

    private double k;

    @NotNull
    @Override
    public Value getSlope() {
        return new Value(k);
    }

    @NotNull
    @Override
    public Value getIntercept() {
        return Value.ZERO;
    }

    @NotNull
    @Override
    public List<Integer> getInliers() {
        return ids;
    }

    @NotNull
    @Override
    public List<Integer> getOutliers() {
        return new ArrayList<>();
    }

    @Override
    public boolean train() {
        final double numerator = IntStream.range(0, ids.size() - 1).mapToDouble(i -> ws.get(i) * xs.get(i) * ys.get(i)).sum();
        final double denumenator = IntStream.range(0, ids.size() - 1).mapToDouble(i -> ws.get(i) * Math.pow(xs.get(i), 2)).sum();
        k = numerator / denumenator;
        return true;
    }

    @Override
    public void add(final int id, @NotNull final Value x, @NotNull final Value y) {
        ids.add(id);
        xs.add(x.getValue());
        ys.add(y.getValue());
        ws.add(1 / (1 + Math.sqrt(x.getError() * y.getError())));
    }
}
