package ru.spbu.astro.dust.algo;

import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.commons.StarFilter;
import ru.spbu.astro.commons.Stars;
import ru.spbu.astro.commons.graph.HammerProjection;
import ru.spbu.astro.dust.DustStars;
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
        testLeft(LuminosityClassifiers.LEFT_III);
    }

    @Test
    public void testLeftV() {
        testLeft(LuminosityClassifiers.LEFT_V);
    }

    @Test
    public void testLeftSVM() {
        testLeft(LuminosityClassifiers.SVM);
    }

    @Test
    public void testRight() {
        final Star[] stars = StarFilter.of(Stars.ALL).bvColor(0.6, Double.POSITIVE_INFINITY).stars();
        test(stars, LuminosityClassifiers.createSVM(stars));
    }

    @Test
    public void testFullCombining() {
        test(Stars.ALL, LuminosityClassifiers.COMBINING);
    }

    @Test
    public void testFullSVM() throws Exception {
        test(DustStars.ALL);
    }

    @Test
    public void compareTest() throws Exception {
        Assert.assertEquals(
                new DustTrendCalculator(DustStars.ALL).toString(), 
                new DustTrendCalculator(DustStars.classified(Stars.ALL, LuminosityClassifiers.COMBINING)).toString()
        );
    }

    private void testLeft(@NotNull final LuminosityClassifier classifier) {
        testBV(Double.NEGATIVE_INFINITY, 0.6, classifier);
    }
    
    private void testBV(final double min, final double max, @NotNull final LuminosityClassifier classifier) {
        final Star[] stars = StarFilter.of(Stars.ALL).bvColor(min, max).stars();
        test(stars, classifier);
    }
    
    private void test(@NotNull final Star[] stars, @NotNull final LuminosityClassifier classifier) {
        test(DustStars.classified(stars, classifier));
    }
    
    private void test(@NotNull final Star[] stars) {
        final DustTrendCalculator calculator = new DustTrendCalculator(stars);
        new HammerProjection(new HealpixDistribution(calculator.getSlopes())).setVisible(true);
    }

    @After
    public void tearDown() throws InterruptedException {
        Thread.sleep(Long.MAX_VALUE);
    }
}