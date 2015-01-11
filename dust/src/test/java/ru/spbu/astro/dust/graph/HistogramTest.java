package ru.spbu.astro.dust.graph;

import org.junit.Ignore;
import org.junit.Test;
import ru.spbu.astro.core.count.DoubleStarCounter;
import ru.spbu.astro.dust.model.Catalogues;

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
                Catalogues.HIPPARCOS_UPDATED.getStars(),
                new DoubleStarCounter("Относительная ошибка в расстоянии", star -> star.getR().getRelativeError(), 0.1)
        );
        Thread.sleep(Long.MAX_VALUE);
    }
}
