package ru.spbu.astro.dust;

import org.math.plot.FrameView;
import ru.spbu.astro.dust.algo.DustDetector;
import ru.spbu.astro.dust.func.HealpixDistribution;
import ru.spbu.astro.dust.func.SphericDistribution;
import ru.spbu.astro.dust.graph.HammerProjection;
import ru.spbu.astro.dust.graph.PixPlot;
import ru.spbu.astro.dust.model.Spheric;
import ru.spbu.astro.dust.model.Star;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.List;

public class DustDetectionEngine {

    public static int SIZE = 500;

    public static List<Star> getStars() {
        List<Star> stars = new ArrayList();

        Scanner fin;
        try {
            fin = new Scanner(new FileInputStream("Datasets/data.txt"));
        } catch(Exception e) {
            return null;
        }

        while (fin.hasNext()) {
            String name = fin.next();
            double l = fin.nextDouble();
            double b = fin.nextDouble();
            double r = fin.nextDouble();
            double ext = fin.nextDouble();
            double extError = fin.nextDouble();

            stars.add(new Star(name, new Spheric(l, b), r, ext, extError));
        }

        Collections.shuffle(stars);
        return stars;
    }

    public static DustDetector getDustDetector() {
        return new DustDetector(getStars(), 300.0);
    }

    public static void main(String[] args) throws Exception {
        DustDetector dustDetector = getDustDetector();

        PrintWriter fout = new PrintWriter(new FileOutputStream("results/2.txt"));
        fout.println("L\t\t\tB\t\t\ta\t\tsigma_a\tb\t\tsigma_b\tn");
        for (DustDetector.DustPix dustPix : dustDetector.getDustPixes()) {
            fout.println(dustPix);
        }
        fout.flush();


        SphericDistribution f = new HealpixDistribution(dustDetector.getSlopes(), dustDetector.getSlopeErrs());
        final HammerProjection hammerProjection = new HammerProjection(f);
        final PixPlot pixPlot = new PixPlot(dustDetector);

        hammerProjection.setSize(2 * SIZE, SIZE);
        JFrame map = new JFrame();
        map.setSize(2 * SIZE, SIZE);
        map.add(hammerProjection);
        map.setVisible(true);
        map.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Spheric dir = HammerProjection.plane2spheric(hammerProjection.fromWindow(hammerProjection.getMousePosition()));
                pixPlot.plot(dir);
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
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
