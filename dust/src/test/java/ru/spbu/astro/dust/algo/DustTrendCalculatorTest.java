package ru.spbu.astro.dust.algo;

import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import ru.spbu.astro.commons.Catalogs;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.commons.StarFilter;
import ru.spbu.astro.commons.graph.HammerProjection;
import ru.spbu.astro.healpix.func.HealpixDistribution;

/**
 * User: amosov-f
 * Date: 28.03.15
 * Time: 0:20
 */
@Ignore
@SuppressWarnings("MagicNumber")
public class DustTrendCalculatorTest {
    @Test
    public void testIII() throws Exception {
        test(new LeftLuminosityClassifier.III());
    }

    private void test(@NotNull final LuminosityClassifier classifier) throws Exception {
        final Star[] stars = StarFilter.of(Catalogs.HIPPARCOS_2007).bvColor(Double.MIN_VALUE, 0.6).stars();
        final DustTrendCalculator calculator = new DustTrendCalculator(classifier.classify(stars));
        new HammerProjection(new HealpixDistribution(calculator.getSlopes())).setVisible(true);
    }

    @After
    public void tearDown() throws Exception {
        Thread.sleep(Long.MAX_VALUE);
    }
}