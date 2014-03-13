package ru.spbu.astro.dust.graph;

import org.math.plot.FrameView;
import org.math.plot.Plot2DPanel;
import ru.spbu.astro.dust.algo.LuminosityClassifier;
import ru.spbu.astro.dust.model.Catalogue;
import ru.spbu.astro.dust.model.SpectralType;
import ru.spbu.astro.dust.model.Star;

import java.awt.*;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class HRDiagram extends Plot2DPanel {

    public HRDiagram(final Catalogue catalogue) {
        addLegend("SOUTH");

        final Map<String, List<Star>> class2stars = new HashMap<>();
        for (final String luminosityClass : SpectralType.parseLuminosityClasses) {
            class2stars.put(luminosityClass, new ArrayList<Star>());
        }
        for (final Star s : catalogue.getStars()) {
            if (s.parallax.value > 10 && s.spectralType.getLuminosityClass() != null) {
                class2stars.get(s.spectralType.getLuminosityClass()).add(s);
            }
        }

        for (final List<Star> stars : class2stars.values()) {
            addLuminosityClassStars(stars);
        }

        setFixedBounds(new double[]{-0.5, -15}, new double[]{2.0, 5});
    }

    private void addLuminosityClassStars(final List<Star> stars) {
        if (stars.isEmpty()) {
            return;
        }
        final String luminosityClass = stars.get(0).spectralType.getLuminosityClass();

        final double[] x = new double[stars.size()];
        final double[] y = new double[stars.size()];

        for (int i = 0; i < stars.size(); ++i) {
            x[i] = stars.get(i).bvColor.value;
            y[i] = -stars.get(i).getAbsoluteMagnitude();
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
            catalogue = new Catalogue("datasets/hipparcos1997.txt");
            catalogue.updateBy(new Catalogue("datasets/hipparcos2007.txt"));
            //catalogue.updateBy(new LuminosityClassifier(catalogue));
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        new FrameView(new HRDiagram(catalogue));
    }

}
