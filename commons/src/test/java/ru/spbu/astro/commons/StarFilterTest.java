package ru.spbu.astro.commons;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class StarFilterTest {
    @Test
    public void test() throws Exception {
        assertTrue(StarFilter.of(Catalogs.HIPPARCOS_2007)
                .hasBVInt()
                .negativeExtinction()
                .filter("ext > 0", star -> star.getExtinction().getValue() >= 0).stars().length == 0);
    }
}