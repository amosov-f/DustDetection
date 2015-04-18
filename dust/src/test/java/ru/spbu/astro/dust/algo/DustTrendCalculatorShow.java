package ru.spbu.astro.dust.algo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
import ru.spbu.astro.util.Filter;
import ru.spbu.astro.util.Value;

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
        final DustTrendCalculator calculator = new DustTrendCalculator(
                StarFilter.of(DustStars.ALL).piRelErr(0.25).stars(), N_SIDE
        );
        final SphericDistribution f = new HealpixDistribution(calculator.getSlopes());
        System.out.println(calculator.getSlopes().length);
        final HammerProjection hammerProjection = new HammerProjection(f);
        final PixPlot pixPlot = new PixPlot(calculator);
        hammerProjection.setProcessor(pixPlot::plot);
        hammerProjection.setVisible(true);
    }

    @Test
    public void showHasLumin() throws Exception {
        show(StarFilter.of(Stars.ALL).hasExt().stars(), null);
    }

    @Test
    public void leftShow() {
        show(StarFilter.of(DustStars.ALL).leftBV().stars(), null);
    }

    @Test
    public void rightShow() {
        show(StarFilter.of(DustStars.ALL).rightBV().stars(), null);
    }

    @Test
    public void showAll() {
        show(DustStars.ALL, null);
    }

    @Test
    public void showTwoSigma() {
        show(DustStars.ALL, Value.POS_TWO_SIGMA);
    }

    @Test
    public void showErrLimit() {
        show(DustStars.ALL, Value.filterByErr(0.00005));
    }

    @Test
    public void showTwoSigmaOrErrLimit() throws Exception {
        show(DustStars.ALL, Value.POS_TWO_SIGMA.or(Value.filterByErr(0.00005)));
    }
    
    private void show(@NotNull final Star[] stars, @Nullable final Filter<Value> filter) {
        final DustTrendCalculator calculator = new DustTrendCalculator(stars, N_SIDE);
        new HammerProjection(new HealpixDistribution(calculator.getSlopes(), filter), MIN_VALUE, MAX_VALUE).setVisible(true);
        System.out.println(calculator.toString(filter));
    }

    @Test
    public void testSmoothed() {
        final DustTrendCalculator calculator = new DustTrendCalculator(DustStars.ALL, N_SIDE);
        new HammerProjection(new SmoothedDistribution(64, new HealpixDistribution(calculator.getSlopes()), 2)).setVisible(true);
    }

    @Test
    public void showErrors() {
        final DustTrendCalculator calculator = new DustTrendCalculator(DustStars.ALL, N_SIDE);
        new HammerProjection(new HealpixDistribution(calculator.getSlopes()), MIN_VALUE, MAX_VALUE, HammerProjection.Mode.WITH_ERRORS).setVisible(true);
    }

    @After
    public void tearDown() throws InterruptedException {
        Thread.sleep(Long.MAX_VALUE);
    }
}