package ru.spbu.astro.dust.algo.classify;

import org.junit.Test;
import ru.spbu.astro.commons.Star;
import ru.spbu.astro.commons.Stars;
import ru.spbu.astro.commons.spect.LuminosityClass;

import java.util.Map;

import static org.junit.Assert.*;

@SuppressWarnings("MagicNumber")
public class LuminosityClassifierTest {
    private static final Map<Integer, Star> STARS = Stars.map(Stars.ALL);

    @Test
    public void testSVM() {
        final Star star = STARS.get(50765);
        assertNotNull(star);
        assertNull(star.getSpectType().getLumin());
        assertEquals(LuminosityClass.V, LuminosityClassifiers.SVM.classify(star));
    }

    @Test
    public void testCombining() throws Exception {
        final Star star = STARS.get(148);
        assertNotNull(star);
        assertNull(star.getSpectType().getLumin());
        assertEquals(LuminosityClass.III_V, LuminosityClassifiers.COMBINING.classify(star));
    }
}