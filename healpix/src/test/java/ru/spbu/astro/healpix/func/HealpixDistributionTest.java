package ru.spbu.astro.healpix.func;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import ru.spbu.astro.commons.Catalogs;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.commons.StarFilter;
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
        testBV(0.6, Double.MAX_VALUE);    
    }
    
    @Test
    public void testBVBefore06() {
        testBV(Double.MIN_VALUE, 0.6);
    }
    
    private void testBV(final double min, final double max) {
        final Healpix healpix = new Healpix(18);
        final Star[] stars = StarFilter.of(Catalogs.HIPPARCOS_2007).stars();
        final Star[][] rings = healpix.split(stars);
        final double[] values = new double[healpix.nPix()];
        for (int i = 0; i < values.length; i++) {
            if (rings[i].length != 0) {
                values[i] = 1.0 * StarFilter.of(rings[i]).bvColor(min, max).stars().length / rings[i].length;
            }
        }
        new HammerProjection(new HealpixDistribution(values), 0.0, 1.0).setVisible(true);
    }

    @After
    public void tearDown() throws Exception {
        Thread.sleep(Long.MAX_VALUE);
    }
}