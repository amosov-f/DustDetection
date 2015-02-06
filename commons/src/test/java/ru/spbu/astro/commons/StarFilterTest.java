package ru.spbu.astro.commons;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class StarFilterTest {
    @Test
    public void test() throws Exception {
        assertTrue(new StarFilter(Catalogs.HIPPARCOS_2007)
                .negativeExtinction()
                .filter(star -> star.getExtinction().getValue() >= 0).getStars().isEmpty());
    }
}