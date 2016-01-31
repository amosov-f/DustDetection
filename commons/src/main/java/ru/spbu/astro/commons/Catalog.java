package ru.spbu.astro.commons;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import ru.spbu.astro.commons.spect.SpectType;
import ru.spbu.astro.commons.spect.SpectTypeParser;
import ru.spbu.astro.util.Value;

import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import static ru.spbu.astro.commons.Catalog.Parameter.*;

public final class Catalog {
    private static final Logger LOGGER = Logger.getLogger(Catalog.class.getName());

    @NotNull
    final Map<Integer, Row> id2row = new TreeMap<>();

    Catalog() {
    }

    @NotNull
    public static Catalog read(@NotNull final InputStream in) {
        final Scanner fin = new Scanner(in);
        final List<Parameter<?>> parameters = new ArrayList<>();
        final String[] parts = fin.nextLine().trim().split("\\|");
        for (final String name : Arrays.copyOfRange(parts, 1, parts.length)) {
            parameters.add(valueOf(name.trim()));
        }

        final Catalog catalog = new Catalog();
        while (fin.hasNextLine()) {
            final Row row = Row.parse(fin.nextLine(), parameters);
            if (row != null) {
                catalog.add(row);
            }
        }

        LOGGER.info("Catalog reading completed, #rows = " + catalog.id2row.size());
        return catalog;
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
        final List<Star> stars = new ArrayList<>();
        for (final Row row : id2row.values()) {
            final Star star = row.toStar();
            if (star != null) {
                stars.add(row.toStar());
            }
        }
        LOGGER.info("Getting stars completed, #rows = " + id2row.size() + ", #stars = " + stars.size());
        return stars.toArray(new Star[stars.size()]);
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
        private static Row parse(@NotNull final String row, @NotNull final List<Parameter<?>> parameters) {
            final String[] fields = ArrayUtils.remove(row.split("\\|"), 0);

            final Map<Parameter<?>, Object> values = new LinkedHashMap<>();
            for (int i = 0; i < parameters.size(); i++) {
                final Parameter<?> parameter = parameters.get(i);
                if (parameter != null) {
                    Optional.of(fields[i].trim())
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

        @Nullable
        Star toStar() {
            final Double lii = get(LII);
            final Double bii = get(BII);
            final Double parallax = get(PARALLAX);
            final Double parallaxError = get(PARALLAX_ERROR);
            final Double vMag = get(VMAG);
            final SpectType spectType = get(SPECT_TYPE);
            final Double bvColor = get(BV_COLOR);
            final Double bvColorError = get(BV_COLOR_ERROR);
            final Integer numberComponents = get(NUMBER_COMPONENTS);
            final Double pmRa = get(PM_RA);
            final Double pmRaError = get(PM_RA_ERROR);
            final Double pmDec = get(PM_DEC);
            final Double pmDecError = get(PM_DEC_ERROR);


            boolean invalid = lii == null || bii == null;
            invalid |= parallax == null || parallaxError == null;
            invalid |= vMag == null;
            invalid |= spectType == null;
            invalid |= bvColor == null || bvColorError == null;
            invalid |= numberComponents != null && numberComponents >= 2;
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

        @NotNull
        @Override
        public String toString() {
            return id + " " + values;
        }
    }

    abstract static class Parameter<T> {
        private static final List<Parameter<?>> REGISTRY = new ArrayList<>();

        static final Parameter<Double> LII = new RadiansParameter("lii");
        static final Parameter<Double> BII = new RadiansParameter("bii");
        static final Parameter<Double> PARALLAX_ERROR = new DoubleParameter("parallax_error");
        static final Parameter<Double> VMAG = new DoubleParameter("vmag");
        static final Parameter<Double> BV_COLOR = new DoubleParameter("bv_color");
        static final Parameter<Double> BV_COLOR_ERROR = new DoubleParameter("bv_color_error");
        static final Parameter<Double> PARALLAX = new Parameter.Lambda<>("parallax", value -> {
            final double parallax = Double.parseDouble(value);
            return parallax > 0 ? parallax : null;
        });
        static final Parameter<Integer> NUMBER_COMPONENTS = new IntegerParameter("number_components");
        static final Parameter<SpectType> SPECT_TYPE = new Parameter.Lambda<>("spect_type", SpectTypeParser::parse);
        static final Parameter<Integer> HIP_NUMBER = new IntegerParameter("hip_number");
        static final Parameter<Double> PM_RA = new DoubleParameter("pm_ra");
        static final Parameter<Double> PM_DEC = new DoubleParameter("pm_dec");
        static final Parameter<Double> PM_RA_ERROR = new DoubleParameter("pm_ra_error");
        static final Parameter<Double> PM_DEC_ERROR = new DoubleParameter("pm_dec_error");

        @NotNull
        private final String name;

        private Parameter(@NotNull final String name) {
            this.name = name;
            REGISTRY.add(this);
        }

        @NotNull
        private static List<Parameter<?>> values() {
            return Collections.unmodifiableList(REGISTRY);
        }

        @Nullable
        static Parameter<?> valueOf(@NotNull final String name) {
            for (final Parameter<?> parameter : values()) {
                if (parameter.name.equals(name)) {
                    return parameter;
                }
            }
            return null;
        }

        abstract T parse(@NotNull String value);

        private static class Lambda<T> extends Parameter<T> {
            @NotNull
            private final Function<String, T> parser;

            public Lambda(@NotNull final String name, @NotNull final Function<String, T> parser) {
                super(name);
                this.parser = parser;
            }

            @Nullable
            @Override
            final T parse(@NotNull final String value) {
                try {
                    return parser.apply(value);
                } catch (RuntimeException e) {
                    LOGGER.log(Level.WARNING, "Invalid value: " + value, e.getLocalizedMessage());
                    return null;
                }
            }
        }

        private static class DoubleParameter extends Lambda<Double> {
            private DoubleParameter(@NotNull final String name) {
                super(name, Double::valueOf);
            }
        }

        private static final class RadiansParameter extends Lambda<Double> {
            private RadiansParameter(@NotNull final String name) {
                super(name, value -> Math.toRadians(Double.parseDouble(value)));
            }
        }

        private static final class IntegerParameter extends Lambda<Integer> {
            private IntegerParameter(@NotNull final String name) {
                super(name, Integer::valueOf);
            }
        }
    }
}
