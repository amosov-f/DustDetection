package ru.spbu.astro.dust.func;

import gov.fnal.eag.healpix.PixTools;
import ru.spbu.astro.dust.model.Spheric;
import ru.spbu.astro.dust.model.Value;

public class HealpixDistribution implements SphericDistribution {

    private final Value[] values;

    private final PixTools pixTools;
    private final long nSide;

    public HealpixDistribution(final Value[] values) throws IllegalArgumentException {
        pixTools = new PixTools();
        nSide = Math.round(Math.sqrt(values.length / 12.0));

        this.values = values;
    }

    @Override
    public Value get(final Spheric dir) {
        double theta = dir.getTheta();
        double phi = dir.getPhi();

        int pix = (int) pixTools.ang2pix_ring(nSide, theta, phi);

        return values[pix];
    }

}
