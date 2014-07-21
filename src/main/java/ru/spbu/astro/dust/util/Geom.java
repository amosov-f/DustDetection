package ru.spbu.astro.dust.util;

public class Geom {

    public static double dotProduct(double[] a, double[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Arrays size may be equal!");
        }

        double dotProduct = 0.0;
        for (int i = 0; i < a.length; ++i) {
            dotProduct += a[i] * b[i];
        }

        return dotProduct;
    }

    public static double abs(double[] a) {
        double abs = 0.0;
        for (double coordinate : a) {
            abs += Math.pow(coordinate, 2);
        }
        abs = Math.sqrt(abs);
        return abs;
    }

}
