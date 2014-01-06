package ru.spbu.astro.dust.algo;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import org.math.plot.FrameView;
import ru.spbu.astro.dust.model.Spheric;
import ru.spbu.astro.dust.model.Star;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import gov.fnal.eag.healpix.PixTools;
import java.util.*;
import java.util.List;

public class DustDetector {

    private static final int N_SIDE = 18;
    private static final double EJECTION = 0.1;

    private final List<Star>[] rings;

    private double[] slopes;
    private double[] slopeErrs;
    private double[] fullSlopeErrs;

    private double[] intercepts;
    private double[] interceptErrs;
    private double[] fullInterceptErrs;

    PixTools pixTools = new PixTools();

    public DustDetector(final List<Star> stars, Double r) {

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

            rings[getPix(star.getDir())].add(star);
        }

        slopes = new double[rings.length];
        slopeErrs = new double[rings.length];
        fullSlopeErrs = new double[rings.length];
        intercepts = new double[rings.length];
        interceptErrs = new double[rings.length];
        fullInterceptErrs = new double[rings.length];

        List<Integer> ringSizes = new ArrayList();

        for (int i = 0; i < rings.length; ++i) {
            SimpleRegression regression = getRegression(getSupportStars(rings[i]));

            slopes[i] = regression.getSlope();
            intercepts[i] = regression.getIntercept();

            fullSlopeErrs[i] = Math.abs(regression.getSlopeStdErr() / slopes[i]);
            fullInterceptErrs[i] = Math.abs(regression.getInterceptStdErr() / intercepts[i]);

            slopeErrs[i] = Math.min(fullSlopeErrs[i], 1);
            interceptErrs[i] = Math.min(fullInterceptErrs[i], 1);

            ringSizes.add(rings[i].size());
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

    public double[] getFullSlopeErrs() {
        return fullSlopeErrs;
    }

    public double[] getFullInterceptErrs() {
        return fullInterceptErrs;
    }


    private List<Star> getSupportStars(final List<Star> stars) {
        List<Star> temp = new ArrayList(stars);

        SimpleRegression regression = getRegression(temp);

        final double a = regression.getSlope();
        final double b = regression.getIntercept();

        Collections.sort(temp, new Comparator<Star>() {
            @Override
            public int compare(Star star1, Star star2) {
                return Double.compare(
                        Math.abs(a * star1.getR() + b - star1.getExt()),
                        Math.abs(a * star2.getR() + b - star2.getExt())
                );
            }
        });

        return temp.subList(0, temp.size() - (int)(EJECTION * temp.size()));
    }

    public List<Star> getSupportStars(final Spheric dir) {
        return getSupportStars(rings[getPix(dir)]);
    }

    public List<Star> getMissStars(final List<Star> stars) {
        List<Star> missStars = new ArrayList(stars);
        missStars.removeAll(getSupportStars(stars));
        return missStars;
    }

    public List<Star> getMissStars(final Spheric dir) {
        int pix = getPix(dir);

        List<Star> missStars = new ArrayList(rings[pix]);
        missStars.removeAll(getSupportStars(dir));

        return missStars;
    }

    public double getSlope(final Spheric dir) {
        return slopes[getPix(dir)];
    }

    public double getIntercept(final Spheric dir) {
        return intercepts[getPix(dir)];
    }

    public List<Star> getMissStars() {
        List<Star> missStars = new ArrayList();
        for (int i = 0; i < rings.length; ++i) {
            missStars.addAll(getMissStars(rings[i]));
        }
        return missStars;
    }

    private static SimpleRegression getRegression(final List<Star> stars) {
        SimpleRegression regression = new SimpleRegression();

        for (Star star : stars) {
            regression.addData(star.getR(), star.getExt());
        }

        return regression;
    }

    public int getPix(Spheric dir) {
        double theta = dir.getTheta();
        double phi = dir.getPhi();

        return (int)pixTools.ang2pix_ring(N_SIDE, theta, phi);
    }

    public class DustPix {
        public double l;
        public double b;
        public double slope;
        public double slopeErr;
        public double intercept;
        public double interceptErr;
        public int n;

        public DustPix(double l, double b, double slope, double slopeErr, double intercept, double interceptErr, int n) {
            this.l = l;
            this.b = b;
            this.slope = slope;
            this.slopeErr = slopeErr;
            this.intercept = intercept;
            this.interceptErr = interceptErr;
            this.n = n;
        }

        @Override
        public String toString() {
            return String.format(
                    "%f\t%f\t%.2f\t%d%%\t%.3f\t%d%%\t%d",
                    l,
                    b,
                    1000 * slope,
                    (int)(100 * slopeErr),
                    intercept,
                    (int)(100 * interceptErr),
                    n
            );
        }

    }

    public List<DustPix> getDustPixes() {
        List<DustPix> dustPixes = new ArrayList();
        for (int i = 0; i < rings.length; ++i) {
            double l = pixTools.pix2ang_ring(N_SIDE, i)[1];
            double b = Math.PI / 2 - pixTools.pix2ang_ring(N_SIDE, i)[0];
            double slope = slopes[i];
            double slopeErr = fullSlopeErrs[i];
            double intercept = intercepts[i];
            double interceptErr = fullInterceptErrs[i];
            int n = rings[i].size();

            dustPixes.add(new DustPix(l, b, slope, slopeErr, intercept, interceptErr, n));
        }
        return dustPixes;
    }



}
