package ru.spbu.astro.dust;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.asterope.healpix.PixTools;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DustDetector {

    private static final int N_SIDE = 15;

    private final List<Star>[] rings;

    private double[] slopes;
    private double[] slopeStdErrs;

    private double[] intercepts;
    private double[] interceptStdErrs;

    PixTools pixTools;

    public DustDetector(final List<Star> stars, Double r) {
        pixTools = new PixTools(N_SIDE);

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
            double phi = star.getDir().getL() - Math.PI;

            rings[(int)pixTools.ang2pix(theta, phi)].add(star);
        }

        slopes = new double[rings.length];
        slopeStdErrs = new double[rings.length];
        intercepts = new double[rings.length];
        interceptStdErrs = new double[rings.length];

        for (int i = 0; i < rings.length; ++i) {
            SimpleRegression regression = getRegression(rings[i]);
            slopes[i] = regression.getSlope();
            slopeStdErrs[i] = regression.getSlopeStdErr();
            intercepts[i] = regression.getIntercept();
            interceptStdErrs[i] = regression.getInterceptStdErr();
        }
    }

    public double[] getSlopes() {
        return slopes;
    }

    public double[] getSlopeStdErrs() {
        return slopeStdErrs;
    }

    public double[] getIntercepts() {
        return intercepts;
    }

    public double[] getInterceptStdErrs() {
        return interceptStdErrs;
    }

    /*public Component getInterceptDistribution(final int height) throws Exception {
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
    }   */

    private static SimpleRegression getRegression(final List<Star> stars) {
        SimpleRegression regression = new SimpleRegression();

        for (Star star : stars) {
            regression.addData(star.getR(), star.getExt());
        }

        return regression;
    }

}
