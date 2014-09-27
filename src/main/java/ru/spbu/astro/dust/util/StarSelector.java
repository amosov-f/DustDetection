package ru.spbu.astro.dust.util;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.dust.func.HealpixCounter;
import ru.spbu.astro.dust.graph.HammerProjection;
import ru.spbu.astro.dust.model.Catalogue;
import ru.spbu.astro.dust.model.Spheric;
import ru.spbu.astro.dust.model.Star;

import java.util.ArrayList;
import java.util.List;

public final class StarSelector {

    private final Catalogue catalogue;

    public StarSelector(Catalogue catalogue) {
        this.catalogue = catalogue;
    }

    @NotNull
    public StarSelector selectByBVColor(final double min, final double max) {
        final Catalogue selection = new Catalogue();
        for (final Star star : catalogue.getStars()) {
            final double bvColor = star.getBVColor().getValue();
            if (min <= bvColor && bvColor <= max) {
                selection.add(star);
            }
        }
        return new StarSelector(selection);
    }

    @NotNull
    public StarSelector selectByAbsoluteMagnitude(final double min, final double max) {
        final Catalogue selection = new Catalogue();
        for (final Star star : catalogue.getStars()) {
            if (min <= star.getAbsoluteMagnitude().getValue() && star.getAbsoluteMagnitude().getValue() <= max) {
                selection.add(star);
            }
        }
        return new StarSelector(selection);
    }

    @NotNull
    public StarSelector selectByBVColorError(final double lim) {
        final Catalogue selection = new Catalogue();
        for (final Star star : catalogue.getStars()) {
            if (star.getBVColor().getRelativeError() < lim) {
                selection.add(star);
            }
        }
        return new StarSelector(selection);
    }

    @NotNull
    public StarSelector selectByParallaxRelativeError(final double lim) {
        final Catalogue selection = new Catalogue();
        for (final Star star : catalogue.getStars()) {
            if (star.getParallax().getRelativeError() < lim) {
                selection.add(star);
            }
        }
        return new StarSelector(selection);
    }

    @NotNull
    public StarSelector selectByExistLuminosityClass() {
        final Catalogue selection = new Catalogue();
        for (Star star : catalogue.getStars()) {
            if (star.getSpectralType().getLuminosityClass() != null) {
                selection.add(star);
            }
        }
        return new StarSelector(selection);
    }

    @NotNull
    public Catalogue getCatalogue() {
        return catalogue;
    }

    @NotNull
    @Override
    public String toString() {
        return catalogue.toString();
    }

    public static void main(String[] args) {
        final Catalogue catalogue = Catalogue.HIPPARCOS_2007;

        final Catalogue selection = new StarSelector(catalogue)
                .selectByBVColor(1.525, 1.95)
                .selectByAbsoluteMagnitude(4.5, 9.5)
                .catalogue;

        final List<Spheric> dirs = new ArrayList<>();
        for (Star s : selection.getStars()) {
            dirs.add(s.getDir());
        }

        System.out.println(selection);
        new HammerProjection(new HealpixCounter(dirs, 18), HammerProjection.Mode.WITH_ERRORS);
    }

}
