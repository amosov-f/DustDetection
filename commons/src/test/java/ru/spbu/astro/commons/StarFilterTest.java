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
        assertEquals(24892, StarFilter.of(Stars.ALL).leftBVColor().noLumin().stars().length);
        assertEquals(8058, StarFilter.of(Stars.ALL).hasLumin().apply(StarFilter.MAIN_LUMIN.negate()).stars().length);
        assertEquals(202, StarFilter.of(Stars.ALL)
                .mainLumin()
                .bvErr(0.01)
                .absMagErr(HRDiagram.SCALE * 0.01)
                .leftBVColor()
                .lumin(LuminosityClass.III).stars().length);
        assertEquals(2457, StarFilter.of(Stars.ALL)
                .mainLumin()
                .leftBVColor()
                .lumin(LuminosityClass.III).stars().length);
    }
}