package ru.spbu.astro.dust;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.asterope.healpix.PixTools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

            double theta = star.getDir().getTheta();
            double phi = star.getDir().getPhi();

            rings[(int)pixTools.ang2pix(theta, phi)].add(star);
        }

        slopes = new double[rings.length];
        slopeErrs = new double[rings.length];
        intercepts = new double[rings.length];
        interceptErrs = new double[rings.length];

        for (int i = 0; i < rings.length; ++i) {
            SimpleRegression regression = getRegression(getSupportStars(rings[i]));

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

    private List<Star> getSupportStars(final List<Star> stars) {
        List<Star> temp = new ArrayList(stars);

        SimpleRegression regression = getRegression(temp);

        final double a = regression.getSlope();
        final double b = regression.getSlope();

        Collections.sort(temp, new Comparator<Star>() {
            @Override
            public int compare(Star star1, Star star2) {
                return Double.compare(
                        Math.pow(a * star1.getR() + b - star1.getExt(), 2),
                        Math.pow(a * star2.getR() + b - star2.getExt(), 2)
                );
            }
        });

        return temp.subList(0, temp.size() - (int)(EJECTION * temp.size()));
    }

    public List<Star> getSupportStars(final Spheric dir) {
        double theta = dir.getTheta();
        double phi = dir.getPhi();

        return getSupportStars(rings[(int)pixTools.ang2pix(theta, phi)]);
    }

    public List<Star> getMissStars(final Spheric dir) {
        double theta = dir.getTheta();
        double phi = dir.getPhi();
        int pix = (int)pixTools.ang2pix(theta, phi);

        List<Star> result = new ArrayList(rings[pix]);
        result.removeAll(getSupportStars(dir));

        return result;
    }

    public double getSlope(final Spheric dir) {
        double theta = dir.getTheta();
        double phi = dir.getPhi();

        return slopes[(int)pixTools.ang2pix(theta, phi)];
    }

    public double getIntercept(final Spheric dir) {
        double theta = dir.getTheta();
        double phi = dir.getPhi();

        return intercepts[(int)pixTools.ang2pix(theta, phi)];
    }

    private static SimpleRegression getRegression(final List<Star> stars) {
        SimpleRegression regression = new SimpleRegression();

        for (Star star : stars) {
            regression.addData(star.getR(), star.getExt());
        }

        return regression;
    }

}
