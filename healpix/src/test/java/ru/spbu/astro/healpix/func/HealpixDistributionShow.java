package ru.spbu.astro.healpix.func;

import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.commons.StarFilter;
import ru.spbu.astro.commons.Stars;
import ru.spbu.astro.commons.graph.HammerProjection;
import ru.spbu.astro.util.Filter;

/**
 * User: amosov-f
 * Date: 27.03.15
 * Time: 0:13
 */
@Ignore
@SuppressWarnings("MagicNumber")
public class HealpixDistributionShow {
    @Test
    public void showBVOver06() {
        bvShow(0.6, Double.POSITIVE_INFINITY);
    }
    
    @Test
    public void showBVBefore06() throws Exception {
        bvShow(Double.NEGATIVE_INFINITY, 0.6);
    }

    @Test
    public void hasLuminShow() {
        show(StarFilter.HAS_LUMIN);
    }
    
    private void bvShow(final double min, final double max) {
        show(StarFilter.byBV(min, max).and(StarFilter.HAS_LUMIN.negate()));
    }

    private void show(@NotNull final Filter<Star> filter) {
        new HammerProjection(new PredicateDistribution(18, Stars.ALL, filter), 0.0, 1.0).setVisible(true);
    }

    @After
    public void tearDown() throws InterruptedException {
        Thread.sleep(Long.MAX_VALUE);
    }
}