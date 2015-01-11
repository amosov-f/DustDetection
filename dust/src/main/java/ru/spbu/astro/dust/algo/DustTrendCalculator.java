package ru.spbu.astro.dust.algo;

import gov.fnal.eag.healpix.PixTools;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.core.HealpixTools;
import ru.spbu.astro.core.Spheric;
import ru.spbu.astro.core.Star;
import ru.spbu.astro.dust.ml.RansacLinearRegression;
import ru.spbu.astro.dust.ml.SimpleRegression;
import ru.spbu.astro.dust.model.Catalogue;
import ru.spbu.astro.dust.model.Catalogues;
import ru.spbu.astro.util.Value;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public final class DustTrendCalculator {
    private static final int N_SIDE = 18;

    @NotNull
    private final List<List<Star>> inliers = new ArrayList<>();
    @NotNull
    private final List<List<Star>> outliers = new ArrayList<>();

    @NotNull
    private final Value[] slopes;
    @NotNull
    private final Value[] intercepts;

    @NotNull
    private final PixTools pixTools;

    public DustTrendCalculator(@NotNull final List<Star> stars) {
        this(stars, false);
    }

    public DustTrendCalculator(@NotNull final List<Star> stars, final boolean includeIntercept) {
        System.out.println("number of stars: " + stars.size());

        pixTools = new PixTools();

        final List<List<Star>> rings = new ArrayList<>();
        for (int i = 0; i < HealpixTools.nPix(N_SIDE); i++) {
            rings.add(new ArrayList<>());
        }

        final Map<Integer, Star> id2star = new HashMap<>();
        for (final Star star : stars) {
            id2star.put(star.getId(), star);
            rings.get(getPix(star.getDir())).add(star);
        }

        slopes = new Value[rings.size()];
        intercepts = new Value[rings.size()];

        for (int pix = 0; pix < rings.size(); pix++) {
            final SimpleRegression regression = new RansacLinearRegression(includeIntercept);
            for (final Star star : rings.get(pix)) {
                regression.add(star.getId(), star.getR(), star.getExtinction());
            }
            if (regression.train()) {
                slopes[pix] = regression.getSlope();
                intercepts[pix] = regression.getIntercept();
                inliers.add(regression.getInliers().stream().map(id2star::get).collect(Collectors.toList()));
                outliers.add(regression.getOutliers().stream().map(id2star::get).collect(Collectors.toList()));
            } else {
                inliers.add(null);
                outliers.add(null);
            }
        }
    }

    public static void main(@NotNull final String[] args) throws FileNotFoundException {
        final Catalogue catalogue = Catalogues.HIPPARCOS_UPDATED;

        final DustTrendCalculator dustTrendCalculator = new DustTrendCalculator(catalogue.getStars());

        final PrintWriter fout = new PrintWriter(new FileOutputStream("results/2.txt"));

        Locale.setDefault(Locale.US);
        fout.print(dustTrendCalculator.toString());
        fout.flush();
    }

    @NotNull
    public Value[] getSlopes() {
        return slopes;
    }

    @NotNull
    public Value[] getIntercepts() {
        return intercepts;
    }

    @Nullable
    public List<Star> getBaseStars(@NotNull final Spheric dir) {
        return inliers.get(getPix(dir));
    }

    @Nullable
    public List<Star> getOutlierStars(@NotNull final Spheric dir) {
        return outliers.get(getPix(dir));
    }

    @Nullable
    public Value getSlope(@NotNull final Spheric dir) {
        return slopes[getPix(dir)];
    }

    @Nullable
    public Value getIntercept(@NotNull final Spheric dir) {
        return intercepts[getPix(dir)];
    }

    public int getPix(@NotNull final Spheric dir) {
        return (int) pixTools.ang2pix_ring(N_SIDE, dir.getPhi(), dir.getTheta());
    }

    @NotNull
    public Spheric getPixCenter(final int pix) {
        return Spheric.valueOf(pixTools.pix2ang_ring(N_SIDE, (long) pix));
    }

    @Override
    public String toString() {
        double dr = 0;
        for (final List<Star> ring : inliers) {
            for (final Star star : ring) {
                dr = Math.max(dr, star.getR().getRelativeError());
            }
        }

        final StringBuilder s = new StringBuilder();
        s.append("dr <= ").append((int) (100 * dr)).append("%, n_side = ").append(N_SIDE).append("\n");
        s.append("№\tl\t\t\tb\t\t\tk\t\tsigma_k\tn\n");
        for (int i = 0; i < inliers.size(); ++i) {
            final Spheric dir = getPixCenter(i);
            final Value k = slopes[i];
            final int n = inliers.get(i).size();
            s.append(String.format(
                    "%d\t%f\t%f\t%.2f\t%.2f\t%d\n",
                    i, dir.getL(), dir.getB(), 1000 * k.getValue(), 1000 * k.getError(), n
            ));
        }
        return s.toString();
    }

}
