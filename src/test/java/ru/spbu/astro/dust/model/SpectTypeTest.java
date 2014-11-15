package ru.spbu.astro.dust.model;

import org.junit.Test;
import ru.spbu.astro.dust.model.spect.SpectType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static ru.spbu.astro.dust.model.spect.LuminosityClass.III;
import static ru.spbu.astro.dust.model.spect.SpectClass.TypeSymbol.M;

public class SpectTypeTest {
    private static final double EPS = 1e-8;

    @Test
    public void testParsing1() {
        final SpectType type = SpectType.parse("K5III");
        assert type != null;
        assertEquals(III, type.getLumin());
        assertEquals(1.450, type.toBV().getValue(), EPS);
    }

    @Test
    public void testParsing2() {
        final SpectType type = SpectType.parse("Mb");
        assertNull(type);
    }

    @Test
    public void testParsing3() {
        final SpectType type = SpectType.parse("G8/K0III");
        assert type != null;
        assertEquals(III, type.getLumin());
    }
}