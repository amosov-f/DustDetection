package ru.spbu.astro.commons;

import org.junit.Test;
import ru.spbu.astro.commons.graph.HRDiagram;
import ru.spbu.astro.commons.spect.LuminosityClass;
import ru.spbu.astro.util.Filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("MagicNumber")
public class StarFilterTest {
    @Test
    public void test() {
        assertTrue(StarFilter.of(Stars.ALL)
                .negExt()
                .apply(Filter.by("ext > 0", star -> star.getExtinction().getValue() >= 0)).stars().length == 0);
        assertEquals(24810, StarFilter.of(Stars.ALL).leftBV().noLumin().stars().length);
        assertEquals(8058, StarFilter.of(Stars.ALL).hasLumin().apply(StarFilter.MAIN_LUMIN.negate()).stars().length);
        assertEquals(202, StarFilter.of(Stars.ALL)
                .mainLumin()
                .bvErr(0.01)
                .absMagErr(HRDiagram.SCALE * 0.01)
                .leftBV()
                .lumin(LuminosityClass.III).stars().length);
        assertEquals(2455, StarFilter.of(Stars.ALL).mainLumin().leftBV().lumin(LuminosityClass.III).stars().length);
        assertEquals(41227, StarFilter.of(Stars.ALL).mainLumin().stars().length);
        assertEquals(20027, StarFilter.of(Stars.ALL).lumin(LuminosityClass.III).stars().length);
        assertEquals(21200, StarFilter.of(Stars.ALL).lumin(LuminosityClass.V).stars().length);
        assertEquals(2221, StarFilter.of(Stars.ALL)
                .mainLumin()
                .bv(Double.NEGATIVE_INFINITY, 0.6)
                .apply(Filter.by(
                                "III as V",
                                star -> -2.9876 * star.getBVColor().getValue() + 0.4526 * star.getAbsMag().getValue() + 1.3547 > 0)
                ).lumin(LuminosityClass.III).stars().length);
        assertEquals(2455, StarFilter.of(Stars.ALL).mainLumin().leftBV().lumin(LuminosityClass.III).stars().length);
        assertEquals(17572, StarFilter.of(Stars.ALL).mainLumin().rightBV().lumin(LuminosityClass.III).stars().length);
    }
}