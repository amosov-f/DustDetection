package ru.spbu.astro.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.core.spect.SpectType;
import ru.spbu.astro.core.spect.SpectTypeParser;
import ru.spbu.astro.util.Value;

import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;

import static ru.spbu.astro.core.Catalogue.Parameter.*;

public final class Catalogue {
    private static final Logger LOGGER = Logger.getLogger(Catalogue.class.getName());

    @NotNull
    final Map<Integer, Row> id2row = new HashMap<>();

    Catalogue() {
    }

    @NotNull
    public static Catalogue read(@NotNull final InputStream in) {
        final Scanner fin = new Scanner(in);
        final List<Parameter<?>> parameters = new ArrayList<>();
        final String[] parts = fin.nextLine().trim().split("\\|");
        for (final String name : Arrays.copyOfRange(parts, 1, parts.length)) {
            parameters.add(valueOf(name.trim()));
        }

        final Catalogue catalogue = new Catalogue();
        while (fin.hasNextLine()) {
            final Row row = Row.parse(fin.nextLine(), parameters);
            if (row != null) {
                catalogue.add(row);
            }
        }

        LOGGER.info("Reading completed");
        return catalogue;
    }

    void add(@NotNull final Row row) {
        id2row.put(row.id, row);
    }

    @NotNull
    public Catalogue updateBy(@NotNull final Function<Star, Star> processor) {
        final Catalogue updatedCatalogue = new Catalogue();
        for (final Row row : id2row.values()) {
            final Star star = row.toStar();
            if (star != null) {
                updatedCatalogue.add(new Row(processor.apply(star)));
            }
        }
        return updatedCatalogue;
    }

    @Nullable
    public Star get(final int id) {
        return id2row.get(id).toStar();
    }

    @NotNull
    public List<Star> getStars() {
        final List<Star> stars = new ArrayList<>();
        for (final Row row : id2row.values()) {
            final Star star = row.toStar();
            if (star != null && star.getSpectType().toBV() != null) {
                stars.add(row.toStar());
            }
        }
        LOGGER.info("Getting stars completed");
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

        Row(@NotNull final Row row) {
            this(row.id, row.values);
        }

        Row(@NotNull final Star star) {
            id = star.getId();
            values.put(LII, star.getDir().getL());
            values.put(BII, star.getDir().getB());
            values.put(PARALLAX, star.getParallax().getValue());
            values.put(PARALLAX_ERROR, star.getParallax().getError());
            values.put(VMAG, star.getVMag());
            values.put(SPECT_TYPE, star.getSpectType());
            values.put(BV_COLOR, star.getBVColor().getValue());
            values.put(BV_COLOR_ERROR, star.getBVColor().getError());
            values.put(NUMBER_COMPONENTS, 1);
        }

        @Nullable
        private static Row parse(@NotNull final String row, @NotNull final List<Parameter<?>> parameters) {
            String[] fields = row.split("\\|");
            fields = Arrays.copyOfRange(fields, 1, fields.length);

            final Map<Parameter<?>, Object> values = new LinkedHashMap<>();
            for (int i = 0; i < parameters.size(); i++) {
                final Object value = parameters.get(i).parse(fields[i].trim());
                if (value != null) {
                    values.put(parameters.get(i), value);
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
        public Star toStar() {
            final Double lii = get(LII);
            final Double bii = get(BII);
            final Double parallax = get(PARALLAX);
            final Double parallaxError = get(PARALLAX_ERROR);
            final Double vMag = get(VMAG);
            final SpectType spectType = get(SPECT_TYPE);
            final Double bvColor = get(BV_COLOR);
            final Double bvColorError = get(BV_COLOR_ERROR);
            final Integer numberComponents = get(NUMBER_COMPONENTS);

            if (lii == null) {
                return null;
            }
            if (bii == null) {
                return null;
            }
            if (parallax == null) {
                return null;
            }
            if (parallaxError == null) {
                return null;
            }
            if (vMag == null) {
                return null;
            }
            if (spectType == null) {
                return null;
            }
            if (bvColor == null) {
                return null;
            }
            if (bvColorError == null) {
                return null;
            }
            if (numberComponents != null && numberComponents >= 2) {
                return null;
            }

            return new Star(id,
                    new Spheric(lii, bii),
                    new Value(parallax, parallaxError),
                    vMag,
                    spectType,
                    new Value(bvColor, bvColorError)
            );
        }

        @NotNull
        @Override
        public String toString() {
            return id + " " + values;
        }
    }

    public abstract static class Parameter<T> {
        public static final Parameter<Double> LII = new RadiansParameter("lii");
        public static final Parameter<Double> BII = new RadiansParameter("bii");
        public static final Parameter<Double> PARALLAX_ERROR = new DoubleParameter("parallax_error");
        public static final Parameter<Double> VMAG = new DoubleParameter("vmag");
        public static final Parameter<Double> BV_COLOR = new DoubleParameter("bv_color");
        public static final Parameter<Double> BV_COLOR_ERROR = new DoubleParameter("bv_color_error");
        public static final Parameter<Double> PARALLAX = new DoubleParameter("parallax") {
            @Nullable
            @Override
            Double parse(@NotNull final String value) {
                final Double parallax = super.parse(value);
                return parallax != null && parallax > 0 ? parallax : null;
            }
        };
        public static final Parameter<Integer> NUMBER_COMPONENTS = new IntegerParameter("number_components");
        public static final Parameter<SpectType> SPECT_TYPE = new Parameter<SpectType>("spect_type") {
            @Nullable
            @Override
            SpectType parse(@NotNull final String value) {
                return SpectTypeParser.parse(value);
            }
        };
        public static final Parameter<Integer> HIP_NUMBER = new IntegerParameter("hip_number");

        @NotNull
        private final String name;

        private Parameter(@NotNull final String name) {
            this.name = name;
        }

        @NotNull
        public static Parameter<?>[] values() {
            return new Parameter[]{
                    LII, BII, PARALLAX_ERROR, VMAG,
                    BV_COLOR, BV_COLOR_ERROR, PARALLAX,
                    NUMBER_COMPONENTS, SPECT_TYPE, HIP_NUMBER
            };
        }

        @Nullable
        public static Parameter<?> valueOf(@NotNull final String name) {
            for (final Parameter<?> parameter : values()) {
                if (name.equals(parameter.getName())) {
                    return parameter;
                }
            }
            return null;
        }

        @NotNull
        public final String getName() {
            return name;
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
