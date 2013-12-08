package ru.spbu.astro.dust;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HammerProjection extends Component {

    private SphericDistribution f;
    private int hue;

    private static final int PARALLEL_COUNT = 10;
    private static final int MERIDIAN_COUNT = 24;

    public HammerProjection(final SphericDistribution f, int hue) {
        this.f = f;
        this.hue = hue;
    }

    @Override
    public void paint(Graphics g) {
        setSize(2 * getHeight(), getHeight());

        List<Double> values = new ArrayList();
        List<Double> errs = new ArrayList();

        for (int x = 0; x < getWidth(); ++x) {
            for (int y = 0; y < getHeight(); ++y) {
                Spheric dir = plane2spheric(fromWindow(new Point(x, y)));
                if (dir == null) {
                    continue;
                }

                double value = f.get(dir)[0];
                double err = f.get(dir)[1];

                if (!Double.isNaN(value)) {
                    values.add(value);
                }
                if (!Double.isNaN(err)) {
                    errs.add(err);
                }
            }
        }

        double minValue = Collections.min(values);
        double maxValue = Collections.max(values);
        double minErr = Collections.min(errs);
        double maxErr = Collections.max(errs);

        for (int x = 0; x < getWidth(); ++x) {
            for (int y = 0; y < getHeight(); ++y) {
                Spheric dir = plane2spheric(fromWindow(new Point(x, y)));
                if (dir == null) {
                    continue;
                }

                double value = f.get(dir)[0];
                double stdErr = f.get(dir)[1];

                g.setColor(Color.getHSBColor(
                        (float)hue / 360,
                        normalize(value, minValue, maxValue),
                        (float)1.0 - normalize(stdErr, minErr, maxErr)
                ));

                g.drawLine(x, y, x, y);
            }
        }

        g.setColor(new Color(148, 167, 187));
        for (double l = 0; l < 2 * Math.PI; l += 2 * Math.PI / MERIDIAN_COUNT) {
            for (double b = -Math.PI / 2; b < Math.PI / 2; b += 0.001) {
                Point p = toWindow(shperic2plane(new Spheric(l, b)));
                g.drawLine(p.x, p.y, p.x, p.y);
            }
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

    private static float normalize(double x, double min, double max) {
        return (float)((x - min) / (max - min));
    }

}
