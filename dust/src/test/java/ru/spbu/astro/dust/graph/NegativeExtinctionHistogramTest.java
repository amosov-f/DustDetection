package ru.spbu.astro.dust.graph;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import ru.spbu.astro.commons.StarFilter;
import ru.spbu.astro.commons.hist.CountHist;
import ru.spbu.astro.commons.hist.SpectClassHist;
import ru.spbu.astro.dust.Stars;
import ru.spbu.astro.util.Split;

import static ru.spbu.astro.commons.spect.LuminosityClass.III;
import static ru.spbu.astro.commons.spect.LuminosityClass.V;

@Ignore
@SuppressWarnings("MagicNumber")
public class NegativeExtinctionHistogramTest {
    @Test
    public void histParallaxRelativeError() {
        new NegativeExtinctionHistogram(
                Stars.HIPPARCOS_UPDATED,
                new CountHist("Относительная ошибка в расстоянии", star -> star.getR().getRelativeError(), new Split(0.1))
        ).show();
    }

    @Test
    public void histBVError() {
        new NegativeExtinctionHistogram(
                Stars.HIPPARCOS_UPDATED,
                new CountHist("Ошибка B-V", star -> star.getBVColor().getValue(), new Split(0, 2.3, 0.2))
        ).show();
    }

    @Test
    public void histSpectType() {
        new NegativeExtinctionHistogram(Stars.HIPPARCOS_UPDATED, new SpectClassHist(5)).show();
    }

    @Test
    public void histSpectTypeOfV() {
        new NegativeExtinctionHistogram(
                StarFilter.of(Stars.HIPPARCOS_UPDATED).luminosityClass(V).stars(),
                new SpectClassHist(5)
        ).show();
    }

    @Test
    public void histSpectTypeOfIII() {
        new NegativeExtinctionHistogram(
                StarFilter.of(Stars.HIPPARCOS_UPDATED).luminosityClass(III).stars(),
                new SpectClassHist(5)
        ).show();
    }

    @Test
    public void histBVErrorOfIIIOfM59() {
        new NegativeExtinctionHistogram(
                StarFilter.of(Stars.HIPPARCOS_UPDATED)
                        .luminosityClass(III)
                        .spectType(65, 69)
                        .stars(),
                new CountHist("Ошибка B-V у звезд III, M5-9", star -> star.getBVColor().getValue(), new Split(0, 2.3, 0.2))
        ).show();
    }

    @Test
    public void histBVErrorOfIII() {
        new NegativeExtinctionHistogram(
                StarFilter.of(Stars.HIPPARCOS_UPDATED)
                        .luminosityClass(III)
                        .stars(),
                new CountHist("Ошибка B-V у звезд III", star -> star.getBVColor().getValue(), new Split(0, 2.3, 0.2))
        ).show();
    }

    @Test
    public void histParallaxRelativeErrorOfIII() {
        new NegativeExtinctionHistogram(
                StarFilter.of(Stars.HIPPARCOS_UPDATED)
                        .luminosityClass(III).stars(),
                new CountHist("Относительная ошибка в расстоянии у III", star -> star.getR().getRelativeError(), new Split(0.1))
        ).show();
    }

    @Test
    public void histParallaxRelativeErrorOfIIIOfM59() {
        new NegativeExtinctionHistogram(
                StarFilter.of(Stars.HIPPARCOS_UPDATED)
                        .luminosityClass(III)
                        .spectType(65, 69)
                        .stars(),
                new CountHist("Относительная ошибка в расстоянии у звезд III, M5-9", star -> star.getR().getRelativeError(), new Split(0.1))
        ).show();
    }

    @After
    public void tearDown() throws InterruptedException {
        Thread.sleep(Long.MAX_VALUE);
    }
}