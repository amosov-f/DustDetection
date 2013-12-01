package ru.spbu.astro.dust;

public class Star implements Comparable {
    private String name;
    private Spheric dir;
    private double r;
    private double ext;
    
    public Star(String name, Spheric dir, double r, double ext) {
        this.name = name;
        this.dir = dir;
        this.r = r;
        this.ext = ext;
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

    public void setName(String name) {
        this.name = name;
    }

    public Spheric getDir() {
        return dir;
    }

    public void setDir(Spheric dir) {
        this.dir = dir;
    }

    public double getR() {
        return r;
    }

    public void setR(double r) {
        this.r = r;
    }

    public double getExt() {
        return ext;
    }

    public void setExt(double ext) {
        this.ext = ext;
    }

    @Override
    public String toString() {
        return String.format(
                "(%s: l = %.3f, b = %.3f, r = %.3f, ext = %.3f)",
                this.name, this.dir.getL(), this.dir.getB(), this.r, this.ext
        );
    }
}
