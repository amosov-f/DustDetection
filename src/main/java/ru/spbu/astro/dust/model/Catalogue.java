package ru.spbu.astro.dust.model;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.dust.algo.LuminosityClassifier;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

public final class Catalogue implements Iterable<Catalogue.Row> {

    private final TreeMap<Integer, Row> id2row = new TreeMap<>();

    public Catalogue() {
    }

    public Catalogue(String path) throws FileNotFoundException {
        Scanner fin = new Scanner(new FileInputStream(path));
        String[] titles = fin.nextLine().trim().split("\\|");
        for (int i = 1; i < titles.length; ++i) {
            titles[i] = titles[i].trim();
        }

        while (fin.hasNextLine()) {
            try {
                Row row = new Row(fin.nextLine(), titles);
                id2row.put(row.id, row);
            } catch (Exception ignored) {
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

    public void updateBy(LuminosityClassifier classifier) {
        for (Row row : this) {
            Star s = row.toStar();
            if (s == null) {
                continue;
            }
            String luminosityClass = s.spectralType.getLuminosityClass();
            if (luminosityClass == null) {
                row.title2value.put(SPECT_TYPE, s.spectralType + classifier.getLuminosityClass(s) + ":");
            }
        }
    }

    public void add(Star s) {
        id2row.put(s.id, new Row(s));
    }

    public List<Star> getStars() {
        List<Star> stars = new ArrayList<>();
        for (Row s : id2row.values()) {
            if (s.toStar() != null && s.toStar().spectralType.toBV() != null) {
                stars.add(s.toStar());
            }
        }
        System.out.println("getting stars completed");
        return stars;
    }

    public List<String> getColumn(String s) {
        List<String> column = new ArrayList<>();
        for (Row row : id2row.values()) {
            column.add(row.title2value.get(s));
        }
        return column;
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
        String s = format("id");
        for (Map.Entry<String, String> entry : id2row.firstEntry().getValue().title2value.entrySet()) {
            s += format(entry.getKey());
        }
        s += "\n";
        for (Row row : id2row.values()) {
            s += format(String.valueOf(row.id));
            for (Map.Entry<String, String> entry : row.title2value.entrySet()) {
                s += format(entry.getValue());
            }
            s += "\n";
        }
        return s;
    }

    private static String format(String s) {
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

    public class Row {
        public final int id;
        @NotNull
        private final Map<String, String> title2value = new TreeMap<>();

        private Row(@NotNull final String row, @NotNull final String[] titles) throws EmptyFieldException, NegativeParallaxException, MissingIdException {
            final String[] fields = row.split("\\|");

            int id = -1;
            for (int i = 1; i < titles.length; ++i) {
                fields[i] = fields[i].trim();

                if (fields[i].isEmpty()) {
                    throw new EmptyFieldException();
                }

                if (titles[i].equals(HIP_NUMBER)) {
                    id = new Integer(fields[i]);
                    continue;
                }

                String value;
                switch (titles[i]) {
                    case PARALLAX:
                        if (new Double(fields[i]) <= 0) {
                            throw new NegativeParallaxException();
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

                title2value.put(titles[i], value);
            }

            if (id != -1) {
                this.id = id;
            } else {
                throw new MissingIdException();
            }
        }

        private Row(Star s) {
            id = s.id;
            title2value.put(LII, String.valueOf(rad2deg(s.dir.l)));
            title2value.put(BII, String.valueOf(rad2deg(s.dir.b)));
            title2value.put(PARALLAX, String.valueOf(s.parallax.value));
            title2value.put(PARALLAX_ERROR, String.valueOf(s.parallax.error));
            title2value.put(VMAG, String.valueOf(s.vMag));
            title2value.put(SPECT_TYPE, s.spectralType.toString());
            title2value.put(BV_COLOR, String.valueOf(s.bvColor.value));
            title2value.put(BV_COLOR_ERROR, String.valueOf(s.bvColor.error));
        }

        private void updateBy(Row row) {
            if (id != row.id) {
                return;
            }
            for (String title : row.title2value.keySet()) {
                title2value.put(title, row.title2value.get(title));
            }
        }

        public String get(String s) {
            return title2value.get(s);
        }

        public Star toStar() {
            if (title2value.containsKey(NUMBER_COMPONENTS) && new Integer(get(NUMBER_COMPONENTS)) > 1) {
                return null;
            }

            try {
                return new Star(
                        id,
                        new Spheric(deg2rad(new Double(get(LII))), deg2rad(new Double(get(BII)))),
                        new Value(new Double(get(PARALLAX)), new Double(get(PARALLAX_ERROR))),
                        new Double(get(VMAG)),
                        new SpectralType(get(SPECT_TYPE)),
                        new Value(new Double(get(BV_COLOR)), new Double(get(BV_COLOR_ERROR)))
                );
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public String toString() {
            return id + " " + title2value;
        }
    }

    private class EmptyFieldException extends Exception {
    }

    private class NegativeParallaxException extends Exception {
    }

    private class MissingIdException extends Exception {
    }

    private static final String LII = "lii";
    private static final String BII = "bii";
    private static final String PARALLAX_ERROR = "parallax_error";
    private static final String VMAG = "vmag";
    private static final String BV_COLOR = "bv_color";
    private static final String BV_COLOR_ERROR = "bv_color_error";
    private static final String PARALLAX = "parallax";
    private static final String NUMBER_COMPONENTS = "number_components";
    private static final String SPECT_TYPE = "spect_type";
    private static final String HIP_NUMBER = "hip_number";

    public static void main(String[] args) throws FileNotFoundException {
        Catalogue catalogue = new Catalogue("datasets/hipparcos1997.txt");
        catalogue.updateBy(new Catalogue("datasets/hipparcos2007.txt"));
        catalogue.updateBy(new LuminosityClassifier(catalogue));
    }
}
