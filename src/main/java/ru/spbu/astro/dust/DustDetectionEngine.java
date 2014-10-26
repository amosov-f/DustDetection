package ru.spbu.astro.dust;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.dust.algo.DustTrendCalculator;
import ru.spbu.astro.dust.func.HealpixDistribution;
import ru.spbu.astro.dust.func.SphericDistribution;
import ru.spbu.astro.dust.graph.HammerProjection;
import ru.spbu.astro.dust.graph.PixPlot;
import ru.spbu.astro.dust.model.Catalogue;
import ru.spbu.astro.dust.model.Spheric;
import ru.spbu.astro.dust.util.StarSelector;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;

public final class DustDetectionEngine {
    public static void main(@NotNull final String[] args) throws FileNotFoundException {
        final DustTrendCalculator dustTrendCalculator = new DustTrendCalculator(
                new StarSelector(Catalogue.HIPPARCOS_UPDATED)
                        .selectByParallaxRelativeError(0.25).getStars()
        );
        final SphericDistribution f = new HealpixDistribution(dustTrendCalculator.getSlopes());
        final HammerProjection hammerProjection = new HammerProjection(f);
        final PixPlot pixPlot = new PixPlot(dustTrendCalculator);
        hammerProjection.setVisible(true);

        hammerProjection.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                final Spheric dir = HammerProjection.plane2spheric(hammerProjection.fromWindow(hammerProjection.getMousePosition()));
                if (dir != null) {
                    pixPlot.plot(dir);
                }
            }
        });
    }
}
