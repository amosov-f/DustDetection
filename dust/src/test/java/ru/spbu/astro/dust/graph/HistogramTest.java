package ru.spbu.astro.dust.graph;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import ru.spbu.astro.commons.Stars;
import ru.spbu.astro.commons.graph.Histogram;
import ru.spbu.astro.commons.hist.AverageHist;
import ru.spbu.astro.commons.hist.CountHist;
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

    @After
    public void tearDown() throws InterruptedException {
        Thread.sleep(Long.MAX_VALUE);
    }
}
