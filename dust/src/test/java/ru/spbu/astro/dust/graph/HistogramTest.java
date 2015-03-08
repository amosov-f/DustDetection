package ru.spbu.astro.dust.graph;

import org.junit.Ignore;
import org.junit.Test;
import ru.spbu.astro.commons.Catalogs;
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
public class HistogramTest {
    @Test
    public void histRelativeError() throws Exception {
        new Histogram<>(
                new CountHist(
                        "Относительная ошибка в расстоянии", 
                        star -> star.getR().getRelativeError(), 
                        new Split(0.1)).histShares(Catalogs.HIPPARCOS_2007.getStars()
                ),
                "Относительная ошибка в расстоянии"
        ).show();
        Thread.sleep(Long.MAX_VALUE);
    }

    @Test
    public void test() throws Exception {
        new Histogram<>(
                new AverageHist(
                        "Расстояние", 
                        star -> star.getR().getValue(), 
                        star -> star.getR().getRelativeError(), 
                        new Split(0, 1000, 10)
                ).hist(Catalogs.HIPPARCOS_2007.getAllStars()), 
                "Расстояние [пк]", 
                "Относительная ошибка в расстоянии",
                true
        ).show();
        Thread.sleep(Long.MAX_VALUE);
    }
}
