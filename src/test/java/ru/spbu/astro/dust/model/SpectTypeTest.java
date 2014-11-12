package ru.spbu.astro.dust.model;

import org.junit.Test;
import ru.spbu.astro.dust.model.spect.SpectType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static ru.spbu.astro.dust.model.spect.LuminosityClass.III;
import static ru.spbu.astro.dust.model.spect.SpectClass.TypeSymbol.M;

public class SpectTypeTest {
    @Test
    public void testParsing1() throws Exception {
        final SpectType spectralType = SpectType.parse("K5III");
        assert spectralType != null;
        assertEquals(III, spectralType.getLumin());
    }

    @Test
    public void testParsing2() throws Exception {
        final SpectType spectralType = SpectType.parse("Mb");
        assert spectralType != null;
        assertEquals(M, spectralType.getTypeSymbol());
        assertNull(spectralType.toBV());
    }
}