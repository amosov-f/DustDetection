package ru.spbu.astro.commons;

import com.google.common.base.Splitter;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import ru.spbu.astro.commons.spect.LuminosityClass;
import ru.spbu.astro.commons.spect.SpectType;
import ru.spbu.astro.commons.spect.SpectTypeParser;
import ru.spbu.astro.commons.spect.TempClass;
import ru.spbu.astro.util.Value;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static ru.spbu.astro.commons.Catalog.Parameter.*;

public final class Catalog {
    private static final Logger LOG = Logger.getLogger(Catalog.class.getName());

    @NotNull
    final Map<Integer, Row> id2row = new TreeMap<>();

    Catalog() {
    }

    @NotNull
    public static Catalog read(@NotNull final InputStream in) {
        final long startTime = System.currentTimeMillis();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        final Parameter<?>[] parameters;
        try {
            parameters = split(reader.readLine()).map(Parameter::valueOf).toArray(Parameter<?>[]::new);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        final Catalog catalog = new Catalog();
        reader.lines().map(row -> Row.parse(row, parameters)).filter(Objects::nonNull).forEach(catalog::add);
        LOG.info(String.format(
                "Catalog reading completed in %d ms, #rows = %s",
                System.currentTimeMillis() - startTime,
                catalog.id2row.size()
        ));
        return catalog;
    }

    @NotNull
    private static Stream<String> split(@NotNull final String row) {
        final Stream<String> parts = Splitter.on('|').trimResults().splitToList(row).stream();
        return row.startsWith("|") ? parts.skip(1) : parts;
    }

    void add(@NotNull final Row row) {
        id2row.put(row.id, row);
    }
    
    @TestOnly
    @Nullable
    Star get(final int id) {
        final Row row = id2row.get(id);
        return row != null ? row.toStar() : null;
    }

    @NotNull
    Star[] getStars() {
        final Star[] stars = id2row.values().stream().map(Row::toStar).filter(Objects::nonNull).toArray(Star[]::new);
        LOG.info("Getting stars completed, #rows = " + id2row.size() + ", #stars = " + stars.length);
        return stars;
    }


    static final class Row {
        final int id;
        @NotNull
        final Map<Parameter<?>, Object> values = new LinkedHashMap<>();

        Row(final int id, @NotNull final Map<Parameter<?>, Object> values) {
            this.id = id;
            this.values.putAll(values);
        }

        @Nullable
        private static Row parse(@NotNull final String row, @NotNull final Parameter<?>... parameters) {
            final String[] fields = split(row).toArray(String[]::new);
            final Map<Parameter<?>, Object> values = new LinkedHashMap<>();
            for (int i = 0; i < parameters.length; i++) {
                final Parameter<?> parameter = parameters[i];
                if (parameter != null) {
                    Optional.of(fields[i])
                            .filter(field -> !field.isEmpty())
                            .map(parameter::parse)
                            .ifPresent(value -> values.put(parameter, value));
                }
            }

            final int id = (int) values.get(HIP_NUMBER);
            return new Row(id, values);
        }

        @Nullable
        @SuppressWarnings("unchecked")
        <T> T get(@NotNull final Parameter<T> parameter) {
            return (T) values.get(parameter);
        }

        @NotNull
        <T> T get(@NotNull final Parameter<T> parameter, @NotNull final T defaultValue) {
            return Optional.ofNullable(get(parameter)).orElse(defaultValue);
        }

        @Nullable
        Star toStar() {
            final Double lii = get(LII);
            final Double bii = get(BII);
            final Double parallax = get(PARALLAX);
            final Double parallaxError = get(PARALLAX_ERROR);
            final Double vMag = get(VMAG);
            final SpectType spectType = getSpectType();
            final Double bvColor = get(BV_COLOR);
            final Double bvColorError = get(BV_COLOR_ERROR);
            final int numberComponents = get(NUMBER_COMPONENTS, 1);
            final Double pmRa = get(PM_RA);
            final Double pmRaError = get(PM_RA_ERROR);
            final Double pmDec = get(PM_DEC);
            final Double pmDecError = get(PM_DEC_ERROR);


            boolean invalid = lii == null || bii == null;
            invalid |= parallax == null || parallaxError == null;
            invalid |= vMag == null;
            invalid |= spectType == null;
            invalid |= bvColor == null || bvColorError == null;
            invalid |= numberComponents >= 2;
            invalid |= pmRa == null || pmRaError == null || pmDec == null || pmDecError == null;
            if (invalid) {
                return null;
            }

            return new Star.Builder(id)
                    .setDir(new Spheric(lii, bii))
                    .setParallax(Value.of(parallax, parallaxError))
                    .setVMag(vMag)
                    .setSpectType(spectType)
                    .setBVColor(Value.of(bvColor, bvColorError))
                    .setRaProperMotion(Value.of(pmRa, pmRaError))
                    .setDecProperMotion(Value.of(pmDec, pmDecError))
                    .build();
        }

        @Nullable
        SpectType getSpectType() {
            final TempClass temp = get(TEMP_CLASS);
            return temp != null ? SpectType.valueOf(temp, get(LUMIN_CLASS)) : get(SPECT_TYPE);
        }

        @NotNull
        @Override
        public String toString() {
            return id + " " + values;
        }
    }

    abstract static class Parameter<T> {
        private static final List<Parameter<?>> REGISTRY = new ArrayList<>();

        static final Parameter<Double> LII = new RadiansParameter("lii", "GLon");
        static final Parameter<Double> BII = new RadiansParameter("bii", "GLat");
        static final Parameter<Double> PARALLAX_ERROR = new DoubleParameter("parallax_error", "e_Plx");
        static final Parameter<Double> VMAG = new DoubleParameter("vmag", "Vmag");
        static final Parameter<Double> BV_COLOR = new DoubleParameter("bv_color", "B-V");
        static final Parameter<Double> BV_COLOR_ERROR = new DoubleParameter("bv_color_error", "e_B-V");
        static final Parameter<Double> PARALLAX = new Parameter.Lambda<>(new String[]{"parallax", "Plx"}, value -> {
            final double parallax = Double.parseDouble(value);
            return parallax > 0 ? parallax : null;
        });
        static final Parameter<Integer> NUMBER_COMPONENTS = new Parameter.Lambda<>(new String[]{"number_components", "Comp"}, value -> {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return "AB".equals(value) ? 2 : 1;
            }
        });
        static final Parameter<SpectType> SPECT_TYPE = new Parameter.Lambda<>("spect_type", SpectTypeParser.INSTANCE::parse);
        static final Parameter<Integer> HIP_NUMBER = new IntegerParameter("hip_number", "HIP");
        static final Parameter<Double> PM_RA = new DoubleParameter("pm_ra", "pmRA");
        static final Parameter<Double> PM_DEC = new DoubleParameter("pm_dec", "pmDE");
        static final Parameter<Double> PM_RA_ERROR = new DoubleParameter("pm_ra_error", "e_pmRA");
        static final Parameter<Double> PM_DEC_ERROR = new DoubleParameter("pm_dec_error", "e_pmDE");
        static final Parameter<Double> PM_L = new DoubleParameter("pmGLon");
        static final Parameter<Double> PM_B = new DoubleParameter("pmGLat");
        static final Parameter<TempClass> TEMP_CLASS = new Parameter.Lambda<>("Tc", value ->
                TempClass.valueOf(Integer.parseInt(value))
        );
        static final Parameter<LuminosityClass> LUMIN_CLASS = new Parameter.Lambda<>("Lc", value ->
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
                    LOG.log(Level.WARNING, "Parameter " + this + ", invalid value: " + value);
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
}
