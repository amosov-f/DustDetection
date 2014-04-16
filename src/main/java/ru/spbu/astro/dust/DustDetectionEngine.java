package ru.spbu.astro.dust;

import ru.spbu.astro.dust.algo.DustDetector;
import ru.spbu.astro.dust.algo.LuminosityClassifier;
import ru.spbu.astro.dust.func.HealpixDistribution;
import ru.spbu.astro.dust.func.SphericDistribution;
import ru.spbu.astro.dust.graph.HammerProjection;
import ru.spbu.astro.dust.graph.PixPlot;
import ru.spbu.astro.dust.model.Catalogue;
import ru.spbu.astro.dust.model.Spheric;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;

public class DustDetectionEngine {

    public static Catalogue getCatalogue() throws FileNotFoundException {
        final Catalogue hipparcos = new Catalogue("datasets/hipparcos1997.txt");

        hipparcos.updateBy(new Catalogue("datasets/hipparcos2007.txt"));
        hipparcos.updateBy(new LuminosityClassifier(hipparcos));

        return hipparcos;
    }


    public static DustDetector getDustDetector() throws FileNotFoundException {
        return new DustDetector(getCatalogue(), 0.25);
    }

    public static void main(final String[] args) throws FileNotFoundException {

        final DustDetector dustDetector = getDustDetector();
        SphericDistribution f = new HealpixDistribution(dustDetector.getSlopes());
        HammerProjection hammerProjection = new HammerProjection(f, HammerProjection.Mode.VALUES_ONLY);
        final PixPlot pixPlot = new PixPlot(dustDetector);

        hammerProjection.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                final Spheric dir = HammerProjection.plane2spheric(hammerProjection.fromWindow(hammerProjection.getMousePosition()));
                pixPlot.plot(dir);
            }
        });

        //new FrameView(pixPlot);
        /*
        Plot2DPanel slopeHistogram = new Plot2DPanel("SOUTH");
        slopeHistogram.addHistogramPlot("a", dustDetector.getSlopes(), 50);
        new FrameView(slopeHistogram);

        Plot2DPanel interceptHistogram = new Plot2DPanel("SOUTH");
        interceptHistogram.addHistogramPlot("b", dustDetector.getIntercepts(), 50);
        new FrameView(interceptHistogram);

        Plot2DPanel fullSlopeErrsHistogram = new Plot2DPanel("SOUTH");
        fullSlopeErrsHistogram.addHistogramPlot("sigma_a / a", dustDetector.getFullSlopeErrs(), 50);
        new FrameView(fullSlopeErrsHistogram);

        Plot2DPanel slopeErrsHistogram = new Plot2DPanel("SOUTH");
        slopeErrsHistogram.addHistogramPlot("min(sigma_a / a, 1)", dustDetector.getSlopeErrs(), 50);
        new FrameView(slopeErrsHistogram);

        Plot2DPanel interceptErrsHistogram = new Plot2DPanel("SOUTH");
        interceptErrsHistogram.addHistogramPlot("min(sigma_b / b, 1)", dustDetector.getInterceptErrs(), 50);
        new FrameView(interceptErrsHistogram);
        */

    }
}
