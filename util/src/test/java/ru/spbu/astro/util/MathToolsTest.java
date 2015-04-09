package ru.spbu.astro.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * User: amosov-f
 * Date: 09.04.15
 * Time: 4:06
 */
@SuppressWarnings("MagicNumber")
public class MathToolsTest {
    private static final double EPS = 1e-8;

    @Test
    public void testWeightedMedian() throws Exception {
        assertEquals(1.6666666666666665, MathTools.weightedMedian(new double[]{1, 2}, new double[]{1, 2}), EPS);
        assertEquals(0.5, MathTools.weightedMedian(new double[]{0.5}, new double[]{0.1}), EPS);
    }
}