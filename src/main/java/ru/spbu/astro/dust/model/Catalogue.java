package ru.spbu.astro.dust.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.dust.algo.LuminosityClassifier;
import ru.spbu.astro.dust.model.spect.LuminosityClass;
import ru.spbu.astro.dust.model.spect.SpectType;

import java.io.InputStream;
import java.util.*;

import static ru.spbu.astro.dust.model.Catalogue.Parameter.*;

public final class Catalogue implements Iterable<Catalogue.Row> {
    public static final Catalogue HIPPARCOS_1997 = Catalogue.read(Catalogue.class.getResourceAsStream("/catalogues/hipparcos1997.txt"));
    public static final Catalogue HIPPARCOS_2007 = Catalogue.read(Catalogue.class.getResourceAsStream("/catalogues/hipparcos2007.txt")).updateBy(HIPPARCOS_1997);
    public static final Catalogue HIPPARCOS_UPDATED = HIPPARCOS_2007.updateBy(new LuminosityClassifier(HIPPARCOS_2007));

    @NotNull
    private final Map<Integer, Row> id2row = new HashMap<>();

    private Catalogue() {
    }

    private Catalogue(@NotNull final Catalogue catalogue) {
        this.id2row.putAll(catalogue.id2row);
    }

    @NotNull
    public static Catalogue read(@NotNull final InputStream in) {
        final Scanner fin = new Scanner(in);
        final List<Parameter> parameters = new ArrayList<>();
        final String[] parts = fin.nextLine().trim().split("\\|");
        for (final String name : Arrays.copyOfRange(parts, 1, parts.length)) {
            parameters.add(Parameter.valueOf(name.trim()));
        }

        final Catalogue catalogue = new Catalogue();
        while (fin.hasNextLine()) {
            final Row row = Row.parse(fin.nextLine(), parameters);
            if (row != null) {
                catalogue.add(row);
            }
        }

        System.out.println("reading completed");
        return catalogue;
    }

    @NotNull
    public Catalogue updateBy(@NotNull final Catalogue catalogue) {
        System.out.println(id2row.size());
        System.out.println(catalogue.id2row.size());
        final Catalogue updatedCatalogue = new Catalogue();
        for (final Row row : this) {
            if (catalogue.id2row.get(row.id) != null) {
                updatedCatalogue.add(row.updateBy(catalogue.id2row.get(row.id)));
            }
        }
        System.out.println("update completed " + updatedCatalogue.id2row.size());
        return updatedCatalogue;
    }

    private void add(@NotNull final Row row) {
        id2row.put(row.id, row);
    }

    @NotNull
    public Catalogue updateBy(@NotNull final LuminosityClassifier classifier) {
        final Catalogue updatedCatalogue = new Catalogue(this);
        for (final Row row : updatedCatalogue) {
            final Star star = row.toStar();
            if (star == null) {
                continue;
            }
            final Row updatedRow = new Row(row);
            final LuminosityClass lumin = star.getSpectType().getLumin();
            if (lumin == null) {
                star.getSpectType().setLumin(classifier.classify(star));
                updatedRow.values.put(SPECT_TYPE, star.getSpectType());
            }
            updatedCatalogue.add(updatedRow);
        }
        return updatedCatalogue;
    }

    public void add(@NotNull final Star s) {
        id2row.put(s.getId(), new Row(s));
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
        System.out.println("getting stars completed");
        return stars;
    }

    @Override
    public Iterator<Row> iterator() {
        return id2row.values().iterator();
    }

    public static class Row {
        private final int id;
        @NotNull
        private final Map<Parameter, Object> values;

        public Row(final int id, @NotNull final Map<Parameter, Object> values) {
            this.id = id;
            this.values = new LinkedHashMap<>(values);
        }

        private Row(@NotNull final Row row) {
            this(row.id, row.values);
        }

        @Nullable
        public static Row parse(@NotNull final String row, @NotNull final List<Parameter> parameters) {
            String[] fields = row.split("\\|");
            fields = Arrays.copyOfRange(fields, 1, fields.length);

            final Map<Parameter, Object> values = new LinkedHashMap<>();

            for (int i = 0; i < parameters.size(); ++i) {
                if (!fields[i].trim().isEmpty()) {
                    values.put(parameters.get(i), parameters.get(i).parse(fields[i].trim()));
                }
            }

            final int id = (int) values.get(HIP_NUMBER);
            return new Row(id, values);
        }

        private Row(@NotNull final Star star) {
            id = star.getId();
            values = new LinkedHashMap<Parameter, Object>() {{
                put(LII, Math.toDegrees(star.getDir().getL()));
                put(BII, Math.toDegrees(star.getDir().getB()));
                put(PARALLAX, star.getParallax().getValue());
                put(PARALLAX_ERROR, star.getParallax().getError());
                put(VMAG, star.getVMag());
                put(SPECT_TYPE, star.getSpectType());
                put(BV_COLOR, star.getBVColor().getValue());
                put(BV_COLOR_ERROR, star.getBVColor().getError());
            }};
        }

        @NotNull
        private Row updateBy(@NotNull final Row row) {
            if (id != row.id) {
                throw new RuntimeException("Id of rows must be equal!");
            }
            final Map<Parameter, Object> parameter2value = new LinkedHashMap<>(this.values);
            for (final Parameter parameter : row.values.keySet()) {
                parameter2value.put(parameter, row.values.get(parameter));
            }
            return new Row(id, parameter2value);
        }

        @Nullable
        @SuppressWarnings("unchecked")
        public <T> T get(@NotNull final Parameter<T> parameter) {
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

            assert lii != null;
            assert bii != null;
            if (parallax == null) {
                return null;
            }
            if (parallaxError == null) {
                return null;
            }
            assert vMag != null;
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

        @Override
        public String toString() {
            return id + " " + values;
        }
    }

    public static abstract class Parameter<T> {
        public static final Parameter<Double> LII = new Parameter<Double>("lii") {
            @Nullable
            @Override
            Double parse(@NotNull final String value) {
                return Math.toRadians(Double.parseDouble(value));
            }
        };
        public static final Parameter<Double> BII = new Parameter<Double>("bii") {
            @Nullable
            @Override
            Double parse(@NotNull final String value) {
                return Math.toRadians(Double.parseDouble(value));
            }
        };
        public static final Parameter<Double> PARALLAX_ERROR = new Parameter<Double>("parallax_error") {
            @Nullable
            @Override
            Double parse(@NotNull final String value) {
                return !value.isEmpty() ? Double.valueOf(value) : null;
            }
        };
        public static final Parameter<Double> VMAG = new Parameter<Double>("vmag") {
            @NotNull
            @Override
            Double parse(@NotNull final String value) {
                return Double.valueOf(value);
            }
        };
        public static final Parameter<Double> BV_COLOR = new Parameter<Double>("bv_color") {
            @Nullable
            @Override
            Double parse(@NotNull final String value) {
                return !value.isEmpty() ? Double.valueOf(value) : null;
            }
        };
        public static final Parameter<Double> BV_COLOR_ERROR = new Parameter<Double>("bv_color_error") {
            @Nullable
            @Override
            Double parse(@NotNull final String value) {
                return !value.isEmpty() ? Double.valueOf(value) : null;
            }
        };
        public static final Parameter<Double> PARALLAX = new Parameter<Double>("parallax") {
            @Nullable
            @Override
            Double parse(@NotNull final String value) {
                if (value.isEmpty()) {
                    return null;
                }
                final Double parallax = Double.valueOf(value);
                if (parallax < 0) {
                    return null;
                }
                return parallax;
            }
        };
        public static final Parameter<Integer> NUMBER_COMPONENTS = new Parameter<Integer>("number_components") {
            @NotNull
            @Override
            Integer parse(@NotNull final String value) {
                return Integer.valueOf(value);
            }
        };
        public static final Parameter<SpectType> SPECT_TYPE = new Parameter<SpectType>("spect_type") {
            @Nullable
            @Override
            SpectType parse(@NotNull final String value) {
                return SpectType.parse(value);
            }
        };
        public static final Parameter<Integer> HIP_NUMBER = new Parameter<Integer>("hip_number") {
            @NotNull
            @Override
            Integer parse(@NotNull final String value) {
                return Integer.valueOf(value);
            }
        };

        @NotNull
        private final String name;

        private Parameter(@NotNull final String name) {
            this.name = name;
        }

        @NotNull
        public String getName() {
            return name;
        }

        abstract T parse(@NotNull final String value);

        @NotNull
        public static Parameter[] values() {
            return new Parameter[]{
                    LII, BII, PARALLAX_ERROR, VMAG, BV_COLOR, BV_COLOR_ERROR, PARALLAX,
                    NUMBER_COMPONENTS, SPECT_TYPE, HIP_NUMBER
            };
        }

        @Nullable
        public static Parameter valueOf(@NotNull final String name) {
            for (final Parameter parameter : values()) {
                if (name.equals(parameter.getName())) {
                    return parameter;
                }
            }
            return null;
        }

    }
}
