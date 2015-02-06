package ru.spbu.astro.commons;

import gov.fnal.eag.healpix.PixTools;
import org.jetbrains.annotations.NotNull;

import static java.lang.Math.round;
import static java.lang.Math.sqrt;

/**
 * User: amosov-f
 * Date: 28.09.14
 * Time: 0:36
 */
public final class HealpixTools {
    private static final int TWELVE = 12;

    private HealpixTools() {
    }

    public static int nPix(final int nSide) {
        return TWELVE * nSide * nSide;
    }

    public static int nSide(final int nPix) {
        return (int) round(sqrt((double) nPix / TWELVE));
    }

    public static int pix(final int nSide, @NotNull final Spheric dir) {
        return (int) new PixTools().ang2pix_ring(nSide, dir.getPhi(), dir.getTheta());
    }
}
