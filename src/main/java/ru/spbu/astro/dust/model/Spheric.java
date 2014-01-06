package ru.spbu.astro.dust.model;

import java.util.Random;

public class Spheric implements Comparable {
    private double l;
    private double b;

    public Spheric(double l, double b) {
        this.l = l;
        this.b = b;
    }

    public Spheric() {
        this(0, 0);
    }

    public static Spheric randomUniform() {
        double x = Math.random() * 2 - 1;
        double y = Math.random() * 2 - 1;
        double z = Math.random() * 2 - 1;
        double r = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));

        if (r > 1) {
            return randomUniform();
        }

        double l = Math.atan2(y, x);
        if (l < 0) {
            l += 2 * Math.PI;
        }
        double b = Math.PI / 2 - Math.acos(z / r);

        return new Spheric(l, b);
    }

    public double getL() {
        return l;
    }

    public void setL(double l) {
        this.l = l;
    }

    public double getB() {
        return b;
    }

    public void setB(double b) {
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
        if (l < other.getL()) {
            return -1;
        }
        if (l > other.getL()) {
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
}
