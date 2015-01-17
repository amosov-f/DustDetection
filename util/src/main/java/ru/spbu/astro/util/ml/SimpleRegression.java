package ru.spbu.astro.util.ml;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.util.Value;

import java.util.List;

/**
 * User: amosov-f
 * Date: 11.01.15
 * Time: 23:31
 */
public interface SimpleRegression {
    @NotNull
    Value getSlope();

    @NotNull
    Value getIntercept();

    @NotNull
    List<Integer> getInliers();

    @NotNull
    List<Integer> getOutliers();

    boolean train();

    void add(int id, @NotNull Value x, @NotNull Value y);
}
