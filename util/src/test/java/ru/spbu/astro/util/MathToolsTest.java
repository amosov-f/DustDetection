package ru.spbu.astro.util;

import org.junit.Test;

import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static ru.spbu.astro.util.MathTools.*;

/**
 * User: amosov-f
 * Date: 09.04.15
 * Time: 4:06
 */
@SuppressWarnings("MagicNumber")
public final class MathToolsTest {
    private static final double EPS = 1e-8;

    @Test
    public void testAverage() {
        assertEquals(5.5, average(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).val(), EPS);
        assertEquals(0.2890713700313583, average(IntStream.range(0, 10000).mapToDouble(i -> Math.random()).toArray()).err(), 0.01);
    }

    @Test
    public void testWeightedMedian() {
        assertEquals(1.6666666666666665, weightedMedian(new double[]{1, 2}, new double[]{1, 2}), EPS);
        assertEquals(1.6666666666666665, weightedMedian(new double[]{2, 1}, new double[]{2, 1}), EPS);
        assertEquals(0.1, weightedMedian(new double[]{0.1, 0.1}, new double[]{1, 1}), EPS);
        assertEquals(0.5, weightedMedian(new double[]{0.5}, new double[]{0.1}), EPS);
    }

    @Test
    public void testMedian() {
        final double[] v = IntStream.range(0, 10000).mapToDouble(i -> Math.random()).toArray();
        final double[] w = IntStream.range(0, v.length).mapToDouble(i -> 1.0).toArray();
        assertEquals(0.5, weightedMedian(v, w), 0.01);
        assertEquals(0.25, weightedMedianValue(v, w).err(), 0.01);
    }

    @Test
    public void testMAD() {
        assertEquals(1, weightedMedianValue(new double[]{1, 9, 2, 6, 4, 2, 1}, new double[]{1, 1, 1, 1, 1, 1, 1}).err(), EPS);
    }
}