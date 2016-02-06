package ru.spbu.astro.dust.graph;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.commons.StarFilter;
import ru.spbu.astro.commons.Stars;
import ru.spbu.astro.commons.graph.Histogram;
import ru.spbu.astro.commons.hist.*;
import ru.spbu.astro.commons.spect.LuminosityClass;
import ru.spbu.astro.dust.DustStars;
import ru.spbu.astro.dust.algo.DustTrendCalculator;
import ru.spbu.astro.util.Split;

/**
 * User: amosov-f
 * Date: 09.01.15
 * Time: 2:20
 */
@Ignore
@SuppressWarnings("MagicNumber")
public class HistogramTest {
    @Test
    public void histRelativeError() {
        new Histogram<>(
                new CountHist(
                        "Относительная ошибка в расстоянии", 
                        star -> star.getR().relErr(),
                        new Split(0.1)).histShares(Stars.ALL),
                "Относительная ошибка в расстоянии"
        ).show();
    }

    @Test
    public void test() {
        new Histogram<>(
                new AverageHist(
                        "Расстояние", 
                        star -> star.getR().val(),
                        star -> star.getR().relErr(),
                        new Split(0, 1000, 10)
                ).hist(Stars.ALL),
                "Расстояние [пк]", 
                "Относительная ошибка в расстоянии",
                true
        ).show();
    }

    @Test
    public void testDeep() throws Exception {
        final Star[] stars = new DustTrendCalculator(DustStars.ALL, 18).getInliers();

        new Histogram<>(
                new CountHist(
                        "Расстояние [пк]",
                        star -> star.getR().val(),
                        new Split(0, 1000, 10)).histShares(stars),
                "Расстояние [пк]"
        ).show();
    }

    @Test
    public void showBVObsErr() {
        new Histogram<>(
                new AverageHist("pizza", Star::getVMag, s -> s.getBVColor().err(), new Split(2, 13, 9)).hist(Stars.ALL),
                "Видимая зв. вел.",
                "Ошибка B-V obs",
                false
        ).show();
    }

    @Test
    public void showBVIntErr() {
        new Histogram<>(
                new AverageHist("pizza", Star::getVMag, s -> s.getSpectType().toBV().err(), new Split(2, 13, 9)).hist(Stars.ALL),
                "Видимая зв. вел.",
                "Ошибка B-V int",
                false
        ).show();
    }

    @Test
    public void showExtErr() {
        new Histogram<>(
                new AverageHist("pizza", Star::getVMag, s -> s.getExtinction().err(), new Split(2, 13, 9)).hist(DustStars.ALL),
                "Видимая зв. вел.",
                "Ошибка покраснения",
                false
        ).show();
    }

    @Test
    public void showVMag() throws Exception {
        final CountHist hist = new CountHist("Расстояние [пк]", Star::getVMag, new Split(2, 13, 9));
        new Histogram<>(hist.histShares(Stars.ALL), "Видимая зв. вел.").show();
    }

    @Test
    public void showProperMotion() {
        new Histogram<>(
                new LuminosityClassHist<>(new Average<>(Star::getProperMotion)).hist(Stars.ALL),
                "Класс светимости",
                "Собственное движение, mas/yr",
                false
        ).show();
    }

    @Test
    public void showGiantProperMotion() {
        final Star[] stars = StarFilter.of(Stars.ALL).lumin(LuminosityClass.III).stars();
        final AverageHist hist = new AverageHist("pizza", s -> s.getR().val(), Star::getProperMotion, new Split(0, 400, 10));
        new Histogram<>(hist.hist(stars), "Расстояние [пк]", "Собственное движение, mas/yr", false).show();
    }

    @Test
    public void showProperMotions() throws Exception {

    }

    @After
    public void tearDown() throws InterruptedException {
        Thread.sleep(Long.MAX_VALUE);
    }
}
