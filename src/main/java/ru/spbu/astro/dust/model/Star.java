package ru.spbu.astro.dust.model;

public class Star implements Comparable {
    public final int id;
    public final Spheric dir;
    public final Value parallax;
    public final double vMag;
    public final SpectralType spectralType;
    public final Value bvColor;
    
    public Star(int id, final Spheric dir, final Value parallax, double vMag, final SpectralType spectralType, final Value bvColor) {
        this.id = id;
        this.dir = dir;
        this.parallax = parallax;
        this.vMag = vMag;
        this.spectralType = spectralType;
        this.bvColor = bvColor;
    }
    
    @Override
    public int compareTo(Object o) throws ClassCastException {
        if (o == null || getClass() != o.getClass()) {
            throw new ClassCastException();
        }
        Star other = (Star) o;
        if (parallax.value > other.parallax.value) {
            return -1;
        }
        if (parallax.value < other.parallax.value) {
            return 1;
        }
        return 0;
    }

    public int getId() {
        return id;
    }

    public Value getR() {
        return new Value(1000 / parallax.value, 1000 * parallax.error / Math.pow(parallax.value, 2));
    }

    public Value getExtinction() {
        return bvColor.subtract(spectralType.toBV());
    }

    public double getAbsoluteMagnitude() {
        return vMag + 5 * Math.log10(parallax.value) - 10;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Star)) {
            return false;
        }

        Star star = (Star) o;

        return id == star.id;
    }

    @Override
    public String toString() {
        return String.format(
                "(%d: l = %.3f, b = %.3f, pi = %.3f, dpi = %.3f)",
                id, dir.l, dir.b, parallax.value, parallax.error
        );
    }
}
