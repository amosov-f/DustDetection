package ru.spbu.astro.commons;

import org.junit.Assert;
import org.junit.Test;

import java.util.Objects;

import static org.junit.Assert.assertEquals;

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
        Assert.assertEquals(8.7, Stars.KAPTEYNS.getProperMotion() / 1000, 0.1);
    }


    @Test
    public void testDist() {
        final Star star = Objects.requireNonNull(Stars.MAP.get(7));
        assertEquals(57.56, star.getR().val(), 0.01);
        assertEquals(6.7, 100 * star.getR().relErr(), 0.1);
    }
}