package ru.spbu.astro.dust.graph;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.commons.graph.HammerProjection;
import ru.spbu.astro.dust.algo.DustTrendCalculator;
import ru.spbu.astro.commons.func.HealpixBinaryDistribution;
import ru.spbu.astro.commons.Catalog;
import ru.spbu.astro.commons.Spheric;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.commons.StarFilter;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static ru.spbu.astro.dust.DustCatalogues.HIPPARCOS_UPDATED;

/**
 * User: amosov-f
 * Date: 22.09.14
 * Time: 23:32
 */
public class DeepObserver extends JFrame {
    @NotNull
    private final List<DustTrendCalculator> calculators = new ArrayList<>();

    @NotNull
    private final List<List<Star>> layers;

    private int deep = 0;

    private HammerProjection hammerProjection;

    public DeepObserver(@NotNull final Catalog catalog) {
        layers = split(new StarFilter(catalog.getStars()).parallaxRelativeError(0.25).getStars(), 2);

        for (int i = 0; i < layers.size(); i++) {
            calculators.add(new DustTrendCalculator(layers.get(i), i != 0));
        }

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(@NotNull final KeyEvent e) {
                final int key = e.getKeyCode();
                switch (key) {
                    case KeyEvent.VK_UP:
                        if (deep < calculators.size() - 1) {
                            deep++;
                            update();
                        }
                        break;
                    case KeyEvent.VK_DOWN:
                        if (deep > 0) {
                            deep--;
                            update();
                        }
                        break;
                    default:
                        break;
                }
            }
        });

        update();
        setVisible(true);
    }

    public void update() {
        final double r1 = layers.get(deep).stream().mapToDouble(s -> s.getR().getValue()).min().getAsDouble();
        final double r2 = layers.get(deep).stream().mapToDouble(s -> s.getR().getValue()).max().getAsDouble();

        System.out.println("Between " + r1 + " and " + r2);

        final PixPlot pixPlot = new PixPlot(calculators.get(deep));

        hammerProjection = new HammerProjection(new HealpixBinaryDistribution(calculators.get(deep).getSlopes(), 0.002));
        hammerProjection.setVisible(true);

        hammerProjection.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                final Spheric dir = HammerProjection.toSpheric(hammerProjection.fromWindow(hammerProjection.getMousePosition()));
                if (dir != null) {
                    pixPlot.plot(dir);
                }
            }
        });
    }

    @NotNull
    private static List<List<Star>> split(@NotNull final List<Star> stars, final int numParts) {
        System.out.println("Split started!");
        final List<List<Star>> layers = new ArrayList<>();
        layers.add(new ArrayList<>());

        Collections.sort(stars, Comparator.comparing(Star::getR));

        for (final Star star : stars) {
            layers.get(layers.size() - 1).add(star);
            if (layers.get(layers.size() - 1).size() >= stars.size() / numParts + 1) {
                layers.add(new ArrayList<>());
            }
        }
        System.out.println("Split finished!");
        return layers;
    }

    public static void main(@NotNull final String[] args) {
        new DeepObserver(HIPPARCOS_UPDATED);
    }
}
