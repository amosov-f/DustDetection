package ru.spbu.astro.dust.algo;

import gov.fnal.eag.healpix.PixTools;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.dust.model.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class DustTrendCalculator {
    private static final int N_SIDE = 18;
    private static final double EJECTION = 0.1;

    private final boolean includeIntercept;

    @NotNull
    private final List<List<Star>> rings;

    @NotNull
    private final Value[] slopes;
    @NotNull
    private final Value[] intercepts;

    @NotNull
    private final PixTools pixTools;

    private final double dr;

    public DustTrendCalculator(@NotNull final Catalogue catalogue, final double dr, final double r1, final double r2) {
        includeIntercept = r1 != 0;

        pixTools = new PixTools();
        this.dr = dr;

        rings = new ArrayList<>();
        for (int i = 0; i < 12 * N_SIDE * N_SIDE; i++) {
            rings.add(new ArrayList<>());
        }

        int count = 0;
        for (Star star : catalogue.getStars()) {
            if (r1 <= star.getR().value && star.getR().value <= r2 && star.getR().getRelativeError() <= dr) {
                count++;
                rings.get(getPix(star.getDir())).add(star);
            }
        }
        System.out.println("number of stars: " + count);

        slopes = new Value[rings.size()];
        intercepts = new Value[rings.size()];

        for (int i = 0; i < rings.size(); ++i) {
            SimpleRegression regression = getRegression(getSupportStars(rings.get(i)));

            slopes[i] = new Value(regression.getSlope(), regression.getSlopeStdErr());
            intercepts[i] = new Value(regression.getIntercept(), regression.getInterceptStdErr());
        }
    }

    public DustTrendCalculator(@NotNull final Catalogue catalogue, final double dr) {
        this(catalogue, dr, 0, Double.MAX_VALUE);
    }

    public DustTrendCalculator(@NotNull final Catalogue catalogue, final double r1, final double r2) {
        this(catalogue, Double.MAX_VALUE, r1, r2);
    }

    @NotNull
    public Value[] getSlopes() {
        return slopes;
    }

    @NotNull
    public Value[] getIntercepts() {
        return intercepts;
    }

    @NotNull
    private List<Star> getSupportStars(List<Star> stars) {
        List<Star> temp = new ArrayList<>(stars);

        SimpleRegression regression = getRegression(temp);

        double a = regression.getSlope();
        double b = regression.getIntercept();

        Collections.sort(temp, (star1, star2) -> Double.compare(
                Math.abs(a * star1.getR().value + b - star1.getExtinction().value),
                Math.abs(a * star2.getR().value + b - star2.getExtinction().value)
        ));

        return temp.subList(0, temp.size() - (int)(EJECTION * temp.size()));
    }

    @NotNull
    public List<Star> getSupportStars(@NotNull final Spheric dir) {
        return getSupportStars(rings.get(getPix(dir)));
    }

    @NotNull
    public List<Star> getMissStars(List<Star> stars) {
        List<Star> missStars = new ArrayList<>(stars);
        missStars.removeAll(getSupportStars(stars));
        return missStars;
    }

    @NotNull
    public List<Star> getMissStars(@NotNull final Spheric dir) {
        int pix = getPix(dir);

        List<Star> missStars = new ArrayList<>(rings.get(pix));
        missStars.removeAll(getSupportStars(dir));

        return missStars;
    }

    @NotNull
    public Value getSlope(Spheric dir) {
        return slopes[getPix(dir)];
    }

    @NotNull
    public Value getIntercept(Spheric dir) {
        return intercepts[getPix(dir)];
    }

    public List<Star> getMissStars() {
        final List<Star> missStars = new ArrayList<>();
        for (final List<Star> ring : rings) {
            missStars.addAll(getMissStars(ring));
        }
        return missStars;
    }

    @NotNull
    private SimpleRegression getRegression(@NotNull final List<Star> stars) {
        final SimpleRegression regression = new SimpleRegression(includeIntercept);
        for (final Star star : stars) {
            regression.addData(star.getR().value, star.getExtinction().value);
        }
        return regression;
    }

    public int getPix(@NotNull final Spheric dir) {
        return (int) pixTools.ang2pix_ring(N_SIDE, dir.getTheta(), dir.getPhi());
    }

    @NotNull
    public Spheric getPixCenter(final int pix) {
        return Spheric.valueOf(pixTools.pix2ang_ring(N_SIDE, (long) pix));
    }

    @Override
    public String toString() {
        final StringBuilder s = new StringBuilder();
        s.append("dr < ").append((int) (100 * dr)).append("%, n_side = ").append(N_SIDE).append("\n");
        s.append("№\tl\t\t\tb\t\t\tk\t\tsigma_k\tn\n");
        for (int i = 0; i < rings.size(); ++i) {
            final Spheric dir = getPixCenter(i);
            final Value k = slopes[i];
            final int n = rings.get(i).size();
            s.append(String.format(
                    "%d\t%f\t%f\t%.2f\t%.2f\t%d\n",
                    i, dir.l, dir.b, 1000 * k.value, 1000 * k.error, n
            ));
        }
        return s.toString();
    }

    public static void main(@NotNull final String[] args) throws FileNotFoundException {
        final Catalogue catalogue = Catalogue.HIPPARCOS_UPDATED;

        final DustTrendCalculator dustTrendCalculator = new DustTrendCalculator(catalogue, 0.25);

        final PrintWriter fout = new PrintWriter(new FileOutputStream("results/2.txt"));

        Locale.setDefault(Locale.US);
        fout.print(dustTrendCalculator.toString());
        fout.flush();
    }

}
