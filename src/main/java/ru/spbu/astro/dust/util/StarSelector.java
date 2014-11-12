package ru.spbu.astro.dust.util;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.dust.func.HealpixCounter;
import ru.spbu.astro.dust.graph.HammerProjection;
import ru.spbu.astro.dust.model.Catalogue;
import ru.spbu.astro.dust.model.Spheric;
import ru.spbu.astro.dust.model.Star;
import ru.spbu.astro.dust.model.spect.LuminosityClass;
import ru.spbu.astro.dust.model.spect.SpectClass.TypeSymbol;
import ru.spbu.astro.dust.model.spect.SpectType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class StarSelector {
    @NotNull
    private final List<Star> stars;

    public StarSelector(@NotNull final Catalogue catalogue) {
        this(catalogue.getStars());
    }

    public StarSelector(@NotNull final List<Star> stars) {
        this.stars = stars;
    }

    @NotNull
    public StarSelector selectByBVColor(final double min, final double max) {
        final List<Star> selection = new ArrayList<>();
        for (final Star star : stars) {
            final double bvColor = star.getBVColor().getValue();
            if (min <= bvColor && bvColor <= max) {
                selection.add(star);
            }
        }
        return new StarSelector(selection);
    }

    @NotNull
    public StarSelector selectByAbsoluteMagnitude(final double min, final double max) {
        final List<Star> selection = new ArrayList<>();
        for (final Star star : stars) {
            final double absoluteMagnitude = star.getAbsoluteMagnitude().getValue();
            if (min <= absoluteMagnitude && absoluteMagnitude <= max) {
                selection.add(star);
            }
        }
        return new StarSelector(selection);
    }

    @NotNull
    public StarSelector selectByBVColorError(final double lim) {
        final List<Star> selection = new ArrayList<>();
        for (final Star star : stars) {
            if (star.getBVColor().getRelativeError() < lim) {
                selection.add(star);
            }
        }
        return new StarSelector(selection);
    }

    @NotNull
    public StarSelector selectByParallaxRelativeError(final double lim) {
        final List<Star> selection = new ArrayList<>();
        for (final Star star : stars) {
            if (star.getParallax().getRelativeError() < lim) {
                selection.add(star);
            }
        }
        return new StarSelector(selection);
    }

    @NotNull
    public StarSelector selectByR(final double r1, final double r2) {
        final List<Star> selection = new ArrayList<>();
        for (final Star star : stars) {
            final double r = star.getR().getValue();
            if (r1 <= r && r <= r2) {
                selection.add(star);
            }
        }
        return new StarSelector(selection);
    }

    @NotNull
    public StarSelector selectByExistLuminosityClass() {
        final List<Star> selection = new ArrayList<>();
        for (Star star : stars) {
            if (star.getSpectType().getLumin() != null) {
                selection.add(star);
            }
        }
        return new StarSelector(selection);
    }

    @NotNull
    public StarSelector selectByNegativeExtinction() {
        return new StarSelector(stars.stream()
                .filter(star -> star.getExtinction().getMax() < 0)
                .collect(Collectors.toList()));
    }

    @NotNull
    public StarSelector selectByLuminosityClass(@NotNull final LuminosityClass luminosityClass) {
        return new StarSelector(stars.stream()
                .filter(star -> star.getSpectType().getLumin() == luminosityClass)
                .collect(Collectors.toList()));
    }

    @NotNull
    public StarSelector selectBySpectralType(@NotNull TypeSymbol typeSymbol, final double min, final double max) {
        final List<Star> selection = new ArrayList<>();
        for (final Star star : stars) {
            final SpectType spectralType = star.getSpectType();
            if (spectralType.getTypeSymbol() == typeSymbol && min <= spectralType.getTypeNumber() && spectralType.getTypeNumber() <= max) {
                selection.add(star);
            }
        }
        return new StarSelector(selection);
    }

    @NotNull
    public List<Star> getStars() {
        return new ArrayList<>(stars);
    }

    public static void main(String[] args) {
        final Catalogue catalogue = Catalogue.HIPPARCOS_2007;

        final List<Star> selection = new StarSelector(catalogue.getStars())
                .selectByBVColor(1.525, 1.95)
                .selectByAbsoluteMagnitude(4.5, 9.5).getStars();

        final List<Spheric> dirs = selection.stream().map(Star::getDir).collect(Collectors.toList());

        System.out.println(selection);
        final HammerProjection hammerProjection = new HammerProjection(
                new HealpixCounter(dirs, 18), HammerProjection.Mode.WITH_ERRORS
        );
        hammerProjection.setVisible(true);
    }
}
