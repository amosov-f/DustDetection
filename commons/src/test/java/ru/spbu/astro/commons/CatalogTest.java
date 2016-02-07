package ru.spbu.astro.commons;

import org.junit.Test;
import ru.spbu.astro.commons.spect.LuminosityClass;
import ru.spbu.astro.util.Value;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SuppressWarnings("MagicNumber")
public final class CatalogTest {
    private static final double EPS = 1e-8;

    @Test
    public void testReading() {
        final Star star = LegacyCatalogs.HIPPARCOS.get(42708);
        assertNotNull(star);
        assertEquals(2.39, star.getParallax().val(), EPS);
        assertEquals(LuminosityClass.III, star.getSpectType().getLumin());
        assertEquals(71, star.getSpectType().getTemp().getCode());
        final Value bv = star.getSpectType().toBV();
        assertNotNull(bv);
        assertEquals(1.584, bv.val(), EPS);
        assertEquals(302.39620338, Math.toDegrees(star.getDir().getL()), EPS);
        assertEquals(-26.87679541, Math.toDegrees(star.getDir().getB()), EPS);
        assertEquals(42708, star.getId());
        assertEquals(7.20, star.getVMag(), EPS);
        assertEquals(0.60, star.getParallax().err(), EPS);
        assertEquals(1.712, star.getBVColor().val(), EPS);
        assertEquals(0.013, star.getBVColor().err(), EPS);
    }

    @Test
    public void testInnerJoin() {
        final Star star = LegacyCatalogs.HIPPARCOS.get(71729);
        assertNotNull(star);
        assertEquals(9.47, star.getParallax().val(), EPS);
        assertEquals("G0V", star.getSpectType().toString());
        final Star updatedStar = LegacyCatalogs.HIPNEWCAT.get(71729);
        assertNotNull(updatedStar);
        assertEquals(8.60, updatedStar.getParallax().val(), EPS);
        assertEquals("G0V", star.getSpectType().toString());
    }
}