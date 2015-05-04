package ru.spbu.astro.commons;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.commons.spect.LuminosityClass;
import ru.spbu.astro.util.Filter;
import ru.spbu.astro.util.TextUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.logging.Logger;

import static java.lang.String.format;
import static org.apache.commons.lang3.ArrayUtils.contains;

public final class StarFilter {
    private static final Logger LOGGER = Logger.getLogger(StarFilter.class.getName());

    public static final Filter<Star> HAS_EXT = Filter.by("has ext", star -> star.getSpectType().toBV() != null);
    public static final Filter<Star> NEG_EXT = Filter.by("ext + 3sigma < 0", HAS_EXT.getPredicate().and(star -> star.getExtinction().plusNSigma(3) < 0));
    public static final Filter<Star> HAS_LUMIN = Filter.by("has lumin", star -> star.getSpectType().hasLumin());
    public static final Filter<Star> MAIN_LUMIN = byLumin(LuminosityClass.MAIN);

    @NotNull
    private final String name;
    @NotNull
    private final Star[] stars;
    @NotNull
    private final StarFilter[] history;

    private StarFilter(@NotNull final Star[] stars) {
        this.name = "filter";
        this.stars = stars;
        this.history = new StarFilter[]{this};
    }

    private StarFilter(@NotNull final String name, @NotNull final Star[] stars, @NotNull final StarFilter[] history) {
        this.name = name;
        this.stars = stars;
        this.history = ArrayUtils.add(history, this);
    }

    @NotNull
    public static Filter<Star> byLumin(@NotNull final LuminosityClass... lumins) {
        return Filter.by(
                Arrays.toString(lumins),
                star -> star.getSpectType().hasOneLumin() && contains(lumins, star.getSpectType().getLumin())
        );
    }

    @NotNull
    public static Filter<Star> byBV(final double minInclusive, final double maxExclusive) {
        return Filter.by(
                format("%.1f <= B-V < %.1f", minInclusive, maxExclusive),
                star -> minInclusive <= star.getBVColor().val() && star.getBVColor().val() < maxExclusive
        );
    }

    @NotNull
    public static StarFilter of(@NotNull final Star[] stars) {
        return new StarFilter(stars);
    }

    @NotNull
    public StarFilter apply(@NotNull final Filter<Star> filter) {
        return apply(filter.getName(), filter.getPredicate());
    }

    @NotNull
    private StarFilter apply(@NotNull final String name, @NotNull final Predicate<Star> filter) {
        return new StarFilter(name, Arrays.stream(stars).filter(filter).toArray(Star[]::new), this.history);
    }

    @NotNull
    public StarFilter bv(final double min, final double max) {
        return apply(byBV(min, max));
    }

    @NotNull
    public StarFilter region(@NotNull final Spheric dir, final double r) {
        return apply(
                "around " + dir + " in " + format(Locale.US, "%.2f", Math.toDegrees(r)),
                star -> dir.distance(star.getDir()) <= r
        );
    }

    @NotNull
    public StarFilter leftBV() {
        return bv(Double.NEGATIVE_INFINITY, 0.6);
    }

    @NotNull
    public StarFilter rightBV() {
        return bv(0.6, Double.POSITIVE_INFINITY);
    }

    @NotNull
    public StarFilter absMag(final double min, final double max) {
        return apply(format("%.1f < M < %.1f]", min, max), star -> {
            final double absoluteMagnitude = star.getAbsMag().val();
            return min <= absoluteMagnitude && absoluteMagnitude <= max;
        });
    }

    @NotNull
    public StarFilter absMagErr(final double lim) {
        return apply(format("dM < %.2f", lim), star -> star.getAbsMag().err() < lim);
    }

    @NotNull
    public StarFilter bvErr(final double lim) {
        return apply(format("dB-V < %.2f", lim), star -> star.getBVColor().err() < lim);
    }

    @NotNull
    public StarFilter piRelErr(final double lim) {
        return apply(format("dPi < %.1f", lim), star -> star.getParallax().relErr() < lim);
    }

    @NotNull
    public StarFilter r(final double r1, final double r2) {
        return apply(
                format("%.0f < r < %.0f", r1, r2),
                star -> r1 <= star.getR().val() && star.getR().val() <= r2
        );
    }

    @NotNull
    public StarFilter r(final double r2) {
        return r(0, r2);
    }

    @NotNull
    public StarFilter hasLumin() {
        return apply(HAS_LUMIN);
    }

    @NotNull
    public StarFilter noLumin() {
        return apply(HAS_LUMIN.negate());
    }

    @NotNull
    public StarFilter hasExt() {
        return apply(HAS_EXT);
    }

    @NotNull
    public StarFilter negExt() {
        return apply(NEG_EXT);
    }

    @NotNull
    public StarFilter lumin(@NotNull final LuminosityClass... lumins) {
        return apply(byLumin(lumins));
    }

    @NotNull
    public StarFilter mainLumin() {
        return apply(MAIN_LUMIN);
    }

    @NotNull
    public StarFilter spectType(final double minCode, final double maxCode) {
        return apply(format("%.0f < spect < %.0f", minCode, maxCode), star -> {
            final double code = star.getSpectType().getSpect().getDoubleCode();
            return minCode <= code && code <= maxCode;
        });
    }

    @NotNull
    public StarFilter orderBy(@NotNull final Comparator<Star> comparator) {
        return new StarFilter(
                comparator.toString(),
                Arrays.stream(stars).sorted(comparator).toArray(Star[]::new),
                this.history
        );
    }

    @NotNull
    public Star[] stars() {
        LOGGER.info(toString());
        return stars;
    }

    @NotNull
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        for (int i = 0; i < history.length; i++) {
            sb.append(history[i].name)
                    .append(": #")
                    .append(history[i].stars.length);
            if (i > 0) {
                sb.append(" (").append(TextUtils.percents(history[i].stars.length, history[i - 1].stars.length));
            }
            if (i > 1) {
                sb.append(";").append(TextUtils.percents(history[i].stars.length, history[0].stars.length));
            }
            if (i > 0) {
                sb.append(")");
            }
            if (i < history.length - 1) {
                sb.append(" | ");
            }
        }
        return sb.toString();
    }
}
