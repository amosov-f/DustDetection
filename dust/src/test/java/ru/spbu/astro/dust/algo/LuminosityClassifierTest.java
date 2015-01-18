package ru.spbu.astro.dust.algo;

import org.junit.Before;
import org.junit.Test;
import ru.spbu.astro.core.Catalogues;
import ru.spbu.astro.core.Star;
import ru.spbu.astro.core.spect.LuminosityClass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class LuminosityClassifierTest {
    private LuminosityClassifier classifier;

    @Before
    public void setUp() {
        classifier = new LuminosityClassifier(Catalogues.HIPPARCOS_2007.getStars());
    }

    @Test
    public void test() {
        final Star star = Catalogues.HIPPARCOS_2007.get(50765);
        assertNotNull(star);
        assertNull(star.getSpectType().getLumin());
        assertEquals(LuminosityClass.V, classifier.classify(star));
    }
}