package ru.spbu.astro.dust.graph;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.dust.func.SphericDistribution;
import ru.spbu.astro.dust.model.Spheric;
import ru.spbu.astro.dust.model.Value;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import static java.lang.Math.*;

public final class HammerProjection extends JWindow {
    @NotNull
    private final SphericDistribution distribution;
    @NotNull
    private final Mode mode;
    @NotNull
    private DirectionProcessor processor = dir -> {};

    private static final int PARALLEL_COUNT = 10;
    private static final int MERIDIAN_COUNT = 24;

    private static final int SIZE = 500;

    private static final int REMOVE_LIMIT = 100;
    private static final int OUTLIERS = 2;

    public static enum Mode {
        DEFAULT, WITH_ERRORS
    }

    public static interface DirectionProcessor {
        void process(@NotNull final Spheric dir);
    }

    public HammerProjection(@NotNull final SphericDistribution distribution) {
        this(distribution, Mode.DEFAULT);
    }

    public HammerProjection(@NotNull final SphericDistribution distribution, @NotNull final Mode mode) {
        this.distribution = distribution;
        this.mode = mode;
        setSize(2 * SIZE, SIZE);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(@NotNull final MouseEvent e) {
                final Spheric dir = toSpheric(fromWindow(getMousePosition()));
                if (dir != null) {
                    System.out.println(dir);
                    processor.process(dir);
                }
            }
        });
    }

    @Override
    public void paint(@NotNull final Graphics g) {
        final Value[][] f = new Value[Math.min(getWidth(), 2 * getHeight())][Math.min(getHeight(), getWidth() / 2)];

        final Set<Double> values = new TreeSet<>();
        final Set<Double> errors = new TreeSet<>();

        for (int x = 0; x < f.length; ++x) {
            for (int y = 0; y < f[x].length; ++y) {
                final Spheric dir = toSpheric(fromWindow(new Point(x, y)));
                if (dir == null) {
                    continue;
                }

                f[x][y] = distribution.get(dir);

                if (f[x][y] != null) {
                    if (!Double.isNaN(f[x][y].getValue()) && !Double.isInfinite(f[x][y].getValue())) {
                        values.add(f[x][y].getValue());
                    }
                    if (!Double.isNaN(f[x][y].getError()) && !Double.isInfinite(f[x][y].getError())) {
                        errors.add(f[x][y].getError());
                    }
                }
            }
        }

        /*if (values.size() > REMOVE_LIMIT) {
            removeExtremeValues(values);
        }
        if (errors.size() > REMOVE_LIMIT) {
            removeExtremeValues(errors);
        }*/

        //if (minValue == 0) {
        final double minValue = Collections.min(values);
        //}
        //if (maxValue == 0) {
        final double maxValue = Collections.max(values);
        //}

        double maxError = Collections.max(errors);

        for (int x = 0; x < f.length; ++x) {
            for (int y = 0; y < f[x].length; ++y) {
                final Spheric dir = toSpheric(fromWindow(new Point(x, y)));
                if (dir == null) {
                    continue;
                }

                final Color color;
                if (f[x][y] == null) {
                    color = Color.BLACK;
                } else {
                    final double value = normalize(f[x][y].getValue(), minValue, maxValue);
                    final double error;
                    if (mode == Mode.WITH_ERRORS) {
                        error = normalize(f[x][y].getError(), 0, maxError);
                    } else {
                        error = 0;
                    }

                    if (value >= 0) {

                        color = Color.getHSBColor(0, (float) value, (float) (1.0 - error));
                    } else {
                        color = Color.getHSBColor(240f / 360, (float) Math.abs(value), (float) (1.0 - error));
                    }
                }

                g.setColor(color);
                g.drawLine(x, y, x, y);
            }
        }

        paintCircles(g);
    }

    public void setProcessor(@NotNull final DirectionProcessor processor) {
        this.processor = processor;
    }

    private void paintCircles(@NotNull final Graphics g) {
        g.setColor(new Color(148, 167, 187));
        for (double l = 0; l < 2 * Math.PI; l += 2 * Math.PI / MERIDIAN_COUNT) {
            for (double b = -Math.PI / 2; b < Math.PI / 2; b += 0.001) {
                Point p = toWindow(toPlane(new Spheric(l, b)));
                g.drawLine(p.x, p.y, p.x, p.y);
            }
        }
        for (double b = - Math.PI / 2; b < Math.PI / 2; b += 0.001) {
            Point p = toWindow(toPlane(new Spheric(Math.PI - 0.00001, b)));
            g.drawLine(p.x, p.y, p.x, p.y);
        }

        for (double b = - Math.PI / 2; b < Math.PI / 2; b += Math.PI / PARALLEL_COUNT) {
            for (double l = 0; l < 2 * Math.PI; l += 0.0001) {
                Point p = toWindow(toPlane(new Spheric(l, b)));
                g.drawLine(p.x, p.y, p.x, p.y);
            }
        }
    }

    @NotNull
    public static Point2D.Double toPlane(@NotNull final Spheric dir) {
        double l = dir.getL();
        final double b = dir.getB();

        if (l > Math.PI) {
            l -= 2 * Math.PI;
        }

        final double denominator = sqrt(1 + Math.cos(b) * Math.cos(l / 2));

        final double x = - 2 * Math.cos(b) * Math.sin(l / 2) / denominator;
        final double y = - Math.sin(b) / denominator;

        return new Point2D.Double(x, y);
    }

    @Nullable
    public static Spheric toSpheric(@NotNull final Point2D.Double p) {
        double x = p.getX();
        double y = p.getY();

        if (1 - pow(0.5 * x, 2) - pow(y, 2) < 0) {
            return null;
        }

        x *= sqrt(2);
        y *= sqrt(2);

        final double z = sqrt(1 - pow(0.25 * x, 2) - pow(0.5 * y, 2));

        double l = 2 * atan2(z * x, (2 * (2 * pow(z, 2) - 1)));
        final double b = Math.asin(z * y);

        l = -l;
        if (l < 0) {
            l += 2 * Math.PI;
        }

        return new Spheric(l, b);
    }

    @NotNull
    public Point toWindow(@NotNull final Point2D.Double p) {
        double x = p.getX();
        double y = p.getY();

        x = (x / 2 + 1) * getHeight();
        y = (1 - y) * getHeight() / 2;

        return new Point((int) x, (int) y);
    }

    @NotNull
    public Point2D.Double fromWindow(@NotNull final Point p) {
        double x = 2 * (p.getX() / getHeight() - 1);
        double y = 2 * (getHeight() - p.getY()) / getHeight() - 1;

        return new Point2D.Double(x, y);
    }

    private static double normalize(double x, double min, double max) {
        if (x == Double.POSITIVE_INFINITY || Double.isNaN(x)) {
            x = max;
        }
        if (x == Double.NEGATIVE_INFINITY) {
            x = min;
        }
        if (x < min) {
            x = min;
        }
        if (x > max) {
            x = max;
        }

        double d = Math.max(Math.abs(min), Math.abs(max));
        return d == 0 ? 0 : x / d;
    }

    private static void removeExtremeValues(@NotNull final Collection<Double> values) {
        for (int i = 0; i < OUTLIERS;) {
            if (values.size() > 2 && Collections.max(values) > 0) {
                values.remove(Collections.max(values));
                i++;
            }
            if (values.size() > 2 && Collections.min(values) < 0) {
                values.remove(Collections.min(values));
                i++;
            }
        }
    }

}