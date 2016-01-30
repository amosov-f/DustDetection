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
                    final Object value = parameter.parse(fields[i].trim());
                    if (value != null) {
                        values.put(parameter, value);
                    }
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

            boolean invalid = lii == null || bii == null;
            invalid |= parallax == null || parallaxError == null;
            invalid |= vMag == null;
            invalid |= spectType == null;
            invalid |= bvColor == null || bvColorError == null;
            invalid |= numberComponents != null && numberComponents >= 2;
            if (invalid) {
                return null;
            }

            return new Star.Builder(id)
                    .setDir(new Spheric(lii, bii))
                    .setParallax(Value.of(parallax, parallaxError))
                    .setVMag(vMag)
                    .setSpectType(spectType)
                    .setBVColor(Value.of(bvColor, bvColorError)).build();
        }

        @NotNull
        @Override
        public String toString() {
            return id + " " + values;
        }
    }

    abstract static class Parameter<T> {
        static final Parameter<Double> LII = new RadiansParameter("lii");
        static final Parameter<Double> BII = new RadiansParameter("bii");
        static final Parameter<Double> PARALLAX_ERROR = new DoubleParameter("parallax_error");
        static final Parameter<Double> VMAG = new DoubleParameter("vmag");
        static final Parameter<Double> BV_COLOR = new DoubleParameter("bv_color");
        static final Parameter<Double> BV_COLOR_ERROR = new DoubleParameter("bv_color_error");
        static final Parameter<Double> PARALLAX = new DoubleParameter("parallax") {
            @Nullable
            @Override
            Double parse(@NotNull final String value) {
                final Double parallax = super.parse(value);
                return parallax != null && parallax > 0 ? parallax : null;
            }
        };
        static final Parameter<Integer> NUMBER_COMPONENTS = new IntegerParameter("number_components");
        static final Parameter<SpectType> SPECT_TYPE = new Parameter<SpectType>("spect_type") {
            @Nullable
            @Override
            SpectType parse(@NotNull final String value) {
                return SpectTypeParser.parse(value);
            }
        };
        static final Parameter<Integer> HIP_NUMBER = new IntegerParameter("hip_number");

        @NotNull
        private final String name;

        private Parameter(@NotNull final String name) {
            this.name = name;
        }

        @NotNull
        private static Parameter<?>[] values() {
            return new Parameter[]{
                    LII, BII, PARALLAX_ERROR, VMAG,
                    BV_COLOR, BV_COLOR_ERROR, PARALLAX,
                    NUMBER_COMPONENTS, SPECT_TYPE, HIP_NUMBER
            };
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

        private static class DoubleParameter extends Parameter<Double> {
            private DoubleParameter(@NotNull final String name) {
                super(name);
            }

            @Nullable
            @Override
            Double parse(@NotNull final String value) {
                try {
                    return Double.valueOf(value);
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }

        private static final class RadiansParameter extends DoubleParameter {
            private RadiansParameter(@NotNull final String name) {
                super(name);
            }

            @Nullable
            @Override
            Double parse(@NotNull final String value) {
                final Double deg = super.parse(value);
                return deg != null ? Math.toRadians(deg) : null;
            }
        }

        private static final class IntegerParameter extends Parameter<Integer> {
            private IntegerParameter(@NotNull final String name) {
                super(name);
            }

            @Nullable
            @Override
            Integer parse(@NotNull final String value) {
                try {
                    return Integer.valueOf(value);
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
    }
}
