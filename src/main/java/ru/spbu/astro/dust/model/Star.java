package ru.spbu.astro.dust.model;

public class Star implements Comparable {
    private String name;
    private Spheric dir;
    private double r;
    private double ext;
    private double extError;
    
    public Star(String name, Spheric dir, double r, double ext, double extError) {
        this.name = name;
        this.dir = dir;
        this.r = r;
        this.ext = ext;
        this.extError = extError;
    }
    
    @Override
    public int compareTo(Object o) throws ClassCastException {
        if (o == null || getClass() != o.getClass()) {
            throw new ClassCastException();
        }
        Star other = (Star)o;
        if (r < other.r) {
            return -1;
        }
        if (r > other.r) {
            return 1;
        }
        return 0;
    }

    public String getName() {
        return name;
    }

    public Spheric getDir() {
        return dir;
    }

    public double getR() {
        return r;
    }

    public double getExt() {
        return ext;
    }

    public double getExtError() {
        return extError;
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

        if (Double.compare(star.extError, extError) != 0) {
            return false;
        }

        if (Double.compare(star.ext, ext) != 0) {
            return false;
        }
        if (Double.compare(star.r, r) != 0) {
            return false;
        }
        if (!dir.equals(star.dir)) {
            return false;
        }
        if (!name.equals(star.name)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return String.format(
                "(%s: l = %.3f, b = %.3f, r = %.3f, ext = %.3f, extError = %.3f)",
                name, dir.l, dir.b, r, ext, extError
        );
    }
}
