package ru.spbu.astro.dust.graph;

import org.math.plot.FrameView;
import org.math.plot.Plot2DPanel;
import ru.spbu.astro.dust.model.Catalogue;
import ru.spbu.astro.dust.model.SpectralType;

import java.awt.*;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HRDiagram extends Plot2DPanel {

    private static final int MAX_LUMINOSITY_CLASS = 7;
    private static final Map<Integer, Color> class2color = new HashMap<>();
    static {
        class2color.put(0, Color.MAGENTA);
        class2color.put(1, Color.CYAN);
        class2color.put(2, Color.ORANGE);
        class2color.put(3, Color.RED);
        class2color.put(4, Color.YELLOW);
        class2color.put(5, Color.BLACK);
        class2color.put(6, Color.GREEN);
        class2color.put(7, Color.BLUE);
    }

    private class Star {
        final double bvColor;
        final Integer luminosityClass;
        final double parallax;
        final double mag;

        Star(final Catalogue.Row row) {
            bvColor = new Double(row.get("bv_color"));

            Integer luminosityClass;
            try {
                luminosityClass = new SpectralType(row.get("spect_type")).getLuminosityClass();
            } catch (IllegalArgumentException e) {
                luminosityClass = null;
            }
            this.luminosityClass = luminosityClass;

            double vMag = new Double(row.get("vmag"));
            parallax = new Double(row.get("parallax"));
            mag = vMag + 5 * Math.log10(parallax) - 10;
        }
    }

    public HRDiagram(final Catalogue catalogue) {
        addLegend("SOUTH");

        final List<List<Star>> stars = new ArrayList<>();
        for (int i = 0; i <= MAX_LUMINOSITY_CLASS; ++i) {
            stars.add(new ArrayList<Star>());
        }
        for (final Catalogue.Row row : catalogue) {
            final Star s = new Star(row);
            if (s.parallax > 20 && s.luminosityClass != null) {
                stars.get(s.luminosityClass).add(s);
            }
        }

        for (int i = stars.size() - 1; i >= 0; --i) {
            addLuminosityClassStars(stars.get(i));
        }
    }

    private void addLuminosityClassStars(final List<Star> stars) {
        if (stars.isEmpty()) {
            return;
        }
        int luminosityClass = stars.get(0).luminosityClass;

        double[] x = new double[stars.size()];
        double[] y = new double[stars.size()];

        for (int i = 0; i < stars.size(); ++i) {
            x[i] = stars.get(i).bvColor;
            y[i] = - stars.get(i).mag;
        }

        addScatterPlot("Звезды класса " + luminosityClass, class2color.get(luminosityClass), x, y);
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
