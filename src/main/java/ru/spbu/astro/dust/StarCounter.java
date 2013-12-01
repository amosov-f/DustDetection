package ru.spbu.astro.dust;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StarCounter {

    protected List<Star> stars = new ArrayList();
    protected double alpha;
    protected Double r;

    public StarCounter(final List<Star> stars, double alpha, Double r) {
        for (Star star : stars) {
            if (star.getR() < 0) {
                continue;
            }
            if (r != null && star.getR() > r) {
                continue;
            }
            this.stars.add(star);
        }
        this.alpha = alpha;
        this.r = r;
    }

    public List<Star> getConeStars(Spheric dir) {
        List<Star> result = new ArrayList();

        for (Star star : stars) {
            double mult = Math.sin(dir.getB()) * Math.sin(star.getDir().getB()) +
                    Math.cos(dir.getB()) * Math.cos(star.getDir().getB()) * Math.cos(star.getDir().getL() - dir.getL());

            if (Math.acos(mult) < alpha) {
                result.add(star);
            }
        }

        Collections.sort(result);

        return result;
    }
}
