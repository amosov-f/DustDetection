package ru.spbu.astro.dust.func;

import gov.fnal.eag.healpix.PixTools;
import ru.spbu.astro.dust.model.Spheric;
import ru.spbu.astro.dust.model.Value;

public class CountHealpixDistribution extends HealpixDistribution {

    public CountHealpixDistribution(int nSide, final Spheric[] dirs) {
        super(toPixes(nSide, dirs));
    }

    private static Value[] toPixes(int nSide, final Spheric[] dirs) {
        final Value[] values = new Value[12 * nSide * nSide];
        for (int i = 0; i < values.length; ++i) {
            values[i] = new Value(0, 0);
        }

        PixTools pixTools = new PixTools();

        for (final Spheric dir : dirs) {
            double theta = dir.getTheta();
            double phi = dir.getPhi();

            int pix = (int) pixTools.ang2pix_ring(nSide, theta, phi);

            values[pix] = new Value(values[pix].value + 1, values[pix].error);
        }

        for (final Value value : values) {
            System.out.println(value);
        }

        return values;
    }

}
