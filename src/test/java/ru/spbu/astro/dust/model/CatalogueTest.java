package ru.spbu.astro.dust.model;

import org.junit.Test;
import ru.spbu.astro.dust.model.spect.LuminosityClass;

import static org.junit.Assert.*;

public class CatalogueTest {
    private static final double EPS = 1e-8;

    @Test
    public void testReading() {
        final Catalogue catalogue = Catalogue.HIPPARCOS_1997;
        final Star star = catalogue.get(42708);
        assert star != null;
        assertEquals(2.39, star.getParallax().getValue(), EPS);
        assertEquals(LuminosityClass.III, star.getSpectType().getLumin());
        assertEquals(61, star.getSpectType().getSpect().getCode());
        final Value bv = star.getSpectType().toBV();
        assert bv != null;
        assertEquals(1.584, bv.getValue(), EPS);
        assertEquals(302.39620338, Math.toDegrees(star.getDir().getL()), EPS);
        assertEquals(-26.87679541, Math.toDegrees(star.getDir().getB()), EPS);
        assertEquals(42708, star.getId());
        assertEquals(7.20, star.getVMag(), EPS);
        assertEquals(0.60, star.getParallax().getError(), EPS);
        assertEquals(1.712, star.getBVColor().getValue(), EPS);
        assertEquals(0.013, star.getBVColor().getError(), EPS);
    }
}