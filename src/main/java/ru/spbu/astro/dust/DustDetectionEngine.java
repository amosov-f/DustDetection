package ru.spbu.astro.dust;

import org.math.plot.FrameView;
import org.math.plot.plots.ScatterPlot;
import ru.spbu.astro.dust.algo.DustDetector;
import ru.spbu.astro.dust.algo.LuminosityClassifier;
import ru.spbu.astro.dust.func.CountHealpixDistribution;
import ru.spbu.astro.dust.func.HealpixDistribution;
import ru.spbu.astro.dust.func.SphericDistribution;
import ru.spbu.astro.dust.graph.HammerProjection;
import ru.spbu.astro.dust.graph.PixPlot;
import ru.spbu.astro.dust.model.Catalogue;
import ru.spbu.astro.dust.model.Spheric;
import ru.spbu.astro.dust.model.Star;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.List;

public class DustDetectionEngine {

    public static final int SIZE = 500;

    public static List<Star> getStars() throws FileNotFoundException {
        final Catalogue hipparcos = new Catalogue("datasets/hipparcos1997.txt");
        final Catalogue hipparcos2007 = new Catalogue("datasets/hipparcos2007.txt");

        hipparcos.updateBy(hipparcos2007);
        hipparcos.updateBy(new LuminosityClassifier(hipparcos));

        return hipparcos.getStars();
    }

    private static Spheric[] getDirs() throws FileNotFoundException {
        final Catalogue hipparcos = new Catalogue("datasets/hipparcos1997.txt");
        final Catalogue hipparcos2007 = new Catalogue("datasets/hipparcos2007.txt");

        hipparcos.updateBy(hipparcos2007);

        final List<Star> stars = hipparcos.getStars();
        final Spheric[] dirs = new Spheric[stars.size()];
        for (int i = 0; i < dirs.length; ++i) {
            dirs[i] = stars.get(i).dir;
        }
        return dirs;
    }

    public static DustDetector getDustDetector() throws FileNotFoundException {
        return new DustDetector(getStars(), 300.0);
    }

    public static void main(final String[] args) throws FileNotFoundException {


        /*
        PrintWriter fout = new PrintWriter(new FileOutputStream("results/2.txt"));
        fout.println("L\t\t\tB\t\t\ta\t\tsigma_a\tb\t\tsigma_b\tn");
        for (DustDetector.DustPix dustPix : dustDetector.getDustPixes()) {
            fout.println(dustPix);
        }
        fout.flush();
        */

        final DustDetector dustDetector = getDustDetector();
        SphericDistribution f = new HealpixDistribution(dustDetector.getSlopes());
        //SphericDistribution f = new CountHealpixDistribution(18, getDirs());
        final HammerProjection hammerProjection = new HammerProjection(f, HammerProjection.Mode.VALUES_ONLY);
        final PixPlot pixPlot = new PixPlot(dustDetector);

        hammerProjection.setSize(2 * SIZE, SIZE);
        final JFrame map = new JFrame();
        map.setSize(2 * SIZE, SIZE);
        map.add(hammerProjection);
        map.setVisible(true);
        map.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                final Spheric dir = HammerProjection.plane2spheric(hammerProjection.fromWindow(hammerProjection.getMousePosition()));
                pixPlot.plot(dir);
            }
        });

        new FrameView(pixPlot);
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
