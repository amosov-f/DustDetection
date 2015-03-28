package ru.spbu.astro.dust.algo;

import org.junit.Test;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.commons.Stars;
import ru.spbu.astro.commons.spect.LuminosityClass;
import ru.spbu.astro.dust.algo.classify.LuminosityClassifiers;

import static org.junit.Assert.*;

@SuppressWarnings("MagicNumber")
public class SVMLuminosityClassifierTest {
    @Test
    public void test() {
        final Star star = Stars.MAP_ALL.get(50765);
        assertNotNull(star);
        assertNull(star.getSpectType().getLumin());
        assertEquals(LuminosityClass.V, LuminosityClassifiers.SVM.classify(star));
    }
}