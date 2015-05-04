package ru.spbu.astro.dust.graph;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.commons.Spheric;
import ru.spbu.astro.dust.algo.DustTrendCalculator;
import ru.spbu.astro.healpix.Healpix;

import java.util.logging.Logger;

public final class PixPlot extends ExtPlot {
    private static final Logger LOG = Logger.getLogger(PixPlot.class.getName());

    @NotNull
    private final DustTrendCalculator dustTrendCalculator;

    public PixPlot(@NotNull final DustTrendCalculator dustTrendCalculator) {
        this.dustTrendCalculator = dustTrendCalculator;
        plot(new Spheric(0, 0));
    }

    public void plot(@NotNull final Spheric dir) {
        final DustTrendCalculator.Regression regression = dustTrendCalculator.getRegression(dir);
        if (regression != null) {
            plot(regression, "Покраснение в направлении " + new Healpix(dustTrendCalculator.getNSide()).getCenter(dir));
        }
    }
}
