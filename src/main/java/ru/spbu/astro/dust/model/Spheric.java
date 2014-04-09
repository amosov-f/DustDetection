package ru.spbu.astro.dust.model;

public class Spheric implements Comparable {

    public final double l;
    public final double b;

    public Spheric(double l, double b) {
        this.l = l;
        this.b = b;
    }

    public double getTheta() {
        return Math.PI / 2 - b;
    }

    public double getPhi() {
        return l;
    }

    @Override
    public int compareTo(Object o) {
        if (o == null || getClass() != o.getClass()) {
            throw new ClassCastException();
        }
        Spheric other = (Spheric)o;
        if (l < other.l) {
            return -1;
        }
        if (l > other.l) {
            return 1;
        }
        return 0;
    }

    private static double rad2deg(double rad) {
        return rad / Math.PI * 180;
    }

    @Override
    public String toString() {
        return String.format("%.2f° г.ш., %.2f° г.д.", rad2deg(b), rad2deg(l));
    }

    public static Spheric valueOf(double[] dir) {
        return new Spheric(dir[1], Math.PI / 2 - dir[0]);
    }
}
