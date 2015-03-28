package ru.spbu.astro.healpix;

import gov.fnal.eag.healpix.PixTools;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.commons.Spheric;
import ru.spbu.astro.commons.Star;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    
    public int nPix() {
        return nPix(nSide);
    }
    
    @NotNull
    public Star[][] split(@NotNull final Star[] stars) {
        final List<List<Star>> rings = new ArrayList<>();
        for (int i = 0; i < nPix(); i++) {
            rings.add(new ArrayList<>());
        }
        Arrays.stream(stars).forEach(star -> rings.get(getPix(star.getDir())).add(star));
        return rings.stream().map(ring -> ring.toArray(new Star[ring.size()])).toArray(Star[][]::new);
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
