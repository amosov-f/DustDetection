package ru.spbu.astro.dust.graph;

import org.junit.Ignore;
import org.junit.Test;
import ru.spbu.astro.dust.DustCatalogues;
import ru.spbu.astro.core.StarFilter;
import ru.spbu.astro.core.hist.DoubleStarHist;
import ru.spbu.astro.core.hist.SpectClassHist;

import static ru.spbu.astro.core.spect.LuminosityClass.III;
import static ru.spbu.astro.core.spect.LuminosityClass.V;

@Ignore
public class NegativeExtinctionHistogramTest {
    @Test
    public void histParallaxRelativeError() throws InterruptedException {
        new NegativeExtinctionHistogram(
                DustCatalogues.HIPPARCOS_UPDATED.getStars(),
                new DoubleStarHist("Относительная ошибка в расстоянии", star -> star.getR().getRelativeError(), 0.1)
        );
        Thread.sleep(Long.MAX_VALUE);
    }

    @Test
    public void histBVError() throws InterruptedException {
        new NegativeExtinctionHistogram(
                DustCatalogues.HIPPARCOS_UPDATED.getStars(),
                new DoubleStarHist("Ошибка B-V", star -> star.getBVColor().getValue(), 0, 2.3, 0.2)
        );
        Thread.sleep(Long.MAX_VALUE);
    }

    @Test
    public void histSpectType() throws InterruptedException {
        new NegativeExtinctionHistogram(DustCatalogues.HIPPARCOS_UPDATED.getStars(), new SpectClassHist(5));
        Thread.sleep(Long.MAX_VALUE);
    }

    @Test
    public void histSpectTypeOfV() throws InterruptedException {
        new NegativeExtinctionHistogram(
                new StarFilter(DustCatalogues.HIPPARCOS_UPDATED).luminosityClass(V).getStars(),
                new SpectClassHist(5)
        );
        Thread.sleep(Long.MAX_VALUE);
    }

    @Test
    public void histSpectTypeOfIII() throws InterruptedException {
        new NegativeExtinctionHistogram(
                new StarFilter(DustCatalogues.HIPPARCOS_UPDATED).luminosityClass(III).getStars(),
                new SpectClassHist(5)
        );
        Thread.sleep(Long.MAX_VALUE);
    }

    @Test
    public void histBVErrorOfIIIOfM59() throws InterruptedException {
        new NegativeExtinctionHistogram(
                new StarFilter(DustCatalogues.HIPPARCOS_UPDATED)
                        .luminosityClass(III)
                        .spectType(65, 69)
                        .getStars(),
                new DoubleStarHist("Ошибка B-V у звезд III, M5-9", star -> star.getBVColor().getValue(), 0, 2.3, 0.2)
        );
        Thread.sleep(Long.MAX_VALUE);
    }

    @Test
    public void histBVErrorOfIII() throws InterruptedException {
        new NegativeExtinctionHistogram(
                new StarFilter(DustCatalogues.HIPPARCOS_UPDATED)
                        .luminosityClass(III)
                        .getStars(),
                new DoubleStarHist("Ошибка B-V у звезд III", star -> star.getBVColor().getValue(), 0, 2.3, 0.2)
        );
        Thread.sleep(Long.MAX_VALUE);
    }

    @Test
    public void histParallaxRelativeErrorOfIII() throws InterruptedException {
        new NegativeExtinctionHistogram(
                new StarFilter(DustCatalogues.HIPPARCOS_UPDATED)
                        .luminosityClass(III).getStars(),
                new DoubleStarHist("Относительная ошибка в расстоянии у III", star -> star.getR().getRelativeError(), 0.1)
        );
        Thread.sleep(Long.MAX_VALUE);
    }

    @Test
    public void histParallaxRelativeErrorOfIIIOfM59() throws InterruptedException {
        new NegativeExtinctionHistogram(
                new StarFilter(DustCatalogues.HIPPARCOS_UPDATED)
                        .luminosityClass(III)
                        .spectType(65, 69)
                        .getStars(),
                new DoubleStarHist("Относительная ошибка в расстоянии у звезд III, M5-9", star -> star.getR().getRelativeError(), 0.1)
        );
        Thread.sleep(Long.MAX_VALUE);
    }
}