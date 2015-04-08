package ru.spbu.astro.dust.algo;

import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.commons.StarFilter;
import ru.spbu.astro.commons.Stars;
import ru.spbu.astro.commons.func.SphericDistribution;
import ru.spbu.astro.commons.graph.HammerProjection;
import ru.spbu.astro.dust.DustStars;
import ru.spbu.astro.dust.graph.PixPlot;
import ru.spbu.astro.healpix.func.HealpixDistribution;
import ru.spbu.astro.healpix.func.SmoothedDistribution;

/**
 * User: amosov-f
 * Date: 28.03.15
 * Time: 0:20
 */
@Ignore
@SuppressWarnings("MagicNumber")
public class DustTrendCalculatorShow {
    private static final int N_SIDE = 18;

    private static final double MIN_VALUE = -0.001;
    private static final double MAX_VALUE = 0.002;

    @Test
    public void main() throws Exception {
        final DustTrendCalculator dustTrendCalculator = new DustTrendCalculator(
                StarFilter.of(DustStars.ALL).piRelErr(0.25).stars(), N_SIDE
        );
        final SphericDistribution f = new HealpixDistribution(dustTrendCalculator.getSlopes());
        final HammerProjection hammerProjection = new HammerProjection(f);
        final PixPlot pixPlot = new PixPlot(dustTrendCalculator);
        hammerProjection.setProcessor(pixPlot::plot);
        hammerProjection.setVisible(true);
    }

    @Test
    public void leftShow() {
        show(StarFilter.of(DustStars.ALL).leftBV().stars());
    }

    @Test
    public void rightShow() {
        show(StarFilter.of(DustStars.ALL).rightBV().stars());
    }


    @Test
    public void showHasLumin() throws Exception {
        show(StarFilter.of(Stars.ALL).hasExt().stars());
    }

    @Test
    public void show() {
        show(DustStars.ALL);
    }

    @Test
    public void testSmoothed() {
        final DustTrendCalculator calculator = new DustTrendCalculator(DustStars.ALL, N_SIDE);
        new HammerProjection(new SmoothedDistribution(64, new HealpixDistribution(calculator.getSlopes()), 2)).setVisible(true);
    }
    
    private void show(@NotNull final Star[] stars) {
        final DustTrendCalculator calculator = new DustTrendCalculator(stars, N_SIDE);
        new HammerProjection(new HealpixDistribution(calculator.getSlopes()), MIN_VALUE, MAX_VALUE).setVisible(true);
    }

    @After
    public void tearDown() throws InterruptedException {
        Thread.sleep(Long.MAX_VALUE);
    }
}