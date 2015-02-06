package ru.spbu.astro.dust.graph;

import org.junit.Ignore;
import org.junit.Test;
import ru.spbu.astro.commons.graph.Histogram;
import ru.spbu.astro.commons.hist.DoubleStarHist;
import ru.spbu.astro.dust.DustCatalogues;

/**
 * User: amosov-f
 * Date: 09.01.15
 * Time: 2:20
 */
@Ignore
public class HistogramTest {
    @Test
    public void histRelativeError() throws Exception {
        new Histogram(
                DustCatalogues.HIPPARCOS_UPDATED.getStars(),
                new DoubleStarHist("Относительная ошибка в расстоянии", star -> star.getR().getRelativeError(), 0.1)
        );
        Thread.sleep(Long.MAX_VALUE);
    }
}
