package ru.spbu.astro.dust.algo;

import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.commons.StarFilter;
import ru.spbu.astro.commons.Stars;
import ru.spbu.astro.commons.graph.HammerProjection;
import ru.spbu.astro.dust.DustStars;
import ru.spbu.astro.dust.algo.classify.ConstLuminosityClassifier;
import ru.spbu.astro.dust.algo.classify.LuminosityClassifier;
import ru.spbu.astro.dust.algo.classify.LuminosityClassifiers;
import ru.spbu.astro.healpix.func.HealpixDistribution;

/**
 * User: amosov-f
 * Date: 28.03.15
 * Time: 0:20
 */
@Ignore
@SuppressWarnings("MagicNumber")
public class DustTrendCalculatorShow {
    @Test
    public void testLeftIII() {
        test(LuminosityClassifiers.LEFT_III);
    }

    @Test
    public void testLeftV() throws Exception {
        test(LuminosityClassifiers.LEFT_V);
    }

    private void test(@NotNull final LuminosityClassifier classifier) {
        final Star[] stars = StarFilter.of(Stars.ALL).bvColor(Double.MIN_VALUE, ConstLuminosityClassifier.Left.DELIMETER).stars();
        final DustTrendCalculator calculator = new DustTrendCalculator(DustStars.by(stars, classifier));
        new HammerProjection(new HealpixDistribution(calculator.getSlopes())).setVisible(true);
    }

    @After
    public void tearDown() throws InterruptedException {
        Thread.sleep(Long.MAX_VALUE);
    }
}