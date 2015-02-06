package ru.spbu.astro.commons;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.commons.spect.LuminosityClass;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ArrayUtils.contains;

@SuppressWarnings("unused")
public final class StarFilter {
    public static final Predicate<Star> NEGATIVE_EXTINCTION = star -> star.getExtinction().getNSigma(3) < 0;

    @NotNull
    private final List<Star> stars;

    public StarFilter(@NotNull final Catalogue catalogue) {
        this(catalogue.getStars());
    }

    public StarFilter(@NotNull final List<Star> stars) {
        this.stars = stars;
    }

    @NotNull
    public StarFilter filter(@NotNull final Predicate<Star> filter) {
        return new StarFilter(stars.stream().filter(filter).collect(Collectors.toList()));
    }

    @NotNull
    public StarFilter bvColor(final double min, final double max) {
        return filter(star -> min <= star.getBVColor().getValue() && star.getBVColor().getValue() <= max);
    }

    @NotNull
    public StarFilter absoluteMagnitude(final double min, final double max) {
        return filter(star -> {
            final double absoluteMagnitude = star.getAbsoluteMagnitude().getValue();
            return min <= absoluteMagnitude && absoluteMagnitude <= max;
        });
    }

    @NotNull
    public StarFilter absoluteMagnitudeError(final double lim) {
        return filter(star -> star.getAbsoluteMagnitude().getError() < lim);
    }

    @NotNull
    public StarFilter bvColorError(final double lim) {
        return filter(star -> star.getBVColor().getError() < lim);
    }

    @NotNull
    public StarFilter parallaxRelativeError(final double lim) {
        return filter(star -> star.getParallax().getRelativeError() < lim);
    }

    @NotNull
    public StarFilter r(final double r1, final double r2) {
        return filter(star -> r1 <= star.getR().getValue() && star.getR().getValue() <= r2);
    }

    @NotNull
    public StarFilter existLuminosityClass() {
        return filter(star -> star.getSpectType().getLumin() != null);
    }

    @NotNull
    public StarFilter negativeExtinction() {
        return filter(NEGATIVE_EXTINCTION);
    }

    @NotNull
    public StarFilter luminosityClass(@NotNull final LuminosityClass lumin) {
        return filter(star -> star.getSpectType().getLumin() == lumin);
    }

    @NotNull
    public StarFilter luminosityClasses(@NotNull final LuminosityClass[] lumins) {
        return filter(star -> contains(lumins, star.getSpectType().getLumin()));
    }

    @NotNull
    public StarFilter mainLuminosityClasses() {
        return luminosityClasses(LuminosityClass.MAIN);
    }

    @NotNull
    public StarFilter spectType(final double minCode, final double maxCode) {
        return filter(star -> {
            final double code = star.getSpectType().getSpect().getDoubleCode();
            return minCode <= code && code <= maxCode;
        });
    }

    @NotNull
    public StarFilter orderBy(@NotNull final Comparator<Star> comparator) {
        return new StarFilter(stars.stream().sorted(comparator).collect(Collectors.toList()));
    }

    @NotNull
    public List<Star> getStars() {
        return new ArrayList<>(stars);
    }
}
