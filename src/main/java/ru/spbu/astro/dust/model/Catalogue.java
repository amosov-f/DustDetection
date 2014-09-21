package ru.spbu.astro.dust.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.dust.algo.LuminosityClassifier;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

import static ru.spbu.astro.dust.model.Catalogue.Parameter.*;

public final class Catalogue implements Iterable<Catalogue.Row> {
    @NotNull
    private final Map<Integer, Row> id2row = new LinkedHashMap<>();

    public Catalogue() {
    }

    public Catalogue(@NotNull final String path) {
        final Scanner fin = new Scanner(Catalogue.class.getResourceAsStream(path));
        final List<Parameter> parameters = new ArrayList<>();
        final String[] parts = fin.nextLine().trim().split("\\|");
        for (final String name : Arrays.copyOfRange(parts, 1, parts.length)) {
            parameters.add(Parameter.fromName(name.trim()));
        }

        while (fin.hasNextLine()) {
            final Row row = Row.parse(fin.nextLine(), parameters);
            if (row != null) {
                id2row.put(row.id, row);
            }
        }

        System.out.println("reading completed");
    }

    public void updateBy(Catalogue catalogue) {
        id2row.keySet().retainAll(catalogue.id2row.keySet());
        for (Row row : this) {
            row.updateBy(catalogue.id2row.get(row.id));
        }
        System.out.println("update completed");
    }

    public void updateBy(@NotNull final LuminosityClassifier classifier) {
        for (final Row row : this) {
            final Star star = row.toStar();
            if (star == null) {
                continue;
            }
            final SpectralType.LuminosityClass luminosityClass = star.spectralType.getLuminosityClass();
            if (luminosityClass == null) {
                row.parameter2value.put(SPECT_TYPE, star.spectralType + classifier.getLuminosityClass(star).name() + ":");
            }
        }
    }

    public void add(@NotNull final Star s) {
        id2row.put(s.id, new Row(s));
    }

    @NotNull
    public List<Star> getStars() {
        final List<Star> stars = new ArrayList<>();
        for (final Row row : id2row.values()) {
            final Star star = row.toStar();
            //System.out.println(star.spectralType.toBV());
            if (star != null && star.spectralType.toBV() != null) {
                stars.add(row.toStar());
            }
        }
        System.out.println("getting stars completed");
        return stars;
    }

    @NotNull
    public List<String> getColumn(@NotNull final Parameter parameter) {
        return id2row.values().stream().map(row -> row.get(parameter)).collect(Collectors.toList());
    }

    private static double deg2rad(double deg) {
        return deg / 180 * Math.PI;
    }

    private static double rad2deg(double rad) {
        return rad / Math.PI * 180;
    }

    @Override
    public String toString() {
        if (id2row.isEmpty()) {
            return "Catalogue is empty";
        }
        final StringBuilder sb = new StringBuilder(format("id"));
        for (final Parameter parameter : id2row.entrySet().iterator().next().getValue().parameter2value.keySet()) {
            sb.append(format(parameter.getName()));
        }
        sb.append('\n');
        for (final Row row : id2row.values()) {
            sb.append(format(String.valueOf(row.id)));
            for (final String value : row.parameter2value.values()) {
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
        private final EnumMap<Parameter, String> parameter2value = new EnumMap<>(Parameter.class);

        public Row(final int id, @NotNull final EnumMap<Parameter, String> parameter2value) {
            this.id = id;
            this.parameter2value.putAll(parameter2value);
        }

        @Nullable
        public static Row parse(@NotNull final String row, @NotNull final List<Parameter> parameters) {
            String[] fields = row.split("\\|");
            fields = Arrays.copyOfRange(fields, 1, fields.length);

            final EnumMap<Parameter, String> parameter2value = new EnumMap<>(Parameter.class);

            int id = -1;
            for (int i = 0; i < parameters.size(); ++i) {
                fields[i] = fields[i].trim();

                if (fields[i].isEmpty()) {
                    return null;
                }

                if (parameters.get(i).equals(HIP_NUMBER)) {
                    id = new Integer(fields[i]);
                    continue;
                }

                String value;
                switch (parameters.get(i)) {
                    case PARALLAX:
                        if (new Double(fields[i]) <= 0) {
                            return null;
                        }
                    case NUMBER_COMPONENTS:
                        value = fields[i];
                        break;
                    case SPECT_TYPE:
                        value = fields[i];
                        break;
                    default:
                        value = fields[i];
                        break;
                }

                parameter2value.put(parameters.get(i), value);
            }

            if (id == -1) {
                return null;
            }
            return new Row(id, parameter2value);
        }

        private Row(Star s) {
            id = s.id;
            parameter2value.put(LII, String.valueOf(rad2deg(s.dir.l)));
            parameter2value.put(BII, String.valueOf(rad2deg(s.dir.b)));
            parameter2value.put(PARALLAX, String.valueOf(s.parallax.value));
            parameter2value.put(PARALLAX_ERROR, String.valueOf(s.parallax.error));
            parameter2value.put(VMAG, String.valueOf(s.vMag));
            parameter2value.put(SPECT_TYPE, s.spectralType.toString());
            parameter2value.put(BV_COLOR, String.valueOf(s.bvColor.value));
            parameter2value.put(BV_COLOR_ERROR, String.valueOf(s.bvColor.error));
        }

        private void updateBy(@NotNull final Row row) {
            if (id != row.id) {
                return;
            }
            for (final Parameter parameter : row.parameter2value.keySet()) {
                parameter2value.put(parameter, row.parameter2value.get(parameter));
            }
        }

        public String get(@NotNull final Parameter parameter) {
            return parameter2value.get(parameter);
        }

        @Nullable
        public Star toStar() {
            if (parameter2value.containsKey(NUMBER_COMPONENTS) && new Integer(get(NUMBER_COMPONENTS)) > 1) {
                return null;
            }

            try {
                return new Star(
                        id,
                        new Spheric(deg2rad(new Double(get(LII))), deg2rad(new Double(get(BII)))),
                        new Value(new Double(get(PARALLAX)), new Double(get(PARALLAX_ERROR))),
                        new Double(get(VMAG)),
                        SpectralType.parse(get(SPECT_TYPE)),
                        new Value(new Double(get(BV_COLOR)), new Double(get(BV_COLOR_ERROR)))
                );
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public String toString() {
            return id + " " + parameter2value;
        }
    }

    public static enum Parameter {
        LII, BII, PARALLAX_ERROR, VMAG, BV_COLOR, BV_COLOR_ERROR, PARALLAX, NUMBER_COMPONENTS, SPECT_TYPE, HIP_NUMBER;

        public String getName() {
            return name().toLowerCase();
        }

        public static Parameter fromName(@NotNull final String name) {
            //
            try {
                return valueOf(name.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }


    public static void main(String[] args) throws FileNotFoundException {
        Catalogue catalogue = new Catalogue("/catalogues/hipparcos1997.txt");
        catalogue.updateBy(new Catalogue("/catalogues/hipparcos2007.txt"));
        catalogue.updateBy(new LuminosityClassifier(catalogue));
    }
}
