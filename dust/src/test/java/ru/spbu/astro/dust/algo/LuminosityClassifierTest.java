package ru.spbu.astro.dust.algo;

import org.junit.Before;
import org.junit.Test;
import ru.spbu.astro.commons.Catalogs;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.commons.spect.LuminosityClass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@SuppressWarnings("MagicNumber")
public class LuminosityClassifierTest {
    private LuminosityClassifier classifier;

    @Before
    public void setUp() {
        classifier = new LuminosityClassifier(Catalogs.HIPPARCOS_2007.getStars());
    }

    @Test
    public void test() {
        final Star star = Catalogs.HIPPARCOS_2007.get(50765);
        assertNotNull(star);
        assertNull(star.getSpectType().getLumin());
        assertEquals(LuminosityClass.V, classifier.classify(star));
    }
}