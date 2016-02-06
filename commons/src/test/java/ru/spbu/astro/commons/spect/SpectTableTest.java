package ru.spbu.astro.commons.spect;

import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import ru.spbu.astro.util.Value;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

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
    public void testBV() throws Exception {
        assertEquals(Value.of(-0.26, 0.01), spectTable.getBV(TempClass.valueOf("B1"), LuminosityClass.III));
        final Value bv = requireNonNull(spectTable.getBV(TempClass.valueOf("B0.5"), LuminosityClass.III));
        assertEquals(-0.28, bv.val(), EPS);
        assertEquals(0.01, bv.err(), EPS);
        assertNull(spectTable.getBV(TempClass.valueOf("O4"), LuminosityClass.V));
    }

    @Test
    @Ignore
    public void testDiff() throws FileNotFoundException {
        final SpectTable table1 = SpectTable.STRIGEST;
        final SpectTable table2 = SpectTable.BINNEY_MERRIFIELD;
        final PrintWriter fout = new PrintWriter("docs/articles/dust/other/bv-err/bv-diff.txt");
        for (final LuminosityClass lumin : LuminosityClass.MAIN) {
            for (final int code : Sets.intersection(table1.getBVs(lumin).keySet(), table2.getBVs(lumin).keySet())) {
                final TempClass spect = TempClass.valueOf(code);
                final Value bv1 = table1.getBV(spect, lumin);
                final Value bv2 = table2.getBV(spect, lumin);
                if (bv1 != null && bv2 != null) {
                    fout.println(String.format("%s\t%s\t%.2f", lumin, spect, bv1.val() - bv2.val()));
                }
            }
        }
        fout.close();
    }
}