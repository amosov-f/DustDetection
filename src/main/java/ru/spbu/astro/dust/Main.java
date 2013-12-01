package ru.spbu.astro.dust;

import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.util.*;
import java.util.List;

public class Main {

    private static int SIZE = 660;

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

            stars.add(new Star(name, new Spheric(l, b), r, cex));
        }

        Collections.shuffle(stars);

        DustDetector dustDetector = new DustDetector(stars, 800.0);

        JWindow slopeWindow = new JWindow();
        slopeWindow.setSize(2 * SIZE, SIZE);
        slopeWindow.getContentPane().add(dustDetector.getSlopeDistribution(SIZE));
        slopeWindow.setVisible(true);

        JWindow interceptWindow = new JWindow();
        interceptWindow.setSize(2 * SIZE, SIZE);
        interceptWindow.getContentPane().add(dustDetector.getInterceptDistribution(SIZE));
        interceptWindow.setVisible(true);
    }
}
