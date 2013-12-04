package ru.spbu.astro.dust;

import org.asterope.healpix.PixTools;

public class HealpixDistribution extends SphericDistribution {

    private double[] values;
    private double[] errs;

    private PixTools pixTools;

    public HealpixDistribution(double[] values, double[] errs) {
        if (values.length != errs.length) {
            throw new IllegalArgumentException("Length of value must be equal to errs length");
        }

        this.pixTools = new PixTools(Math.round(Math.sqrt(values.length / 12.0)));

        this.values = values;
        this.errs = errs;
    }

    @Override
    double[] get(final Spheric dir) {
        double theta = Math.PI / 2 - dir.getB();
        double phi = dir.getL();

        int pix = (int)pixTools.ang2pix(theta, phi);

        return new double[]{values[pix], errs[pix]};
    }
}
