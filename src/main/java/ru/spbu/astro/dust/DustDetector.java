package ru.spbu.astro.dust;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.asterope.healpix.PixTools;

import java.util.*;

public class DustDetector {

    private static final int N_SIDE = 18;
    private static final double EJECTION = 0.1;

    private final List<Star>[] rings;

    private double[] slopes;
    private double[] slopeErrs;

    private double[] intercepts;
    private double[] interceptErrs;

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
            double phi = star.getDir().getL();

            rings[(int)pixTools.ang2pix(theta, phi)].add(star);
        }

        slopes = new double[rings.length];
        slopeErrs = new double[rings.length];
        intercepts = new double[rings.length];
        interceptErrs = new double[rings.length];

        for (int i = 0; i < rings.length; ++i) {
            SimpleRegression regression = getRegression(rings[i]);

            final double a = regression.getSlope();
            final double b = regression.getSlope();

            Collections.sort(rings[i], new Comparator<Star>() {
                @Override
                public int compare(Star star1, Star star2) {
                    return Double.compare(
                            Math.pow(a * star1.getR() + b - star1.getExt(), 2),
                            Math.pow(a * star2.getR() + b - star2.getExt(), 2)
                    );
                }
            });

            regression = getRegression(rings[i].subList(0, rings[i].size() - (int)(EJECTION * rings[i].size())));

            slopes[i] = regression.getSlope();
            intercepts[i] = regression.getIntercept();

            slopeErrs[i] = Math.min(Math.abs(regression.getSlopeStdErr() / regression.getSlope()), 1);
            interceptErrs[i] = Math.min(Math.abs(regression.getInterceptStdErr() / regression.getIntercept()), 1);
        }
    }

    public double[] getSlopes() {
        return slopes;
    }

    public double[] getSlopeErrs() {
        return slopeErrs;
    }

    public double[] getIntercepts() {
        return intercepts;
    }

    public double[] getInterceptErrs() {
        return interceptErrs;
    }

    /*public Component getInterceptDistribution(final int height) throws Exception {
        return new Component() {
            @Override
            public void paint(Graphics g) {
                setSize(2 * height, height);

                double intercept1 = Collections.min(intercepts);
                double intercept2 = Collections.max(intercepts);

                double interceptStdErr1 = Collections.min(interceptErrs);
                double interceptStdErr2 = Collections.max(interceptErrs);

                for (int i = 0; i < rings.length; ++i) {
                    for (Star star : rings[i]) {
                        //int color = 255 - (int)((slopes.get(i) - slope1) / (slope2 - slope1) * 255);
                        double intercept = intercepts.get(i);
                        double interceptStdErr = interceptErrs.get(i);

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
