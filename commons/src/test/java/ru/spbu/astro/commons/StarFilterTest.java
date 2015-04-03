package ru.spbu.astro.commons;

import org.junit.Test;
import ru.spbu.astro.commons.graph.HRDiagram;
import ru.spbu.astro.commons.spect.LuminosityClass;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("MagicNumber")
public class StarFilterTest {
    @Test
    public void test() {
        assertTrue(StarFilter.of(Stars.ALL)
                .hasBVInt()
                .negativeExtinction()
                .filter("ext > 0", star -> star.getExtinction().getValue() >= 0).stars().length == 0);
        assertEquals(24892, StarFilter.of(Stars.ALL).leftBVColor().noLumin().stars().length);
        assertEquals(8058, StarFilter.of(Stars.ALL)
                .hasLuminosityClass()
                .filter("no " + Arrays.toString(LuminosityClass.MAIN), star -> !star.getSpectType().getLumin().isMain())
                .stars().length);
        assertEquals(202, StarFilter.of(Stars.ALL)
                .mainLuminosityClasses()
                .bvColorError(0.01)
                .absoluteMagnitudeError(HRDiagram.SCALE * 0.01)
                .leftBVColor()
                .luminosityClass(LuminosityClass.III).stars().length);
        assertEquals(2457, StarFilter.of(Stars.ALL)
                .mainLuminosityClasses()
                .leftBVColor()
                .luminosityClass(LuminosityClass.III).stars().length);
    }
}