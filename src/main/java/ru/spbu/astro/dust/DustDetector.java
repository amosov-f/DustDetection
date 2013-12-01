package ru.spbu.astro.dust;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.asterope.healpix.PixTools;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DustDetector {

    private static final int SIZE = 660;
    private static final int N_SIDE = 18;

    private final List<Star>[] rings;

    private List<Double> slopes = new ArrayList();
    private List<Double> slopeStdErrs = new ArrayList();

    private List<Double> intercepts = new ArrayList();
    private List<Double> interceptStdErrs = new ArrayList();

    public DustDetector(final List<Star> stars, Double r) {
        PixTools pixTools = new PixTools(N_SIDE);

        rings = new List[12 * N_SIDE * N_SIDE];
        for (int i = 0; i < rings.length; ++i) {
            rings[i] = new ArrayList();
        }

        for (Star star : stars) {
            if (star.getR() < 0) {
                continue;
            }
            if (r != null && star.getR() > r) {
                continue;
            }

            double theta = Math.PI / 2 - star.getDir().getB();
            double phi = star.getDir().getL();

            rings[(int)pixTools.ang2pix(theta, phi)].add(star);
        }

        for (int i = 0; i < rings.length; ++i) {
            SimpleRegression regression = getRegression(rings[i]);
            slopes.add(regression.getSlope());
            slopeStdErrs.add(regression.getSlopeStdErr());

            intercepts.add(regression.getIntercept());
            interceptStdErrs.add(regression.getInterceptStdErr());
        }
    }

    public Component getSlopeDistribution(final int height) throws Exception {
        return new Component() {
            @Override
            public void paint(Graphics g) {
                setSize(2 * height, height);

                double slope1 = Collections.min(slopes);
                double slope2 = Collections.max(slopes);

                double slopeStdErr1 = Collections.min(slopeStdErrs);
                double slopeStdErr2 = Collections.max(slopeStdErrs);

                for (int i = 0; i < rings.length; ++i) {
                    for (Star star : rings[i]) {
                        //int color = 255 - (int)((slopes.get(i) - slope1) / (slope2 - slope1) * 255);
                        double slope = slopes.get(i);
                        double slopeStdErr = slopeStdErrs.get(i);

                        g.setColor(Color.getHSBColor(
                                (float) 240.0 / 360,
                                normalize(slope, slope1, slope2),
                                (float) 1.0 - normalize(slopeStdErr, slopeStdErr1, slopeStdErr2)
                        ));

                        double x = aitoffProjection(star.getDir()).getX();
                        double y = aitoffProjection(star.getDir()).getY();

                        g.fillOval((int)(x - 3), (int)(y - 3), 6, 6);
                    }
                }

            }

            private float normalize(double x, double min, double max) {
                return (float)((x - min) / (max - min));
            }
        };
    }

    public Component getInterceptDistribution(final int height) throws Exception {
        return new Component() {
            @Override
            public void paint(Graphics g) {
                setSize(2 * height, height);

                double intercept1 = Collections.min(intercepts);
                double intercept2 = Collections.max(intercepts);

                double interceptStdErr1 = Collections.min(interceptStdErrs);
                double interceptStdErr2 = Collections.max(interceptStdErrs);

                for (int i = 0; i < rings.length; ++i) {
                    for (Star star : rings[i]) {
                        //int color = 255 - (int)((slopes.get(i) - slope1) / (slope2 - slope1) * 255);
                        double intercept = intercepts.get(i);
                        double interceptStdErr = interceptStdErrs.get(i);

                        g.setColor(Color.getHSBColor(
                                (float)300.0 / 360,
                                normalize(intercept, intercept1, intercept2),
                                (float)1.0 - normalize(interceptStdErr, interceptStdErr1, interceptStdErr2)
                        ));

                        double x = aitoffProjection(star.getDir()).getX();
                        double y = aitoffProjection(star.getDir()).getY();

                        g.fillOval((int)(x - 3), (int)(y - 3), 6, 6);
                    }
                }

            }

            private float normalize(double x, double min, double max) {
                return (float)((x - min) / (max - min));
            }
        };
    }

    private static SimpleRegression getRegression(final List<Star> stars) {
        SimpleRegression regression = new SimpleRegression();

        for (Star star : stars) {
            regression.addData(star.getR(), star.getExt());
        }

        return regression;
    }

    private static Point2D.Double aitoffProjection(final Spheric dir) {
        double l = dir.getL();
        double b = dir.getB();

        if (l > Math.PI) {
            l -= 2 * Math.PI;
        }

        double x = - 2 * Math.cos(b) * Math.sin(l / 2) / Math.sqrt(1 + Math.cos(b) * Math.cos(l / 2));
        double y = - Math.sin(b) / Math.sqrt(1 + Math.cos(b) * Math.cos(l / 2));

        x = (x / 2 + 1) * SIZE;
        y = (y + 1) * SIZE / 2;

        return new Point2D.Double(x, y);
    }
}
