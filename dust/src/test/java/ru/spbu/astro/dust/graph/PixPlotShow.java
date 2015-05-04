package ru.spbu.astro.dust.graph;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import ru.spbu.astro.commons.Spheric;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.commons.StarFilter;
import ru.spbu.astro.dust.DustStars;
import ru.spbu.astro.dust.algo.DustTrendCalculator;

/**
 * User: amosov-f
 * Date: 03.05.15
 * Time: 12:29
 */
@Ignore
@SuppressWarnings("MagicNumber")
public class PixPlotShow {
    @Test
    public void lionShow() throws Exception {
        final Spheric lion = new Spheric(Math.toRadians(220.26), Math.toRadians(52.29));
        final Star[] stars = StarFilter.of(DustStars.ALL).r(500).stars();
        final Star[] stars2 = StarFilter.of(DustStars.ALL).piRelErr(0.3).stars();
        for (double r = 1; r <= 10; r += 0.5) {
            final DustTrendCalculator.Regression regression = new DustTrendCalculator.Regression(StarFilter.of(stars).region(lion, Math.toRadians(r)).stars());
            final DustTrendCalculator.Regression regression2 = new DustTrendCalculator.Regression(StarFilter.of(stars2).region(lion, Math.toRadians(r)).stars());
            System.out.print("                    " + r + "$^circ$    &    " + regression.getStars().length + "    &    $" + regression.getSlope().kilo() + "$");
            System.out.println("    &    " + regression2.getStars().length + "    &    $" + regression2.getSlope().kilo() + "$");
        }
        final DustTrendCalculator.Regression regression = new DustTrendCalculator.Regression(StarFilter.of(stars2).region(lion, Math.toRadians(2.5)).stars());
        new ExtPlot().plot(regression, null);
    }

    @After
    public void tearDown() throws Exception {
        Thread.sleep(Long.MAX_VALUE);
    }
}