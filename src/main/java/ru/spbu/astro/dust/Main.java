package ru.spbu.astro.dust;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileInputStream;
import java.util.*;
import java.util.List;

public class Main {

    private static int SIZE = 500;

    public static void main(String[] args) throws Exception {
        List<Star> stars = new ArrayList();

        Scanner fin;
        try {
            fin = new Scanner(new FileInputStream("Datasets/data.txt"));
        } catch(Exception e) {
            return;
        }

        while (fin.hasNext()) {
            String name = fin.next();
            double l = fin.nextDouble();
            double b = fin.nextDouble();
            double r = fin.nextDouble();
            double cex = fin.nextDouble();
            if (cex > 100) {
                System.out.println(name);
            }

            stars.add(new Star(name, new Spheric(l, b), r, cex));
        }

        Collections.shuffle(stars);

        DustDetector dustDetector = new DustDetector(stars, 300.0);
        SphericDistribution f = new HealpixDistribution(dustDetector.getSlopes(), dustDetector.getSlopeErrs());
        final HammerProjection hammerProjection = new HammerProjection(f, 240);
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
                pixPlot.removeAllPlots();
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

        JFrame window = new JFrame();
        window.setSize(SIZE, SIZE);
        window.add(pixPlot);
        window.setVisible(true);
    }
}
