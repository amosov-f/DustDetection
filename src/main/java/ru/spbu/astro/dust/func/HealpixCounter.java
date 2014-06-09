package ru.spbu.astro.dust.func;

import ru.spbu.astro.dust.graph.HammerProjection;
import ru.spbu.astro.dust.model.Catalogue;
import ru.spbu.astro.dust.model.Spheric;
import ru.spbu.astro.dust.model.Star;
import ru.spbu.astro.dust.model.Value;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public final class HealpixCounter extends HealpixDistribution {

    public HealpixCounter(Iterable<Spheric> dirs, int nSide) {
        super(nSide);

        for (int i = 0; i < values.length; ++i) {
            values[i] = new Value(0, 0);
        }

        for (Spheric dir : dirs) {
            int pix = getPix(dir);
            values[pix] = values[pix].add(new Value(1, 0));
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        Catalogue hipparcos = new Catalogue("datasets/hipparcos1997.txt");

        hipparcos.updateBy(new Catalogue("datasets/hipparcos2007.txt"));

        List<Spheric> dirs = new ArrayList<>();
        for (Star s : hipparcos.getStars()) {
            dirs.add(s.dir);
        }

        new HammerProjection(new HealpixCounter(dirs, 18));
    }

}
