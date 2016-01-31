package ru.spbu.astro.commons.hist;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.commons.spect.LuminosityClass;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * User: amosov-f
 * Date: 12.10.14
 * Time: 19:30
 */
public final class LuminosityClassHist<Y extends Number> extends AbstractStarHist.Lambda<LuminosityClass, Y> {
    public LuminosityClassHist(@NotNull final Function<Stream<Star>, Y> fy) {
        super("Класс светимости", star -> star.getSpectType().getLumin(), fy);
    }
}
