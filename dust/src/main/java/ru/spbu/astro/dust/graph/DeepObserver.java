package ru.spbu.astro.dust.graph;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.core.graph.HammerProjection;
import ru.spbu.astro.dust.algo.DustTrendCalculator;
import ru.spbu.astro.core.func.HealpixBinaryDistribution;
import ru.spbu.astro.dust.model.Catalogue;
import ru.spbu.astro.core.Spheric;
import ru.spbu.astro.core.Star;
import ru.spbu.astro.dust.util.StarSelector;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static ru.spbu.astro.dust.model.Catalogue.HIPPARCOS_UPDATED;

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

    @NotNull
    private HammerProjection hammerProjection;


    public DeepObserver(@NotNull final Catalogue catalogue) {
        layers = split(new StarSelector(catalogue).parallaxRelativeError(0.25).getStars(), 2);

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
                }
            }
        });

        update();
        setVisible(true);
    }

    public void update() {
        final double r1 = Collections.min(layers.get(deep)).getR().getValue();
        final double r2 = Collections.max(layers.get(deep)).getR().getValue();

        System.out.println("Between " + r1 + " and " + r2);

        final PixPlot pixPlot = new PixPlot(calculators.get(deep));

        hammerProjection = new HammerProjection(new HealpixBinaryDistribution(calculators.get(deep).getSlopes(), 0.002));
        hammerProjection.setVisible(true);

        hammerProjection.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
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

        Collections.sort(stars);

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
