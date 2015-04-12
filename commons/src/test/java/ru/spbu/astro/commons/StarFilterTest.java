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
                .apply(Filter.by("ext > 0", star -> star.getExtinction().val() >= 0)).stars().length == 0);
        assertEquals(24810, StarFilter.of(Stars.ALL).leftBV().noLumin().stars().length);
        assertEquals(9478, StarFilter.of(Stars.ALL).hasLumin().apply(StarFilter.MAIN_LUMIN.negate()).stars().length);
        assertEquals(153, StarFilter.of(Stars.ALL)
                .mainLumin()
                .bvErr(0.01)
                .absMagErr(HRDiagram.SCALE * 0.01)
                .leftBV()
                .lumin(LuminosityClass.III).stars().length);
        assertEquals(1947, StarFilter.of(Stars.ALL).mainLumin().leftBV().lumin(LuminosityClass.III).stars().length);
        assertEquals(39807, StarFilter.of(Stars.ALL).mainLumin().stars().length);
        assertEquals(18628, StarFilter.of(Stars.ALL).lumin(LuminosityClass.III).stars().length);
        assertEquals(21179, StarFilter.of(Stars.ALL).lumin(LuminosityClass.V).stars().length);
        assertEquals(1758, StarFilter.of(Stars.ALL)
                .mainLumin()
                .leftBV()
                .apply(Filter.by(
                                "III as V",
                                star -> -3.0752 * star.getBVColor().val() + 0.4485 * star.getAbsMag().val() + 1.4793 > 0)
                ).lumin(LuminosityClass.III).stars().length);
        assertEquals(1947, StarFilter.of(Stars.ALL).mainLumin().leftBV().lumin(LuminosityClass.III).stars().length);
        assertEquals(16681, StarFilter.of(Stars.ALL).mainLumin().rightBV().lumin(LuminosityClass.III).stars().length);
    }
}