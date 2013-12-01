package ru.spbu.astro.dust;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BinaryStarCounter extends StarCounter {

    private List<Double>[] latitudeBands;

    private List<Star>[] starBands;

    public BinaryStarCounter(final List<Star> stars, double alpha, Double r) {
        super(stars, alpha, r);

        starBands = new List[getBand(Math.PI / 2) + 1];
        for (int i = 0; i < starBands.length; ++i) {
            starBands[i] = new ArrayList();
        }

        for (Star star : this.stars) {
            starBands[getBand(star)].add(star);
        }

        for (List band : starBands) {
            Collections.sort(band, new Comparator<Star>() {
                @Override
                public int compare(Star s1, Star s2) {
                    return s1.getDir().compareTo(s2.getDir());
                }
            });
        }

        latitudeBands = new List[starBands.length];
        for (int i = 0; i < latitudeBands.length; ++i) {
            latitudeBands[i] = new ArrayList();

            for (Star star : starBands[i]) {
                latitudeBands[i].add(star.getDir().getL() - 2 * Math.PI);
            }
            for (Star star : starBands[i]) {
                latitudeBands[i].add(star.getDir().getL());
            }
            for (Star star : starBands[i]) {
                latitudeBands[i].add(star.getDir().getL() + 2 * Math.PI);
            }
        }
    }

    @Override
    public List<Star> getConeStars(Spheric dir) {
        int b1 = Math.max(getBand(dir.getB() - alpha), 0);
        int b2 = Math.min(getBand(dir.getB() + alpha) + 1, starBands.length);

        List<Star> candidates = new ArrayList();
        for (int b = b1; b < b2; ++b) {
            double beta = Math.atan2(Math.tan(alpha), Math.cos(dir.getB()));

            int i1 = Math.abs(Collections.binarySearch(latitudeBands[b], dir.getL() - beta) + 1);
            int i2 = Math.abs(Collections.binarySearch(latitudeBands[b], dir.getL() + beta) + 1);

            for (int i = i1; i < i2; ++i) {
                candidates.add(starBands[b].get(i % starBands[b].size()));
            }
        }

        StarCounter counter = new StarCounter(candidates, alpha, r);
        return counter.getConeStars(dir);
    }

    private int getBand(double b) {
        return (int)Math.round(Math.floor((b + Math.PI / 2) / (2 * alpha)));
    }

    private int getBand(Star star) {
        return getBand(star.getDir().getB());
    }
}
