package ru.spbu.astro.commons;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.commons.spect.LuminosityClass;
import ru.spbu.astro.commons.spect.SpectType;
import ru.spbu.astro.commons.spect.SpectTypeParser;
import ru.spbu.astro.commons.spect.TempClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * User: amosov-f
 * Date: 07.02.16
 * Time: 14:33
 */
abstract class Parameter<T> {
    private static final List<Parameter<?>> REGISTRY = new ArrayList<>();

    static final Parameter<Double> LII = new RadiansParameter("lii", "GLon");
    static final Parameter<Double> BII = new RadiansParameter("bii", "GLat");
    static final Parameter<Double> PARALLAX_ERROR = new DoubleParameter("parallax_error", "e_Plx");
    static final Parameter<Double> VMAG = new DoubleParameter("vmag", "Vmag");
    static final Parameter<Double> BV_COLOR = new DoubleParameter("bv_color", "B-V");
    static final Parameter<Double> BV_COLOR_ERROR = new DoubleParameter("bv_color_error", "e_B-V");
    static final Parameter<Double> PARALLAX = new Lambda<>(new String[]{"parallax", "Plx"}, value -> {
        final double parallax = Double.parseDouble(value);
        return parallax > 0 ? parallax : null;
    });
    static final Parameter<Integer> NUMBER_COMPONENTS = new Lambda<>(new String[]{"number_components", "Comp"}, value -> {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return "AB".equals(value) ? 2 : 1;
        }
    });
    static final Parameter<SpectType> SPECT_TYPE = new Lambda<>("spect_type", SpectTypeParser.INSTANCE::parse);
    static final Parameter<Integer> HIP_NUMBER = new IntegerParameter("hip_number", "HIP");
    static final Parameter<Double> PM_RA = new DoubleParameter("pm_ra", "pmRA");
    static final Parameter<Double> PM_DEC = new DoubleParameter("pm_dec", "pmDE");
    static final Parameter<Double> PM_RA_ERROR = new DoubleParameter("pm_ra_error", "e_pmRA");
    static final Parameter<Double> PM_DEC_ERROR = new DoubleParameter("pm_dec_error", "e_pmDE");
    static final Parameter<Double> PM_L = new DoubleParameter("pmGLon");
    static final Parameter<Double> PM_B = new DoubleParameter("pmGLat");
    static final Parameter<TempClass> TEMP_CLASS = new Lambda<>("Tc", value ->
            TempClass.valueOf(Integer.parseInt(value))
    );
    static final Parameter<LuminosityClass> LUMIN_CLASS = new Lambda<>("Lc", value ->
            LuminosityClass.valueOf(Integer.parseInt(value))
    );
    static final Parameter<Double> RV = new DoubleParameter("RV");
    static final Parameter<Double> RV_ERROR = new DoubleParameter("e_RV");

    @NotNull
    private final String[] names;

    private Parameter(@NotNull final String... names) {
        this.names = names;
        REGISTRY.add(this);
    }

    @NotNull
    private static List<Parameter<?>> values() {
        return Collections.unmodifiableList(REGISTRY);
    }

    @Nullable
    static Parameter<?> valueOf(@NotNull final String name) {
        return values().stream()
                .filter(parameter -> ArrayUtils.contains(parameter.names, name))
                .findFirst()
                .orElse(null);
    }

    @Nullable
    abstract T parse(@NotNull String value);

    @Override
    public String toString() {
        return Arrays.toString(names);
    }

    private static class Lambda<T> extends Parameter<T> {
        @NotNull
        private final Function<String, T> parser;

        public Lambda(@NotNull final String name, @NotNull final Function<String, T> parser) {
            super(name);
            this.parser = parser;
        }

        public Lambda(@NotNull final String[] names, @NotNull final Function<String, T> parser) {
            super(names);
            this.parser = parser;
        }

        @Nullable
        @Override
        final T parse(@NotNull final String value) {
            try {
                return parser.apply(value);
            } catch (RuntimeException e) {
//                    LOG.log(Level.WARNING, "Parameter " + this + ", invalid value: " + value);
                return null;
            }
        }
    }

    private static class DoubleParameter extends Lambda<Double> {
        private DoubleParameter(@NotNull final String... names) {
            super(names, Double::valueOf);
        }
    }

    private static final class RadiansParameter extends Lambda<Double> {
        private RadiansParameter(@NotNull final String... names) {
            super(names, value -> Math.toRadians(Double.parseDouble(value)));
        }
    }

    private static final class IntegerParameter extends Lambda<Integer> {
        private IntegerParameter(@NotNull final String... names) {
            super(names, Integer::valueOf);
        }
    }

    private static final class StringParameter extends Lambda<String> {
        private StringParameter(@NotNull final String... names) {
            super(names, Function.identity());
        }
    }
}
