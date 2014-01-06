package ru.spbu.astro.dust.graph;

import ru.spbu.astro.dust.func.SphericDistribution;
import ru.spbu.astro.dust.model.Spheric;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

public class HammerProjection extends Component {

    private SphericDistribution sphericDistribution;

    private static final int PARALLEL_COUNT = 10;
    private static final int MERIDIAN_COUNT = 24;

    public HammerProjection(final SphericDistribution sphericDistribution) {
        this.sphericDistribution = sphericDistribution;
    }

    @Override
    public void paint(Graphics g) {

        if (sphericDistribution.dim() == 1) {
            paintValues(g);
        }

        if (sphericDistribution.dim() == 2) {
            paintValuesAndErrs(g);
        }

        paintCircles(g);
    }

    private void paintValues(Graphics g) {
        System.out.println(getHeight());

        double[][] f = new double[Math.min(getWidth(), 2 * getHeight())][Math.min(getHeight(), getWidth() / 2)];

        Set<Double> values = new TreeSet();

        for (int x = 0; x < f.length; ++x) {
            for (int y = 0; y < f[x].length; ++y) {
                Spheric dir = plane2spheric(fromWindow(new Point(x, y)));
                if (dir == null) {
                    continue;
                }

                f[x][y] = sphericDistribution.get(dir)[0];

                if (!Double.isNaN(f[x][y]) && !Double.isInfinite(f[x][y])) {
                    values.add(f[x][y]);
                }
            }
        }

        removeExtremeValues(values);

        double minValue = Collections.min(values);
        double maxValue = Collections.max(values);

        for (int x = 0; x < f.length; ++x) {
            for (int y = 0; y < f[x].length; ++y) {
                Spheric dir = plane2spheric(fromWindow(new Point(x, y)));
                if (dir == null) {
                    continue;
                }

                double d = normalize(f[x][y], minValue, maxValue);

                Color color;
                if (d > 0) {
                    color = new Color((float)d, 0, 0);
                } else {
                    color = new Color(0, 0, (float)Math.abs(d));
                }

                g.setColor(color);
                g.drawLine(x, y, x, y);
            }
        }

        //System.out.println(shperic2plane(new Spheric(0, 0)));

    }

    private void paintValuesAndErrs(Graphics g) {


        double[][][] f = new double[Math.min(getWidth(), 2 * getHeight())][Math.min(getHeight(), getWidth() / 2)][2];

        Set<Double> values = new TreeSet();

        for (int x = 0; x < f.length; ++x) {
            for (int y = 0; y < f[x].length; ++y) {
                Spheric dir = plane2spheric(fromWindow(new Point(x, y)));
                if (dir == null) {
                    continue;
                }

                f[x][y] = sphericDistribution.get(dir);

                if (!Double.isNaN(f[x][y][0]) && !Double.isInfinite(f[x][y][0])) {
                    values.add(f[x][y][0]);
                }

            }
        }

        removeExtremeValues(values);

        double minValue = Collections.min(values);
        double maxValue = Collections.max(values);
        System.out.println(maxValue);

        for (int x = 0; x < f.length; ++x) {
            for (int y = 0; y < f[x].length; ++y) {
                Spheric dir = plane2spheric(fromWindow(new Point(x, y)));
                if (dir == null) {
                    continue;
                }

                double d = normalize(f[x][y][0], minValue, maxValue);
                Color color = Color.getHSBColor(
                        0,
                        (float)d,
                        (float)(1.0 - normalize(f[x][y][1], 0, 1))
                );
                if (d < 0) {

                    color = Color.getHSBColor(
                            (float)(240.0 / 360.0),
                            (float)Math.abs(d),
                            (float)(1.0 - normalize(f[x][y][1], 0, 1))
                    );
                }
                g.setColor(color);
                g.drawLine(x, y, x, y);
            }
        }
    }

    private void paintCircles(Graphics g) {
        g.setColor(new Color(148, 167, 187));
        for (double l = 0; l < 2 * Math.PI; l += 2 * Math.PI / MERIDIAN_COUNT) {
            for (double b = -Math.PI / 2; b < Math.PI / 2; b += 0.001) {
                Point p = toWindow(shperic2plane(new Spheric(l, b)));
                g.drawLine(p.x, p.y, p.x, p.y);
            }
        }
        for (double b = -Math.PI / 2; b < Math.PI / 2; b += 0.001) {
            Point p = toWindow(shperic2plane(new Spheric(Math.PI - 0.00001, b)));
            g.drawLine(p.x, p.y, p.x, p.y);
        }

        for (double b = - Math.PI / 2; b < Math.PI / 2; b += Math.PI / PARALLEL_COUNT) {
            for (double l = 0; l < 2 * Math.PI; l += 0.0001) {
                Point p = toWindow(shperic2plane(new Spheric(l, b)));
                g.drawLine(p.x, p.y, p.x, p.y);
            }
        }
    }

    public static Point2D.Double shperic2plane(final Spheric dir) {
        double l = dir.getL();
        double b = dir.getB();

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

    public Point toWindow(Point2D.Double p) {
        double x = p.getX();
        double y = p.getY();

        x = (x / 2 + 1) * getHeight();
        y = (1 - y) * getHeight() / 2;

        return new Point((int)x, (int)y);
    }

    public Point2D.Double fromWindow(Point p) {
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


    private static void removeExtremeValues(Set<Double> values) {

        List<Double> valueList = new ArrayList(values);

        if (values.size() > 2) {
            values.remove(valueList.get(valueList.size() - 1));
        }
        if (values.size() > 2) {
            values.remove(valueList.get(0));
        }

    }


}
//0.006443230944889031