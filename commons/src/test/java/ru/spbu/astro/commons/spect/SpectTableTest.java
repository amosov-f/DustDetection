package ru.spbu.astro.commons.spect;

import org.junit.Before;
import org.junit.Test;
import ru.spbu.astro.util.Value;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * User: amosov-f
 * Date: 11.05.15
 * Time: 18:15
 */
@SuppressWarnings("MagicNumber")
public final class SpectTableTest {
    private static final double EPS = 1e-8;

    private SpectTable spectTable;

    @Before
    public void setUp() {
        spectTable = SpectTable.TSVETKOV;
    }

    @Test
    public void test() throws Exception {
        assertEquals(Value.of(-0.26, 0.015), spectTable.getBV(requireNonNull(SpectClass.parse("B1")), LuminosityClass.III));
        final Value bv = requireNonNull(spectTable.getBV(requireNonNull(SpectClass.parse("B0.5")), LuminosityClass.III));
        assertEquals(-0.28, bv.val(), EPS);
        assertEquals(0.02, bv.err(), EPS);
        assertNull(spectTable.getBV(requireNonNull(SpectClass.parse("O4")), LuminosityClass.V));
    }
}