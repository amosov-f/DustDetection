package ru.spbu.astro.healpix;

import gov.fnal.eag.healpix.PixTools;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.commons.Spheric;
import ru.spbu.astro.commons.Star;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.round;
import static java.lang.Math.sqrt;

/**
 * User: amosov-f
 * Date: 28.09.14
 * Time: 0:36
 */
public final class Healpix {
    private static final PixTools PIX_TOOLS = PixTools.getInstance();

    private final int nSide;

    public Healpix(final int nSide) {
        this.nSide = nSide;
    }

    public static int nPix(final int nSide) {
        // noinspection MagicNumber
        return 12 * nSide * nSide;
    }
    
    public int getNPix() {
        return nPix(nSide);
    }

    public int getNSide() {
        return nSide;
    }
    
    @NotNull
    public Star[][] split(@NotNull final Star[] stars) {
        final List<List<Star>> rings = IntStream.range(0, getNPix()).mapToObj(pix -> new ArrayList<Star>()).collect(Collectors.toList());
        Arrays.stream(stars).forEach(star -> rings.get(getPix(star.getDir())).add(star));
        return rings.stream().map(ring -> ring.toArray(new Star[ring.size()])).toArray(Star[][]::new);
    }

    public static int nSide(final int nPix) {
        // noinspection MagicNumber
        return (int) round(sqrt(nPix / 12.0));
    }

    public int getPix(@NotNull final Spheric dir) {
        return (int) PIX_TOOLS.ang2pix_ring(nSide, dir.getPhi(), dir.getTheta());
    }

    public double getPixArea() {
        return 4 * Math.PI / getNPix();
    }

    @NotNull
    public Spheric getCenter(final int pix) {
        return Spheric.valueOf(PIX_TOOLS.pix2ang_ring(nSide, pix));
    }

    @NotNull
    public Spheric getCenter(@NotNull final Spheric dir) {
        return getCenter(getPix(dir));
    }

    @NotNull
    @Deprecated
    public int[] getNeighbours(final int pix) {
        // noinspection unchecked
        return PIX_TOOLS.neighbours_nest(nSide, pix).stream().mapToInt(i -> ((Long) i).intValue()).toArray();
    }
}
