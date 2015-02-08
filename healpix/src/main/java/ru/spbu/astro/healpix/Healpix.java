package ru.spbu.astro.healpix;

import gov.fnal.eag.healpix.PixTools;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.commons.Spheric;

import static java.lang.Math.round;
import static java.lang.Math.sqrt;

/**
 * User: amosov-f
 * Date: 28.09.14
 * Time: 0:36
 */
public final class Healpix {
    private static final int TWELVE = 12;

    @NotNull
    private final PixTools pixTools = new PixTools();
    private final int nSide;

    public Healpix(final int nSide) {
        this.nSide = nSide;
    }

    public static int nPix(final int nSide) {
        return TWELVE * nSide * nSide;
    }

    public static int nSide(final int nPix) {
        return (int) round(sqrt((double) nPix / TWELVE));
    }

    public int getPix(@NotNull final Spheric dir) {
        return (int) pixTools.ang2pix_ring(nSide, dir.getPhi(), dir.getTheta());
    }

    @NotNull
    public Spheric getCenter(final int pix) {
        return Spheric.valueOf(pixTools.pix2ang_ring(nSide, pix));
    }

    @NotNull
    public Spheric getCenter(@NotNull final Spheric dir) {
        return getCenter(getPix(dir));
    }
}
