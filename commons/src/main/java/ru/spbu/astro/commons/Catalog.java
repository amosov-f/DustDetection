package ru.spbu.astro.commons;

import com.google.common.base.Splitter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import ru.spbu.astro.commons.spect.SpectType;
import ru.spbu.astro.commons.spect.TempClass;
import ru.spbu.astro.util.Value;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static ru.spbu.astro.commons.Parameter.*;

public final class Catalog {
    private static final Logger LOG = Logger.getLogger(Catalog.class.getName());

    @NotNull
    private final String name;
    @NotNull
    final Map<Integer, Row> id2row = new TreeMap<>();

    Catalog(@NotNull final String name) {
        this.name = name;
    }

    @NotNull
    public static Catalog read(@NotNull final String name, @NotNull final InputStream in) {
        final long startTime = System.currentTimeMillis();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        final Parameter<?>[] parameters;
        try {
            parameters = split(reader.readLine()).map(Parameter::valueOf).toArray(Parameter<?>[]::new);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        final Catalog catalog = new Catalog(name);
        reader.lines().map(row -> Row.parse(row, parameters)).filter(Objects::nonNull).forEach(catalog::add);
        LOG.info(String.format(
                "%s catalog reading completed in %d ms, #rows = %s",
                name.toUpperCase(), System.currentTimeMillis() - startTime, catalog.id2row.size()
        ));
        return catalog;
    }

    void add(@NotNull final Row row) {
        id2row.put(row.id, row);
    }
    
    @TestOnly
    @Nullable
    Star get(final int id) {
        return Optional.ofNullable(id2row.get(id)).map(Row::toStar).orElse(null);
    }

    @NotNull
    Star[] getStars() {
        final long startTime = System.currentTimeMillis();
        final Star[] stars = id2row.values().stream().map(Row::toStar).filter(Objects::nonNull).toArray(Star[]::new);
        LOG.info(String.format(
                "%s stars extracting completed in %d ms, #rows = %d, #stars = %d",
                name.toUpperCase(), System.currentTimeMillis() - startTime, id2row.size(), stars.length
        ));
        return stars;
    }

    @NotNull
    private static Stream<String> split(@NotNull final String row) {
        final Stream<String> parts = Splitter.on('|').trimResults().splitToList(row).stream();
        return row.startsWith("|") ? parts.skip(1) : parts;
    }

    static final class Row {
        private final int id;
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
        private <T> T get(@NotNull final Parameter<T> parameter) {
            return (T) values.get(parameter);
        }

        @NotNull
        private <T> T get(@NotNull final Parameter<T> parameter, @NotNull final T defaultValue) {
            return Optional.ofNullable(get(parameter)).orElse(defaultValue);
        }

        @Nullable
        private Star toStar() {
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
        private SpectType getSpectType() {
            final TempClass temp = get(TEMP_CLASS);
            return temp != null ? SpectType.valueOf(temp, get(LUMIN_CLASS)) : get(SPECT_TYPE);
        }

        @NotNull
        @Override
        public String toString() {
            return id + " " + values;
        }
    }
}
