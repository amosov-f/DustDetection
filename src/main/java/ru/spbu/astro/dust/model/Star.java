package ru.spbu.astro.dust.model;

public class Star implements Comparable {
    public final int id;
    public final Spheric dir;
    public final Value parallax;
    public final Value ext;
    
    public Star(int id, final Spheric dir, final Value parallax, final Value ext) {
        this.id = id;
        this.dir = dir;
        this.parallax = parallax;
        this.ext = ext;
    }
    
    @Override
    public int compareTo(Object o) throws ClassCastException {
        if (o == null || getClass() != o.getClass()) {
            throw new ClassCastException();
        }
        Star other = (Star)o;
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

    public double getR() {
        return 1000 / parallax.value;
    }

    public double getRError() {
        return 1000 * parallax.error / Math.pow(parallax.value, 2);
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
                "(%d: l = %.3f, b = %.3f, pi = %.3f, dpi = %.3f, ext = %.3f, extError = %.3f)",
                id, dir.l, dir.b, parallax.value, parallax.error, ext.value, ext.error
        );
    }
}
