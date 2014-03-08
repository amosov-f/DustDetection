package ru.spbu.astro.dust.model;

import java.io.*;
import java.util.*;

public class Catalogue implements Iterable<Catalogue.Row> {

    private final Map<Integer, Row> id2row = new HashMap<>();

    public Catalogue() {
    }

    public Catalogue(final String name) throws FileNotFoundException {
        final Scanner fin = new Scanner(new FileInputStream(name));
        final String[] titles = fin.nextLine().trim().split("\\|");
        for (int i = 1; i < titles.length; ++i) {
            titles[i] = titles[i].trim();
        }

        while (fin.hasNextLine()) {
            final String[] fields = fin.nextLine().trim().split("\\|");

            int id = 0;
            final Map<String, String> title2value = new HashMap<>();
            boolean isCorrect = true;
            for (int i = 1; i < titles.length; ++i) {
                fields[i] = fields[i].trim();

                if (fields[i].isEmpty()) {
                    isCorrect = false;
                    break;
                }
                if (titles[i].equals("hip_number")) {
                    id = new Integer(fields[i]);
                    continue;
                }
                if (titles[i].equals("parallax")) {
                    if (new Double(fields[i]) <= 0) {
                        isCorrect = false;
                        break;
                    }
                }

                final String value;
                switch (titles[i]) {
                    case "number_components":
                        value = fields[i];
                        break;
                    case "spect_type":
                        value = fields[i];
                        break;
                    default:
                        value = fields[i];
                        break;
                }

                title2value.put(titles[i], value);
            }
            if (isCorrect) {
                add(new Row(id, title2value));
            }
        }
        System.out.println("reading completed");
    }

    private void add(final Row s) {
        id2row.put(s.id, s);
    }

    public Catalogue updateBy(final Catalogue catalogue) {
        final Catalogue mergeCatalogue = new Catalogue();
        for (final Row s : id2row.values()) {
            if (catalogue.id2row.containsKey(s.id)) {
                mergeCatalogue.add(s.updateBy(catalogue.id2row.get(s.id)));
            }
        }
        System.out.println("update completed");
        return mergeCatalogue;
    }

    public List<Star> getStars() {
        final List<Star> stars = new ArrayList<>();
        for (final Row s : id2row.values()) {
            if (s.toStar() != null) {
                stars.add(s.toStar());
            }
        }
        System.out.println("getting stars completed");
        return stars;
    }

    public List<String> getColumn(final String s) {
        final List<String> column = new ArrayList<>();
        for (Row row : id2row.values()) {
            column.add(row.title2value.get(s));
        }
        return column;
    }

    private static double deg2rad(double deg) {
        return deg / 180 * Math.PI;
    }

    @Override
    public String toString() {
        String str = "";
        for (Row s : id2row.values()) {
            System.out.println(s);
        }
        return str;
    }

    @Override
    public Iterator<Row> iterator() {
        return id2row.values().iterator();
    }

    public static class Row {

        public final int id;
        private final Map<String, String> title2value;

        private Row(int id, final Map<String, String> title2value) {
            this.id = id;
            this.title2value = title2value;
        }

        private Row(final Row s) {
            id = s.id;
            title2value = new HashMap<>(s.title2value);
        }

        private Row updateBy(final Row s) {
            if (id != s.id) {
                return null;
            }
            final Row mergeRow = new Row(this);
            for (final String title : s.title2value.keySet())  {
                mergeRow.title2value.put(title, s.title2value.get(title));
            }
            return mergeRow;
        }

        public String get(final String s) {
            return title2value.get(s);
        }

        public Star toStar() {
            if (new Integer(title2value.get("number_components")) > 1) {
                return null;
            }

            double l = deg2rad(new Double(title2value.get("lii")));
            double b = deg2rad(new Double(title2value.get("bii")));
            double parallax = new Double(title2value.get("parallax"));
            double parallaxError = new Double(title2value.get("parallax_error"));
            final Value bvInt;
            try {
                bvInt = new SpectralType(title2value.get("spect_type")).toBV();
            } catch (IllegalArgumentException e) {
                return null;
            }
            if (bvInt == null) {
                return null;
            }
            double bvObs = new Double(title2value.get("bv_color"));
            double bvObsError = new Double(title2value.get("bv_color_error"));

            return new Star(
                    id,
                    new Spheric(l, b),
                    new Value(parallax, parallaxError),
                    new Value(bvObs - bvInt.value, bvObsError + bvInt.error)
            );
        }

        @Override
        public String toString() {
            return id + " " + title2value;
        }

    }
}
