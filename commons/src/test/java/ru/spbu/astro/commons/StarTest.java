package ru.spbu.astro.commons;

import org.junit.Assert;
import org.junit.Test;

/**
 * User: amosov-f
 * Date: 31.01.16
 * Time: 19:46
 */
public final class StarTest {
    @Test
    public void testProperMotion() throws Exception {
        // Barnard's Star, http://www.solstation.com/stars/barnards.htm
        Assert.assertEquals(10.3, Stars.BARNARDS.getProperMotion() / 1000, 0.1);
    }
}