package ru.spbu.astro.dust.func;

import gov.fnal.eag.healpix.PixTools;
import ru.spbu.astro.dust.model.Spheric;

public class HealpixDistribution implements SphericDistribution {

    private double[] values;
    private double[] errs;

    private PixTools pixTools;
    private final long N_SIDE;

    public HealpixDistribution(double[] values, double[] errs) throws IllegalArgumentException {
        if (errs != null && values.length != errs.length) {
            throw new IllegalArgumentException("Length of value must be equal to errs length");
        }

        this.pixTools = new PixTools();
        N_SIDE = Math.round(Math.sqrt(values.length / 12.0));

        this.values = values;
        this.errs = errs;
    }

    public HealpixDistribution(double[] values) {
        this(values, null);
    }

    @Override
    public double[] get(final Spheric dir) {
        double theta = dir.getTheta();
        double phi = dir.getPhi();

        int pix = (int)pixTools.ang2pix_ring(N_SIDE, theta, phi);

        if (errs == null) {
            return new double[]{values[pix]};
        }
        return new double[]{values[pix], errs[pix]};
    }

    @Override
    public int dim() {
        if (errs == null) {
            return 1;
        }
        return 2;
    }
}
