package ru.spbu.astro.dust;

import ru.spbu.astro.commons.Catalogs;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.commons.StarFilter;
import ru.spbu.astro.dust.algo.FullLuminosityClassifier;

/**
 * User: amosov-f
 * Date: 28.03.15
 * Time: 16:16
 */
public final class Stars {
    private Stars() {
    }
    
    public static final Star[] ALL = Catalogs.HIPPARCOS_2007.getStars();
    public static final Star[] HIPPARCOS_UPDATED = StarFilter.of(new FullLuminosityClassifier(ALL).classify(ALL)).hasBVInt().stars();

    public static void main(String[] args) {
    }
}
