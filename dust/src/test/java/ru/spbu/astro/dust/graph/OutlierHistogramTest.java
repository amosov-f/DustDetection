package ru.spbu.astro.dust.graph;

import org.junit.Ignore;
import org.junit.Test;
import ru.spbu.astro.dust.model.Catalogue;
import ru.spbu.astro.dust.util.StarSelector;
import ru.spbu.astro.core.count.DoubleStarCounter;
import ru.spbu.astro.core.count.SpectClassCounter;

import static ru.spbu.astro.core.spect.LuminosityClass.III;
import static ru.spbu.astro.core.spect.LuminosityClass.V;
import static ru.spbu.astro.core.spect.SpectClass.TypeSymbol.M;

@Ignore
public class OutlierHistogramTest {
    @Test
    public void histParallaxRelativeError() throws InterruptedException {
        new OutlierHistogram(
                Catalogue.HIPPARCOS_UPDATED.getStars(),
                new DoubleStarCounter("Относительная ошибка в расстоянии", star -> star.getR().getRelativeError(), 0.1)
        );
        Thread.sleep(Long.MAX_VALUE);
    }

    @Test
    public void histBVError() throws InterruptedException {
        new OutlierHistogram(
                Catalogue.HIPPARCOS_UPDATED.getStars(),
                new DoubleStarCounter("Ошибка B-V", star -> star.getBVColor().getValue(), 0, 2.3, 0.2)
        );
        Thread.sleep(Long.MAX_VALUE);
    }

    @Test
    public void histSpectType() throws InterruptedException {
        new OutlierHistogram(Catalogue.HIPPARCOS_UPDATED.getStars(), new SpectClassCounter(5));
        Thread.sleep(Long.MAX_VALUE);
    }

    @Test
    public void histSpectTypeOfV() throws InterruptedException {
        new OutlierHistogram(
                new StarSelector(Catalogue.HIPPARCOS_UPDATED).luminosityClass(V).getStars(),
                new SpectClassCounter(5)
        );
        Thread.sleep(Long.MAX_VALUE);
    }

    @Test
    public void histSpectTypeOfIII() throws InterruptedException {
        new OutlierHistogram(
                new StarSelector(Catalogue.HIPPARCOS_UPDATED).luminosityClass(III).getStars(),
                new SpectClassCounter(5)
        );
        Thread.sleep(Long.MAX_VALUE);
    }

    @Test
    public void histBVErrorOfIIIOfM59() throws InterruptedException {
        new OutlierHistogram(
                new StarSelector(Catalogue.HIPPARCOS_UPDATED)
                        .luminosityClass(III)
                        .spectType(M, 5, 9)
                        .getStars(),
                new DoubleStarCounter("Ошибка B-V у звезд III, M5-9", star -> star.getBVColor().getValue(), 0, 2.3, 0.2)
        );
        Thread.sleep(Long.MAX_VALUE);
    }

    @Test
    public void histBVErrorOfIII() throws InterruptedException {
        new OutlierHistogram(
                new StarSelector(Catalogue.HIPPARCOS_UPDATED)
                        .luminosityClass(III)
                        .getStars(),
                new DoubleStarCounter("Ошибка B-V у звезд III", star -> star.getBVColor().getValue(), 0, 2.3, 0.2)
        );
        Thread.sleep(Long.MAX_VALUE);
    }

    @Test
    public void histParallaxRelativeErrorOfIII() throws InterruptedException {
        new OutlierHistogram(
                new StarSelector(Catalogue.HIPPARCOS_UPDATED)
                        .luminosityClass(III).getStars(),
                new DoubleStarCounter("Относительная ошибка в расстоянии у III", star -> star.getR().getRelativeError(), 0.1)
        );
        Thread.sleep(Long.MAX_VALUE);
    }

    @Test
    public void histParallaxRelativeErrorOfIIIOfM59() throws InterruptedException {
        new OutlierHistogram(
                new StarSelector(Catalogue.HIPPARCOS_UPDATED)
                        .luminosityClass(III)
                        .spectType(M, 5, 9)
                        .getStars(),
                new DoubleStarCounter("Относительная ошибка в расстоянии у звезд III, M5-9", star -> star.getR().getRelativeError(), 0.1)
        );
        Thread.sleep(Long.MAX_VALUE);
    }
}