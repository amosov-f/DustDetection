package ru.spbu.astro.dust;

import ru.spbu.astro.commons.Star;
import ru.spbu.astro.commons.StarFilter;
import ru.spbu.astro.commons.Stars;
import ru.spbu.astro.dust.algo.classify.LuminosityClassifiers;

/**
 * User: amosov-f
 * Date: 28.03.15
 * Time: 16:16
 */
public enum DustStars {
    ;

    public static final Star[] ALL = StarFilter.of(LuminosityClassifiers.SVM.classify(Stars.ALL)).hasExt().stars();
    public static final Star[] DR30 = StarFilter.of(ALL).piRelErr(0.3).stars();
}
