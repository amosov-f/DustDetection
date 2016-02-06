package ru.spbu.astro.commons.spect;

import org.junit.Test;
import ru.spbu.astro.util.Value;

import java.util.Objects;

import static org.junit.Assert.*;
import static ru.spbu.astro.commons.spect.LuminosityClass.III;
import static ru.spbu.astro.commons.spect.LuminosityClass.V;

@SuppressWarnings("MagicNumber")
public final class SpectTypeParserTest {
    private static final double EPS = 1e-8;

    @Test
    public void testParsing1() {
        final SpectType type = SpectTypeParser.INSTANCE.parse("K5III");
        assertNotNull(type);
        assertEquals(III, type.getLumin());
        final Value bv = type.toBV();
        assertNotNull(bv);
        assertEquals(1.450, bv.val(), EPS);
    }

    @Test
    public void testParsing2() {
        final SpectType type = SpectTypeParser.INSTANCE.parse("Mb");
        assertNull(type);
    }

    @Test
    public void testParsing3() {
        final SpectType type = SpectTypeParser.INSTANCE.parse("G8/K0III");
        assertNotNull(type);
        assertEquals(III, type.getLumin());
    }

    @Test
    public void testParsing4() throws Exception {
        final SpectType type = SpectTypeParser.INSTANCE.parse("K2");
        assertNotNull(type);
        assertEquals(TempClass.Symbol.K, type.getTemp().getSymbol());
        assertEquals(2, type.getTemp().getNumber());
        assertNull(type.getLumin());
    }

    @Test
    public void testToString() {
        final SpectType type = SpectTypeParser.INSTANCE.parse("B3/B4V");
        assertNotNull(type);
        assertEquals("B3/B4V", type.toString());
    }

    @Test
    public void testIVV() {
        final SpectType type = SpectTypeParser.INSTANCE.parse("F5IV/V");
        assertNotNull(type);
        assertEquals(V, type.getLumin());
    }

    @Test
    public void testSpace() {
        final SpectType type = Objects.requireNonNull(SpectTypeParser.INSTANCE.parse("G8/K0 III/IV"));
        assertEquals(LuminosityClass.III, type.getLumin());
    }
}