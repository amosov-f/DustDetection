package ru.spbu.astro.dust.util;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.core.Star;
import ru.spbu.astro.core.spect.LuminosityClass;
import ru.spbu.astro.core.spect.SpectClass;
import ru.spbu.astro.core.spect.SpectClass.TypeSymbol;
import ru.spbu.astro.dust.model.Catalogue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
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
    public StarSelector bvColor(final double min, final double max) {
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
    public StarSelector absoluteMagnitude(final double min, final double max) {
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
    public StarSelector bvColorError(final double lim) {
        final List<Star> selection = new ArrayList<>();
        for (final Star star : stars) {
            if (star.getBVColor().getRelativeError() < lim) {
                selection.add(star);
            }
        }
        return new StarSelector(selection);
    }

    @NotNull
    public StarSelector parallaxRelativeError(final double lim) {
        final List<Star> selection = new ArrayList<>();
        for (final Star star : stars) {
            if (star.getParallax().getRelativeError() < lim) {
                selection.add(star);
            }
        }
        return new StarSelector(selection);
    }

    @NotNull
    public StarSelector r(final double r1, final double r2) {
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
    public StarSelector existLuminosityClass() {
        final List<Star> selection = new ArrayList<>();
        for (Star star : stars) {
            if (star.getSpectType().getLumin() != null) {
                selection.add(star);
            }
        }
        return new StarSelector(selection);
    }

    @NotNull
    public StarSelector negativeExtinction() {
        return new StarSelector(stars.stream()
                .filter(star -> star.getExtinction().getMax() < 0)
                .collect(Collectors.toList()));
    }

    @NotNull
    public StarSelector luminosityClass(@NotNull final LuminosityClass lumin) {
        return new StarSelector(stars.stream().filter(
                star -> star.getSpectType().getLumin() == lumin
        ).collect(Collectors.toList()));
    }

    @NotNull
    public StarSelector luminosityClasses(@NotNull final LuminosityClass[] lumins) {
        return new StarSelector(stars.stream().filter(
                star -> ArrayUtils.contains(lumins, star.getSpectType().getLumin())
        ).collect(Collectors.toList()));
    }

    @NotNull
    public StarSelector spectType(@NotNull TypeSymbol typeSymbol, final double min, final double max) {
        final List<Star> selection = new ArrayList<>();
        for (final Star star : stars) {
            final SpectClass spect = star.getSpectType().getSpect();
            if (spect.getSymbol() == typeSymbol && min <= spect.getNumber() && spect.getNumber() <= max) {
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
        final int n1 = catalogue.getStars().size();
        final int n2 = new StarSelector(catalogue).luminosityClasses(LuminosityClass.MAIN).getStars().size();
        System.out.println(n1 - n2 + " " + n2);
        System.out.println((double) (n1 - n2) / n1);
    }

//    public static void main(String[] args) {
//        final Catalogue catalogue = Catalogue.HIPPARCOS_2007;
//
//        final List<Star> selection = new StarSelector(catalogue.getStars())
//                .bvColor(1.525, 1.95)
//                .absoluteMagnitude(4.5, 9.5).getStars();
//
//        final List<Spheric> dirs = selection.stream().map(Star::getDir).collect(Collectors.toList());
//
//        System.out.println(selection);
//        final HammerProjection hammerProjection = new HammerProjection(
//                new HealpixCounter(dirs, 18), HammerProjection.Mode.WITH_ERRORS
//        );
//        hammerProjection.setVisible(true);
//    }
}
