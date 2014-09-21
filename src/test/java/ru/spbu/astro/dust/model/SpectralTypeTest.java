package ru.spbu.astro.dust.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class SpectralTypeTest {
    @Test
    public void testParsing1() throws Exception {
        final SpectralType spectralType = SpectralType.parse("K5III");
        assert spectralType != null;
        assertEquals(SpectralType.LuminosityClass.III, spectralType.getLuminosityClass());
    }

    @Test
    public void testParsing2() throws Exception {
        final SpectralType spectralType = SpectralType.parse("Mb");
        assert spectralType != null;
        assertEquals(SpectralType.TypeSymbol.M, spectralType.getTypeSymbol());
        assertNull(spectralType.toBV());
    }
}