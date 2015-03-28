package ru.spbu.astro.dust;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.commons.StarFilter;
import ru.spbu.astro.healpix.func.HealpixDistribution;
import ru.spbu.astro.commons.func.SphericDistribution;
import ru.spbu.astro.commons.graph.HammerProjection;
import ru.spbu.astro.dust.algo.DustTrendCalculator;
import ru.spbu.astro.dust.graph.PixPlot;

public final class DustDetectionEngine {
    private DustDetectionEngine() {
    }

    public static void main(@NotNull final String[] args) {
        final DustTrendCalculator dustTrendCalculator = new DustTrendCalculator(
                StarFilter.of(Stars.HIPPARCOS_UPDATED).parallaxRelativeError(0.25).stars()
        );
        final SphericDistribution f = new HealpixDistribution(dustTrendCalculator.getSlopes());
        final HammerProjection hammerProjection = new HammerProjection(f);
        final PixPlot pixPlot = new PixPlot(dustTrendCalculator);
        hammerProjection.setProcessor(pixPlot::plot);
        hammerProjection.setVisible(true);
    }
}
