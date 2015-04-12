package ru.spbu.astro.util.ml;

import org.junit.Assert;
import org.junit.Test;
import ru.spbu.astro.util.Point;
import ru.spbu.astro.util.Value;

/**
 * User: amosov-f
 * Date: 12.04.15
 * Time: 21:48
 */
@SuppressWarnings("MagicNumber")
public final class WeightedMedianRegressionTest {
    @Test
    public void test() {
        Assert.assertEquals(100, new WeightedMedianRegression(new Point(0.01, 1), new Point(Value.of(0.01), Value.of(1, 100))).getSlope().val(), 1e-8);
    }
}