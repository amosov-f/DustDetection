package ru.spbu.astro.dust.graph;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.dust.algo.DustTrendCalculator;
import ru.spbu.astro.dust.func.HEALPixDistribution;
import ru.spbu.astro.dust.func.SphericDistribution;
import ru.spbu.astro.dust.model.Catalogue;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import static ru.spbu.astro.dust.model.Catalogue.HIPPARCOS_UPDATED;

/**
 * User: amosov-f
 * Date: 22.09.14
 * Time: 23:32
 */
public class DeepObserver extends JFrame {
    @NotNull
    private final List<SphericDistribution> distributions = new ArrayList<>();

    private int deep = 0;
    private final double dr;

    private final HammerProjection hammerProjection;

    public DeepObserver(@NotNull final Catalogue catalogue, final double dr) {
        hammerProjection = new HammerProjection(new HEALPixDistribution(new DustTrendCalculator(catalogue, 0, 100).getSlopes()));


        this.dr = dr;
        for (int i = 0; i < 5; i++) {
            distributions.add(new HEALPixDistribution(new DustTrendCalculator(catalogue, dr * i, dr * (i + 1)).getSlopes()));
        }

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(@NotNull final KeyEvent e) {
                final int key = e.getKeyCode();
                switch (key) {
                    case KeyEvent.VK_UP:
                        if (deep < distributions.size() - 1) {
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
        System.out.println("Between " + dr * deep + " and " + dr * (deep + 1));
        new HammerProjection(distributions.get(deep), hammerProjection.getMinValue(), hammerProjection.getMaxValue());
    }

    public static void main(@NotNull final String[] args) {
        new DeepObserver(HIPPARCOS_UPDATED, 100);
    }
}
