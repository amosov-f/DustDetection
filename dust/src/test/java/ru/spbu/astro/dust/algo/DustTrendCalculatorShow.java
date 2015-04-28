package ru.spbu.astro.dust.algo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Before;
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
import ru.spbu.astro.util.ImageTools;
import ru.spbu.astro.util.Value;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

/**
 * User: amosov-f
 * Date: 28.03.15
 * Time: 0:20
 */
@Ignore
@SuppressWarnings("MagicNumber")
public class DustTrendCalculatorShow {
    private static final int N_SIDE = 18;

    private static final double MIN_VALUE = -1;
    private static final double MAX_VALUE = 2;

    private HammerProjection hasExt;
    private HammerProjection left;
    private HammerProjection right;
    private HammerProjection full;
    private HammerProjection twoSigma;

    @Before
    public void setUp() {
        hasExt = map(StarFilter.of(Stars.ALL).hasExt().stars(), Filter.by("non null", Objects::nonNull));
        left =  map(StarFilter.of(DustStars.ALL).leftBV().stars(), null);
        right = map(StarFilter.of(DustStars.ALL).rightBV().stars(), null);
        full = map(DustStars.ALL, null);
        twoSigma = map(DustStars.ALL, Value.POS_TWO_SIGMA);
    }

    @Test
    public void main() throws Exception {
        final DustTrendCalculator calculator = new DustTrendCalculator(
                DustStars.ALL, N_SIDE
        );
        final SphericDistribution f = new HealpixDistribution(calculator.getSlopes());
        System.out.println(calculator.getSlopes().length);
        final HammerProjection hammerProjection = new HammerProjection(f);
        final PixPlot pixPlot = new PixPlot(calculator);
        hammerProjection.setProcessor(pixPlot::plot);
        hammerProjection.setVisible(true);
    }

    @Test
    public void showHasExt() throws Exception {
        show(hasExt);
    }

    @Test
    public void showLeft() throws IOException, InterruptedException {
        show(left);
    }

    @Test
    public void showRight() throws IOException, InterruptedException {
        show(right);
    }

    @Test
    public void showFull() throws IOException, InterruptedException {
        show(full);
    }

    @Test
    public void showTwoSigma() throws IOException, InterruptedException {
        show(twoSigma);
    }
    
    private void show(@NotNull final HammerProjection map) throws IOException, InterruptedException {
        map.setVisible(true);
        ImageTools.saveAsPNG(map, "docs/articles/dust/text/buffer.png");
        Thread.sleep(Long.MAX_VALUE);
    }

    @Test
    public void generatePictures() throws IOException {
        final String dir = "docs/articles/dust/buffer/";
        final String format = "png";
        ImageTools.saveAsPNG(hasExt, dir + "map-k-has-ext." + format);
        ImageTools.saveAsPNG(left, dir + "map-k-left." + format);
        ImageTools.saveAsPNG(right, dir + "map-k-right." + format);
        ImageTools.saveAsPNG(full, dir + "map-k." + format);
        ImageTools.saveAsPNG(twoSigma, dir + "map-k-2sigma." + format);
    }

    @NotNull
    private static HammerProjection map(@NotNull final Star[] stars, @Nullable final Filter<Value> slopeFilter) {
        final DustTrendCalculator calculator = new DustTrendCalculator(stars, N_SIDE);
        System.out.println(calculator.toString(slopeFilter));
        return new HammerProjection(new HealpixDistribution(Value.multiply(calculator.getSlopes(), 1000), slopeFilter), MIN_VALUE, MAX_VALUE);
    }

    @Test
    public void bvTrendShow() {
        final DustTrendCalculator calculator = new DustTrendCalculator(StarFilter.of(DustStars.ALL).leftBV().stars(), N_SIDE, Star::getBVColor, true);
        new HammerProjection(new HealpixDistribution(calculator.getIntercepts())).setVisible(true);
    }

    @Test
    public void showSNR() {
        final DustTrendCalculator calculator = new DustTrendCalculator(DustStars.ALL, N_SIDE);
        new HammerProjection(new HealpixDistribution(Arrays.stream(calculator.getSlopes()).mapToDouble(Value::snr).toArray())).setVisible(true);
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
}