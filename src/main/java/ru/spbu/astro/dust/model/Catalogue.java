package ru.spbu.astro.dust.model;

import ru.spbu.astro.dust.algo.LuminosityClassifier;

import java.io.*;
import java.util.*;

public class Catalogue implements Iterable<Catalogue.Row> {

    private final String[] titles;

    private final Map<Integer, Row> id2row = new HashMap<>();

    public Catalogue(final String name) throws FileNotFoundException {
        final Scanner fin = new Scanner(new FileInputStream(name));
        titles = fin.nextLine().trim().split("\\|");
        for (int i = 1; i < titles.length; ++i) {
            titles[i] = titles[i].trim();
        }

        while (fin.hasNextLine()) {
            try {
                final Row row = new Row(fin.nextLine());
                id2row.put(row.id, row);
            } catch (Exception ignored) {
            }
        }
        //System.out.println(Arrays.toString(titles));

        System.out.println("reading completed");
    }

    public void updateBy(final Catalogue catalogue) {
        id2row.keySet().retainAll(catalogue.id2row.keySet());
        for (Row row : this) {
            row.updateBy(catalogue.id2row.get(row.id));
        }
        System.out.println("update completed");
    }

    public void updateBy(final LuminosityClassifier classifier) {
        for (Row row : this) {
            final Star s = row.toStar();
            if (s == null) {
                continue;
            }
            final String luminosityClass = s.spectralType.getLuminosityClass();
            //System.out.println(luminosityClass);
            if (luminosityClass == null) {
                //System.out.print(s.spectralType + " -> ");
                row.title2value.put("spect_type", s.spectralType + classifier.getLuminosityClass(s) + ":");
                //System.out.println(row.title2value.get("spect_type"));
            }
        }
    }

    public List<Star> getStars() {
        final List<Star> stars = new ArrayList<>();
        for (final Row s : id2row.values()) {
            if (s.toStar() != null && s.toStar().spectralType.toBV() != null) {
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
            str += s + "\n";
        }
        return str;
    }

    @Override
    public Iterator<Row> iterator() {
        return id2row.values().iterator();
    }

    public class Row {

        public final int id;
        private final Map<String, String> title2value = new HashMap<>();

        private Row(final String row) throws EmptyFieldException, NegativeParallaxException, MissingIdException {
            final String[] fields = row.split("\\|");

            int id = -1;
            for (int i = 1; i < titles.length; ++i) {
                fields[i] = fields[i].trim();

                if (fields[i].isEmpty()) {
                    throw new EmptyFieldException();
                }

                if (titles[i].equals("hip_number")) {
                    id = new Integer(fields[i]);
                    continue;
                }


                final String value;
                switch (titles[i]) {
                    case "parallax":
                        if (new Double(fields[i]) <= 0) {
                            throw new NegativeParallaxException();
                        }
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

            if (id != -1) {
                this.id = id;
            } else {
                throw new MissingIdException();
            }
        }

        private void updateBy(final Row row) {
            if (id != row.id) {
                return;
            }
            //title2value.keySet().addAll(row.title2value.keySet());
            for (final String title : row.title2value.keySet()) {
                title2value.put(title, row.title2value.get(title));
            }
        }

        public String get(final String s) {
            return title2value.get(s);
        }

        public Star toStar() {
            if (title2value.containsKey("number_components") && new Integer(get("number_components")) > 1) {
                return null;
            }

            try {
                return new Star(
                        id,
                        new Spheric(deg2rad(new Double(get("lii"))), deg2rad(new Double(get("bii")))),
                        new Value(new Double(get("parallax")), new Double(get("parallax_error"))),
                        new Double(get("vmag")),
                        new SpectralType(get("spect_type")),
                        new Value(new Double(get("bv_color")), new Double(get("bv_color_error")))
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

    public static void main(final String[] args) throws FileNotFoundException {
        final Catalogue catalogue = new Catalogue("datasets/hipparcos1997.txt");
        catalogue.updateBy(new Catalogue("datasets/hipparcos2007.txt"));
        catalogue.updateBy(new LuminosityClassifier(catalogue));
    }
}
