package ru.spbu.astro.healpix.func;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.commons.StarFilter;
import ru.spbu.astro.commons.Stars;
import ru.spbu.astro.commons.graph.HammerProjection;
import ru.spbu.astro.healpix.Healpix;

/**
 * User: amosov-f
 * Date: 27.03.15
 * Time: 0:13
 */
@Ignore
@SuppressWarnings("MagicNumber")
public class HealpixDistributionTest {
    @Test
    public void testBVOver06() {
        testBV(0.6, Double.POSITIVE_INFINITY);
    }
    
    @Test
    public void testBVBefore06() {
        testBV(Double.NEGATIVE_INFINITY, 0.6);
    }
    
    private void testBV(final double min, final double max) {
        final Healpix healpix = new Healpix(18);
        final Star[][] rings = healpix.split(Stars.ALL);
        final double[] values = new double[healpix.nPix()];
        for (int i = 0; i < values.length; i++) {
            if (rings[i].length != 0) {
                values[i] = 1.0 * StarFilter.of(rings[i]).bvColor(min, max).noLumin().stars().length / rings[i].length;
            }
        }
        new HammerProjection(new HealpixDistribution(values), 0.0, 1.0).setVisible(true);
    }

    @After
    public void tearDown() throws Exception {
        Thread.sleep(Long.MAX_VALUE);
    }
}