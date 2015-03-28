package ru.spbu.astro.commons;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.commons.spect.LuminosityClass;
import ru.spbu.astro.util.TextUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.logging.Logger;

import static java.lang.String.format;
import static org.apache.commons.lang3.ArrayUtils.contains;

public final class StarFilter {
    private static final Logger LOGGER = Logger.getLogger(StarFilter.class.getName());

    public static final Predicate<Star> NEGATIVE_EXTINCTION = star -> star.getExtinction().getNSigma(3) < 0;
    
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
    public static StarFilter of(@NotNull final Star[] stars) {
        return new StarFilter(stars);
    }

    @NotNull
    public StarFilter filter(@NotNull final String name, @NotNull final Predicate<Star> filter) {
        return new StarFilter(name, Arrays.stream(stars).filter(filter).toArray(Star[]::new), this.history);
    }

    @NotNull
    public StarFilter bvColor(final double min, final double max) {
        return filter(
                format("%.1f < B-V < %.1f]", min, max),
                star -> min <= star.getBVColor().getValue() && star.getBVColor().getValue() <= max
        );
    }

    @NotNull
    public StarFilter absoluteMagnitude(final double min, final double max) {
        return filter(format("%.1f < M < %.1f]", min, max), star -> {
            final double absoluteMagnitude = star.getAbsoluteMagnitude().getValue();
            return min <= absoluteMagnitude && absoluteMagnitude <= max;
        });
    }

    @NotNull
    public StarFilter absoluteMagnitudeError(final double lim) {
        return filter(format("dM < %.2f", lim), star -> star.getAbsoluteMagnitude().getError() < lim);
    }

    @NotNull
    public StarFilter bvColorError(final double lim) {
        return filter(format("dB-V < %.2f", lim), star -> star.getBVColor().getError() < lim);
    }

    @NotNull
    public StarFilter parallaxRelativeError(final double lim) {
        return filter(format("dPi < %.1f", lim), star -> star.getParallax().getRelativeError() < lim);
    }

    @NotNull
    public StarFilter r(final double r1, final double r2) {
        return filter(
                format("%.0f < r < %.0f", r1, r2),
                star -> r1 <= star.getR().getValue() && star.getR().getValue() <= r2
        );
    }

    @NotNull
    public StarFilter hasLuminosityClass() {
        return filter("has lumin", star -> star.getSpectType().hasLumin());
    }

    @NotNull
    public StarFilter hasBVInt() {
        return filter("has B-V_int", star -> star.getSpectType().toBV() != null);
    }

    @NotNull
    public StarFilter negativeExtinction() {
        return filter("ext < 0", NEGATIVE_EXTINCTION);
    }

    @NotNull
    public StarFilter luminosityClass(@NotNull final LuminosityClass lumin) {
        return filter(lumin.toString(), star -> star.getSpectType().getLumin() == lumin);
    }

    @NotNull
    public StarFilter luminosityClasses(@NotNull final LuminosityClass[] lumins) {
        return filter(Arrays.toString(lumins), star -> contains(lumins, star.getSpectType().getLumin()));
    }

    @NotNull
    public StarFilter mainLuminosityClasses() {
        return luminosityClasses(LuminosityClass.MAIN);
    }

    @NotNull
    public StarFilter spectType(final double minCode, final double maxCode) {
        return filter(format("%.0f < spect < %.0f", minCode, maxCode), star -> {
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
