package ru.spbu.astro.commons;

import org.junit.Test;
import ru.spbu.astro.commons.spect.LuminosityClass;
import ru.spbu.astro.util.Value;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CatalogueTest {
    private static final double EPS = 1e-8;

    @Test
    public void testReading() {
        final Star star = Catalogues.HIPPARCOS_1997.get(42708);
        assertNotNull(star);
        assertEquals(2.39, star.getParallax().getValue(), EPS);
        assertEquals(LuminosityClass.III, star.getSpectType().getLumin());
        assertEquals(61, star.getSpectType().getSpect().getCode());
        final Value bv = star.getSpectType().toBV();
        assertNotNull(bv);
        assertEquals(1.584, bv.getValue(), EPS);
        assertEquals(302.39620338, Math.toDegrees(star.getDir().getL()), EPS);
        assertEquals(-26.87679541, Math.toDegrees(star.getDir().getB()), EPS);
        assertEquals(42708, star.getId());
        assertEquals(7.20, star.getVMag(), EPS);
        assertEquals(0.60, star.getParallax().getError(), EPS);
        assertEquals(1.712, star.getBVColor().getValue(), EPS);
        assertEquals(0.013, star.getBVColor().getError(), EPS);
    }

    @Test
    public void testInnerJoin() {
        final Star star = Catalogues.HIPPARCOS_1997.get(71729);
        assertNotNull(star);
        assertEquals(9.47, star.getParallax().getValue(), EPS);
        assertEquals("G0V", star.getSpectType().toString());
        final Star updatedStar = Catalogues.HIPPARCOS_2007.get(71729);
        assertNotNull(updatedStar);
        assertEquals(8.60, updatedStar.getParallax().getValue(), EPS);
        assertEquals("G0V", star.getSpectType().toString());
    }
}