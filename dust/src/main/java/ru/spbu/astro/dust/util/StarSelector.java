package ru.spbu.astro.dust.util;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.core.Star;
import ru.spbu.astro.core.spect.LuminosityClass;
import ru.spbu.astro.dust.model.Catalogue;
import ru.spbu.astro.dust.model.Catalogues;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
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

    public static void main(String[] args) {
        final Star star = new StarSelector(Catalogues.HIPPARCOS_2007).luminosityClass(LuminosityClass.III).negativeExtinction().parallaxRelativeError(0.1).spectType(0, 50).orderBy((star1, star2) -> new Double(star1.getExtinction().getMax()).compareTo(star2.getExtinction().getMax())).getStars().get(10);
        System.out.println(new StarSelector(Catalogues.HIPPARCOS_UPDATED).getStars().size() + " " + new StarSelector(Catalogues.HIPPARCOS_UPDATED).negativeExtinction().getStars().size());

        System.out.println(star.getId());
        System.out.println(star.getParallax());
        System.out.println(star.getExtinction());
        System.out.println(star.getBVColor());
        System.out.println(star.getSpectType());
        System.out.println(star);
        System.out.println();
    }

    @NotNull
    public StarSelector bvColor(final double min, final double max) {
        return filter(star -> min <= star.getBVColor().getValue() && star.getBVColor().getValue() <= max);
    }

    @NotNull
    public StarSelector absoluteMagnitude(final double min, final double max) {
        return filter(star -> {
            final double absoluteMagnitude = star.getAbsoluteMagnitude().getValue();
            return min <= absoluteMagnitude && absoluteMagnitude <= max;
        });
    }

    @NotNull
    public StarSelector absoluteMagnitudeError(final double lim) {
        return filter(star -> star.getAbsoluteMagnitude().getError() < lim);
    }

    @NotNull
    public StarSelector bvColorError(final double lim) {
        return filter(star -> star.getBVColor().getError() < lim);
    }

    @NotNull
    public StarSelector parallaxRelativeError(final double lim) {
        return filter(star -> star.getParallax().getRelativeError() < lim);
    }

    @NotNull
    public StarSelector r(final double r1, final double r2) {
        return filter(star -> r1 <= star.getR().getValue() && star.getR().getValue() <= r2);
    }

    @NotNull
    public StarSelector existLuminosityClass() {
        return filter(star -> star.getSpectType().getLumin() != null);
    }

    @NotNull
    public StarSelector negativeExtinction() {
        return filter(star -> star.getExtinction().getMax() < 0);
    }

    @NotNull
    public StarSelector luminosityClass(@NotNull final LuminosityClass lumin) {
        return filter(star -> star.getSpectType().getLumin() == lumin);
    }

    @NotNull
    public StarSelector luminosityClasses(@NotNull final LuminosityClass[] lumins) {
        return filter(star -> ArrayUtils.contains(lumins, star.getSpectType().getLumin()));
    }

    @NotNull
    public StarSelector mainLuminosityClasses() {
        return luminosityClasses(LuminosityClass.MAIN);
    }

    @NotNull
    public StarSelector spectType(final double minCode, final double maxCode) {
        return filter(star -> {
            final double code = star.getSpectType().getSpect().getDoubleCode();
            return minCode <= code && code <= maxCode;
        });
    }

    @NotNull
    public StarSelector orderBy(@NotNull final Comparator<Star> comparator) {
        final List<Star> stars = new ArrayList<>(this.stars);
        stars.sort(comparator);
        return new StarSelector(stars);
    }

    @NotNull
    public List<Star> getStars() {
        return new ArrayList<>(stars);
    }

    @NotNull
    private StarSelector filter(@NotNull final Predicate<Star> predicate) {
        return new StarSelector(stars.stream().filter(predicate).collect(Collectors.toList()));
    }

//    public static void main(String[] args) {
//        final Catalogue catalogue = Catalogue.HIPPARCOS_2007;
//        final int n1 = catalogue.getStars().size();
//        final int n2 = new StarSelector(catalogue).luminosityClasses(LuminosityClass.MAIN).getStars().size();
//        System.out.println(n1 - n2 + " " + n2);
//        System.out.println((double) (n1 - n2) / n1);
//    }

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
