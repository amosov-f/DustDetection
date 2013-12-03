package ru.spbu.astro.dust;

import org.asterope.healpix.PixTools;

public class HealpixDistribution extends SphericDistribution {

    private double[] values;
    private double[] stdErrs;

    private PixTools pixTools;

    public HealpixDistribution(double[] values, double[] stdErrs) {
        if (values.length != stdErrs.length) {
            throw new IllegalArgumentException("Length of value must be equal to stdErrs length");
        }

        this.pixTools = new PixTools(Math.round(Math.sqrt(values.length / 12.0)));

        this.values = values;
        this.stdErrs = stdErrs;
    }

    @Override
    double[] get(final Spheric dir) {
        double theta = Math.PI / 2 - dir.getB();
        double phi = dir.getL() - Math.PI;

        int pix = (int)pixTools.ang2pix(theta, phi);

        return new double[]{values[pix], stdErrs[pix]};
    }
}
