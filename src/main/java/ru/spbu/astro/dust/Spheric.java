package ru.spbu.astro.dust;

public class Spheric implements Comparable {
    private double l;
    private double b;

    public Spheric(double l, double b) {
        this.l = l;
        this.b = b;
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

    @Override
    public String toString() {
        return "Spheric{" +
                "l = " + l +
                ", b = " + b +
                '}';
    }
}
