package ru.spbu.astro.dust.algo;

import ru.spbu.astro.dust.func.HealpixCounter;
import ru.spbu.astro.dust.graph.HammerProjection;
import ru.spbu.astro.dust.model.Catalogue;
import ru.spbu.astro.dust.model.Spheric;
import ru.spbu.astro.dust.model.Star;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public final class StarSelector {

    private final Catalogue catalogue;

    public StarSelector(Catalogue catalogue) {
        this.catalogue = catalogue;
    }

    public StarSelector selectByBVColor(double min, double max) {
        Catalogue selection = new Catalogue();

        for (Star s : catalogue.getStars()) {
            double bvColor = s.bvColor.value;

            if (min <= bvColor && bvColor <= max) {

                selection.add(s);
            }
        }

        return new StarSelector(selection);
    }

    public StarSelector selectByAbsoluteMagnitude(double min, double max) {
        Catalogue selection = new Catalogue();

        for (Star s : catalogue.getStars()) {
            if (min <= s.getAbsoluteMagnitude().value && s.getAbsoluteMagnitude().value <= max) {
                selection.add(s);
            }
        }

        return new StarSelector(selection);
    }

    public StarSelector selectByBVColorError(double lim) {
        Catalogue selection = new Catalogue();

        for (Star s : catalogue.getStars()) {
            if (s.bvColor.getRelativeError() < lim) {
                selection.add(s);
            }
        }

        return new StarSelector(selection);
    }

    public StarSelector selectByParallaxRelativeError(double lim) {
        Catalogue selection = new Catalogue();

        for (Star s : catalogue.getStars()) {
            if (s.parallax.getRelativeError() < lim) {
                selection.add(s);
            }
        }

        return new StarSelector(selection);
    }

    public StarSelector selectByExistLuminosityClass() {
        Catalogue selection = new Catalogue();

        for (Star s : catalogue.getStars()) {
            if (s.spectralType.getLuminosityClass() != null) {
                selection.add(s);
            }
        }

        return new StarSelector(selection);
    }

    public Catalogue getCatalogue() {
        return catalogue;
    }

    @Override
    public String toString() {
        return catalogue.toString();
    }

    public static void main(String[] args) throws FileNotFoundException {
        Catalogue catalogue = new Catalogue("datasets/hipparcos1997.txt");
        catalogue.updateBy(new Catalogue("datasets/hipparcos2007.txt"));
        catalogue.updateBy(new LuminosityClassifier(catalogue));

        Catalogue selection = new StarSelector(catalogue)
                .selectByBVColor(1.525, 1.95)
                .selectByAbsoluteMagnitude(4.5, 9.5)
                .catalogue;

        List<Spheric> dirs = new ArrayList<>();
        for (Star s : selection.getStars()) {
            dirs.add(s.dir);
        }

        System.out.println(selection);
        new HammerProjection(new HealpixCounter(dirs, 18), HammerProjection.Mode.WITH_ERRORS);
    }

}
