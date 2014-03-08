package ru.spbu.astro.dust.graph;

import org.math.plot.FrameView;
import org.math.plot.Plot2DPanel;
import ru.spbu.astro.dust.model.Catalogue;
import ru.spbu.astro.dust.model.SpectralType;

import java.awt.*;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.List;

import static ru.spbu.astro.dust.algo.LuminosityClassifier.Star;
import static ru.spbu.astro.dust.model.Catalogue.*;

public class HRDiagram extends Plot2DPanel {

    public HRDiagram(final Catalogue catalogue) {
        addLegend("SOUTH");

        final Map<String, List<Star>> class2stars = new HashMap<>();
        for (final String luminosityClass : SpectralType.parseLuminosityClasses) {
            class2stars.put(luminosityClass, new ArrayList<Star>());
        }
        for (final Row row : catalogue) {
            final Star s = new Star(row);
            if (s.parallax > 5 && s.luminosityClass != null) {
                System.out.println(s.luminosityClass);
                class2stars.get(s.luminosityClass).add(s);

            }
        }

        for (final List<Star> stars : class2stars.values()) {
            addLuminosityClassStars(stars);
        }
    }

    private void addLuminosityClassStars(final List<Star> stars) {
        if (stars.isEmpty()) {
            return;
        }
        final String luminosityClass = stars.get(0).luminosityClass;

        final double[] x = new double[stars.size()];
        final double[] y = new double[stars.size()];

        for (int i = 0; i < stars.size(); ++i) {
            x[i] = stars.get(i).bvColor;
            y[i] = -stars.get(i).mag;
        }

        System.out.println("#" + luminosityClass + ": " + stars.size());

        addScatterPlot("Звезды класса " + luminosityClass, color(luminosityClass), PixPlot.getX(x, y), PixPlot.getY(x, y));
    }

    private Color color(final String s) {
        int hash = Math.abs(s.hashCode()) % 1000;
        return new Color(hash % 256, (hash * hash + 100) % 256, (hash * hash * hash) % 256);
    }

    public static void main(final String[] args) {
        final Catalogue catalogue;
        try {
            catalogue = new Catalogue("datasets/hipparcos1997.txt").updateBy(new Catalogue("datasets/hipparcos2007.txt"));
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        new FrameView(new HRDiagram(catalogue));
    }

}
