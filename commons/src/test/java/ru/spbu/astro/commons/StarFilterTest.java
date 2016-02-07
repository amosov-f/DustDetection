package ru.spbu.astro.commons;

import org.junit.Test;
import ru.spbu.astro.commons.graph.HRDiagram;
import ru.spbu.astro.commons.spect.LuminosityClass;
import ru.spbu.astro.util.Filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("MagicNumber")
public final class StarFilterTest {
    @Test
    public void testBuiltInFilter() {
        assertEquals(16617, StarFilter.of(Stars.ALL).leftBV().noLumin().stars().length);
        assertEquals(10283, StarFilter.of(Stars.ALL).hasLumin().apply(StarFilter.MAIN_LUMIN.negate()).stars().length);
        assertEquals(3312, StarFilter.of(Stars.ALL).mainLumin().leftBV().lumin(LuminosityClass.III).stars().length);
        assertEquals(52927, StarFilter.of(Stars.ALL).mainLumin().stars().length);
        assertEquals(24896, StarFilter.of(Stars.ALL).lumin(LuminosityClass.III).stars().length);
        assertEquals(28031, StarFilter.of(Stars.ALL).lumin(LuminosityClass.V).stars().length);
        assertEquals(21584, StarFilter.of(Stars.ALL).mainLumin().rightBV().lumin(LuminosityClass.III).stars().length);
        assertEquals(35119, StarFilter.of(Stars.ALL).apply(StarFilter.HAS_LUMIN.negate()).stars().length);
    }

    @Test
    public void testCustomFilter() throws Exception {
        assertTrue(StarFilter.of(Stars.ALL)
                .negExt()
                .apply(Filter.by("ext > 0", star -> star.getExtinction().val() >= 0)).stars().length == 0);
        assertEquals(166, StarFilter.of(Stars.ALL)
                .mainLumin()
                .bvErr(0.01)
                .absMagErr(HRDiagram.SCALE * 0.01)
                .leftBV()
                .lumin(LuminosityClass.III).stars().length);
        assertEquals(3063, StarFilter.of(Stars.ALL)
                .mainLumin()
                .leftBV()
                .apply(Filter.by("III as V", star ->
                        -3.0752 * star.getBVColor().val() + 0.4485 * star.getAbsMag().val() + 1.4793 > 0
                )).lumin(LuminosityClass.III).stars().length);
    }
}