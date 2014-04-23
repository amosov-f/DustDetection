package ru.spbu.astro.dust.algo;

import gov.fnal.eag.healpix.PixTools;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import ru.spbu.astro.dust.model.Catalogue;
import ru.spbu.astro.dust.model.Spheric;
import ru.spbu.astro.dust.model.Star;
import ru.spbu.astro.dust.model.Value;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class DustDetector {

    private static final int N_SIDE = 18;
    private static final double EJECTION = 0.1;

    private final List<List<Star>> rings;

    private final Value[] slopes;
    private final Value[] intercepts;

    private final PixTools pixTools;

    private final double dr;

    public DustDetector(final Catalogue catalogue, final double dr) {
        pixTools = new PixTools();
        this.dr = dr;

        rings = new ArrayList<>();
        for (int i = 0; i < 12 * N_SIDE * N_SIDE; ++i) {
            rings.add(new ArrayList<>());
        }

        int count = 0;
        for (Star star : catalogue.getStars()) {
            if (star.getR().getRelativeError() <= dr) {
                count++;
                rings.get(getPix(star.dir)).add(star);
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

    public Value[] getSlopes() {
        return slopes;
    }

    public Value[] getIntercepts() {
        return intercepts;
    }

    private List<Star> getSupportStars(final List<Star> stars) {
        final List<Star> temp = new ArrayList<>(stars);

        final SimpleRegression regression = getRegression(temp);

        final double a = regression.getSlope();
        final double b = regression.getIntercept();

        Collections.sort(temp, (star1, star2) -> Double.compare(
                Math.abs(a * star1.getR().value + b - star1.getExtinction().value),
                Math.abs(a * star2.getR().value + b - star2.getExtinction().value)
        ));

        return temp.subList(0, temp.size() - (int)(EJECTION * temp.size()));
    }

    public List<Star> getSupportStars(final Spheric dir) {
        return getSupportStars(rings.get(getPix(dir)));
    }

    public List<Star> getMissStars(final List<Star> stars) {
        List<Star> missStars = new ArrayList<>(stars);
        missStars.removeAll(getSupportStars(stars));
        return missStars;
    }

    public List<Star> getMissStars(final Spheric dir) {
        int pix = getPix(dir);

        List<Star> missStars = new ArrayList<>(rings.get(pix));
        missStars.removeAll(getSupportStars(dir));

        return missStars;
    }

    public Value getSlope(final Spheric dir) {
        return slopes[getPix(dir)];
    }

    public Value getIntercept(final Spheric dir) {
        return intercepts[getPix(dir)];
    }

    public List<Star> getMissStars() {
        final List<Star> missStars = new ArrayList<>();
        for (List<Star> ring : rings) {
            missStars.addAll(getMissStars(ring));
        }
        return missStars;
    }

    private static SimpleRegression getRegression(final List<Star> stars) {
        final SimpleRegression regression = new SimpleRegression();

        for (Star star : stars) {
            regression.addData(star.getR().value, star.getExtinction().value);
        }

        return regression;
    }

    public int getPix(Spheric dir) {
        double theta = dir.getTheta();
        double phi = dir.getPhi();

        return (int) pixTools.ang2pix_ring(N_SIDE, theta, phi);
    }

    public Spheric getPixCenter(int pix) {
        return Spheric.valueOf(pixTools.pix2ang_ring(N_SIDE, (long) pix));
    }

    @Override
    public String toString() {
        final StringBuilder s = new StringBuilder();
        s.append("dr < ").append((int) (100 * dr)).append("%, n_side = ").append(N_SIDE).append("\n");
        s.append("№\tl\t\t\tb\t\t\ta\t\tsigma_a\tb\t\tsigma_b\tn\n");
        for (int i = 0; i < rings.size(); ++i) {
            final Spheric dir = getPixCenter(i);
            final Value a = slopes[i];
            final Value b = intercepts[i];
            int n = rings.get(i).size();
            s.append(String.format(
                    "%d\t%f\t%f\t%.2f\t%.2f\t%.3f\t%.3f\t%d\n",
                    i, dir.l, dir.b, 1000 * a.value, 1000 * a.error, b.value, b.error, n
            ));
        }
        return s.toString();
    }

    public static void main(final String[] args) throws FileNotFoundException {
        final Catalogue catalogue = new Catalogue("datasets/hipparcos1997.txt");
        catalogue.updateBy(new Catalogue("datasets/hipparcos2007.txt"));
        catalogue.updateBy(new LuminosityClassifier(catalogue));

        final DustDetector dustDetector = new DustDetector(catalogue, 0.25);

        final PrintWriter fout = new PrintWriter(new FileOutputStream("results/2.txt"));

        Locale.setDefault(Locale.US);
        fout.print(dustDetector.toString());
        fout.flush();
    }

}
