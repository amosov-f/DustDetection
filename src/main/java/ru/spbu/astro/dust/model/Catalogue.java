package ru.spbu.astro.dust.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.dust.algo.LuminosityClassifier;
import ru.spbu.astro.dust.util.Converter;

import java.util.*;

import static ru.spbu.astro.dust.model.Catalogue.Parameter.*;

public final class Catalogue implements Iterable<Catalogue.Row> {
    public static final Catalogue HIPPARCOS_1997 = new Catalogue("/catalogues/hipparcos1997.txt");
    public static final Catalogue HIPPARCOS_2007 = new Catalogue("/catalogues/hipparcos2007.txt").updateBy(HIPPARCOS_1997);
    public static final Catalogue HIPPARCOS_UPDATED = HIPPARCOS_2007.updateBy(new LuminosityClassifier(HIPPARCOS_2007));

    @NotNull
    private final Map<Integer, Row> id2row = new LinkedHashMap<>();

    public Catalogue() {
    }

    private Catalogue(@NotNull final Catalogue catalogue) {
        this.id2row.putAll(catalogue.id2row);
    }

    Catalogue(@NotNull final String path) {
        final Scanner fin = new Scanner(Catalogue.class.getResourceAsStream(path));
        final List<Parameter> parameters = new ArrayList<>();
        final String[] parts = fin.nextLine().trim().split("\\|");
        for (final String name : Arrays.copyOfRange(parts, 1, parts.length)) {
            parameters.add(Parameter.valueOf(name.trim()));
        }

        while (fin.hasNextLine()) {
            final Row row = Row.parse(fin.nextLine(), parameters);
            if (row != null) {
                put(row);
            }
        }

        System.out.println("reading completed");
    }

    @NotNull
    public Catalogue updateBy(@NotNull final Catalogue catalogue) {
        System.out.println(id2row.size());
        System.out.println(catalogue.id2row.size());
        final Catalogue updatedCatalogue = new Catalogue();
        for (final Row row : this) {
            if (catalogue.id2row.get(row.id) != null) {
                updatedCatalogue.put(row.updateBy(catalogue.id2row.get(row.id)));
            }
        }
        System.out.println("update completed " + updatedCatalogue.id2row.size());
        return updatedCatalogue;
    }

    private void put(@NotNull final Row row) {
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
            final SpectralType.LuminosityClass luminosityClass = star.getSpectralType().getLumin();
            if (luminosityClass == null) {
                updatedRow.values.put(SPECT_TYPE, star.getSpectralType() + classifier.getLuminosityClass(star).name() + ":");
            }
            updatedCatalogue.put(updatedRow);
        }
        return updatedCatalogue;
    }

    public void add(@NotNull final Star s) {
        id2row.put(s.getId(), new Row(s));
    }

    @NotNull
    public List<Star> getStars() {
        final List<Star> stars = new ArrayList<>();
        for (final Row row : id2row.values()) {
            final Star star = row.toStar();
            //System.out.println(star.spectralType.toBV());
            if (star != null && star.getSpectralType().toBV() != null) {
                stars.add(row.toStar());
            }
        }
        System.out.println("getting stars completed");
        return stars;
    }

    @Override
    public String toString() {
        if (id2row.isEmpty()) {
            return "Catalogue is empty";
        }
        final StringBuilder sb = new StringBuilder(format("id"));
        for (final Parameter parameter : id2row.entrySet().iterator().next().getValue().values.keySet()) {
            sb.append(format(parameter.getName()));
        }
        sb.append('\n');
        for (final Row row : id2row.values()) {
            sb.append(format(String.valueOf(row.id)));
            for (final String value : row.values.values()) {
                sb.append(format(value));
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    @NotNull
    private static String format(@NotNull final String s) {
        String format;
        try {
            {
                double value = new Double(s);
                format = String.format("%.2f", value);
            }
            {
                int value = new Integer(s);
                format = String.format("%d", value);
            }
        } catch (NumberFormatException e) {
            format = s;
        }
        if (format.length() > 7) {
            format = format.substring(0, 7);
        }
        if (format.length() < 4) {
            format = format + "\t";
        }
        return format + "\t\t";
    }

    @Override
    public Iterator<Row> iterator() {
        return id2row.values().iterator();
    }

    public static class Row {
        private final int id;
        @NotNull
        private final Map<Parameter, String> values;

        public Row(final int id, @NotNull final Map<Parameter, String> values) {
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

            final Map<Parameter, String> values = new LinkedHashMap<>();

            for (int i = 0; i < parameters.size(); ++i) {
                values.put(parameters.get(i), fields[i].trim());
            }

            final int id = HIP_NUMBER.parse(values.get(HIP_NUMBER));
            return new Row(id, values);
        }

        private Row(@NotNull final Star star) {
            id = star.getId();
            values = new LinkedHashMap<Parameter, String>() {{
                put(LII, String.valueOf(Converter.rad2deg(star.getDir().l)));
                put(BII, String.valueOf(Converter.rad2deg(star.getDir().b)));
                put(PARALLAX, String.valueOf(star.getParallax().getValue()));
                put(PARALLAX_ERROR, String.valueOf(star.getParallax().getError()));
                put(VMAG, String.valueOf(star.getVMag()));
                put(SPECT_TYPE, star.getSpectralType().toString());
                put(BV_COLOR, String.valueOf(star.getBVColor().getValue()));
                put(BV_COLOR_ERROR, String.valueOf(star.getBVColor().getError()));
            }};
        }

        @NotNull
        private Row updateBy(@NotNull final Row row) {
            if (id != row.id) {
                throw new RuntimeException("Id of rows must be equal!");
            }
            final Map<Parameter, String> parameter2value = new LinkedHashMap<>(this.values);
            for (final Parameter parameter : row.values.keySet()) {
                parameter2value.put(parameter, row.values.get(parameter));
            }
            return new Row(id, parameter2value);
        }

        @Nullable
        public <T> T get(@NotNull final Parameter<T> parameter) {
            return values.containsKey(parameter) ? parameter.parse(values.get(parameter)) : null;
        }

        @Nullable
        public Star toStar() {
            final Double lii = get(LII);
            final Double bii = get(BII);
            final Double parallax = get(PARALLAX);
            final Double parallaxError = get(PARALLAX_ERROR);
            final Double vMag = get(VMAG);
            final SpectralType spectralType = get(SPECT_TYPE);
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
            if (spectralType == null) {
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
                    spectralType,
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
                return Converter.deg2rad(Double.parseDouble(value));
            }
        };
        public static final Parameter<Double> BII = new Parameter<Double>("bii") {
            @Nullable
            @Override
            Double parse(@NotNull final String value) {
                return Converter.deg2rad(Double.parseDouble(value));
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
        public static final Parameter<SpectralType> SPECT_TYPE = new Parameter<SpectralType>("spect_type") {
            @Nullable
            @Override
            SpectralType parse(@NotNull final String value) {
                return SpectralType.parse(value);
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
