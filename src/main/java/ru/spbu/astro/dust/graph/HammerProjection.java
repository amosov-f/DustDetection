package ru.spbu.astro.dust.graph;

import ru.spbu.astro.dust.func.SphericDistribution;
import ru.spbu.astro.dust.model.Spheric;
import ru.spbu.astro.dust.model.Value;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public final class HammerProjection extends Component {

    private final SphericDistribution distribution;
    private final Mode mode;

    private static final int PARALLEL_COUNT = 10;
    private static final int MERIDIAN_COUNT = 24;

    public static enum Mode {
        DEFAULT, VALUES_ONLY
    }

    public HammerProjection(final SphericDistribution distribution) {
        this(distribution, Mode.DEFAULT);
    }

    public HammerProjection(final SphericDistribution distribution, final Mode mode) {
        this.distribution = distribution;
        this.mode = mode;
    }

    @Override
    public void paint(final Graphics g) {
        final Value[][] f = new Value[Math.min(getWidth(), 2 * getHeight())][Math.min(getHeight(), getWidth() / 2)];

        final Set<Double> values = new TreeSet<>();

        for (int x = 0; x < f.length; ++x) {
            for (int y = 0; y < f[x].length; ++y) {
                final Spheric dir = plane2spheric(fromWindow(new Point(x, y)));
                if (dir == null) {
                    continue;
                }

                f[x][y] = new Value(distribution.get(dir).value, Math.min(distribution.get(dir).error, 1));

                if (!Double.isNaN(f[x][y].value) && !Double.isInfinite(f[x][y].value)) {
                    values.add(f[x][y].value);
                }
            }
        }

        removeExtremeValues(values);

        double minValue = Collections.min(values);
        double maxValue = Collections.max(values);

        for (int x = 0; x < f.length; ++x) {
            for (int y = 0; y < f[x].length; ++y) {
                final Spheric dir = plane2spheric(fromWindow(new Point(x, y)));
                if (dir == null) {
                    continue;
                }

                double d = normalize(f[x][y].value, minValue, maxValue);

                final Color color;

                if (mode == Mode.VALUES_ONLY) {
                    if (d > 0) {
                        color = new Color((float)d, 0, 0);
                    } else {
                        color = new Color(0, 0, (float)Math.abs(d));
                    }
                } else {
                    if (d < 0) {
                        color = Color.getHSBColor(
                                240f / 360,
                                (float) Math.abs(d),
                                (float) (1.0 - normalize(f[x][y].error, 0, 1))
                        );
                    } else {
                        color = Color.getHSBColor(
                                0,
                                (float) d,
                                (float) (1.0 - normalize(f[x][y].error, 0, 1))
                        );
                    }
                }

                g.setColor(color);
                g.drawLine(x, y, x, y);
            }
        }

        paintCircles(g);
    }

    private void paintCircles(final Graphics g) {
        g.setColor(new Color(148, 167, 187));
        for (double l = 0; l < 2 * Math.PI; l += 2 * Math.PI / MERIDIAN_COUNT) {
            for (double b = -Math.PI / 2; b < Math.PI / 2; b += 0.001) {
                final Point p = toWindow(shperic2plane(new Spheric(l, b)));
                g.drawLine(p.x, p.y, p.x, p.y);
            }
        }
        for (double b = - Math.PI / 2; b < Math.PI / 2; b += 0.001) {
            final Point p = toWindow(shperic2plane(new Spheric(Math.PI - 0.00001, b)));
            g.drawLine(p.x, p.y, p.x, p.y);
        }

        for (double b = - Math.PI / 2; b < Math.PI / 2; b += Math.PI / PARALLEL_COUNT) {
            for (double l = 0; l < 2 * Math.PI; l += 0.0001) {
                final Point p = toWindow(shperic2plane(new Spheric(l, b)));
                g.drawLine(p.x, p.y, p.x, p.y);
            }
        }
    }

    public static Point2D.Double shperic2plane(final Spheric dir) {
        double l = dir.l;
        double b = dir.b;

        if (l > Math.PI) {
            l -= 2 * Math.PI;
        }

        double denominator = Math.sqrt(1 + Math.cos(b) * Math.cos(l / 2));

        double x = - 2 * Math.cos(b) * Math.sin(l / 2) / denominator;
        double y = - Math.sin(b) / denominator;

        return new Point2D.Double(x, y);
    }

    public static Spheric plane2spheric(final Point2D.Double p) {
        double x = p.getX();
        double y = p.getY();

        if (1 - Math.pow(0.5 * x, 2) - Math.pow(y, 2) < 0) {
            return null;
        }

        x *= Math.sqrt(2);
        y *= Math.sqrt(2);

        double z = Math.sqrt(1 - Math.pow(0.25 * x, 2) - Math.pow(0.5 * y, 2));

        double l = 2 * Math.atan2(z * x, (2 * (2 * Math.pow(z, 2) - 1)));
        double b = Math.asin(z * y);

        l = -l;
        if (l < 0) {
            l += 2 * Math.PI;
        }

        return new Spheric(l, b);
    }

    public Point toWindow(final Point2D.Double p) {
        double x = p.getX();
        double y = p.getY();

        x = (x / 2 + 1) * getHeight();
        y = (1 - y) * getHeight() / 2;

        return new Point((int) x, (int) y);
    }

    public Point2D.Double fromWindow(final Point p) {
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

        return x / d;
    }

    private static void removeExtremeValues(final Collection<Double> values) {
        if (values.size() > 2) {
            values.remove(Collections.max(values));
        }
        if (values.size() > 2) {
            values.remove(Collections.min(values));
        }
    }

}
//0.006443230944889031