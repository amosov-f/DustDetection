package ru.spbu.astro.commons;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("MagicNumber")
public class StarFilterTest {
    @Test
    public void testNoStars() throws Exception {
        assertTrue(StarFilter.of(Stars.ALL)
                .hasBVInt()
                .negativeExtinction()
                .filter("ext > 0", star -> star.getExtinction().getValue() >= 0).stars().length == 0);
    }

    @Test
    public void noLuminRegressionTest() throws Exception {
        assertEquals(24892, StarFilter.of(Stars.ALL).bvColor(Double.NEGATIVE_INFINITY, 0.6).noLumin().stars().length);
    }
}