package ru.spbu.astro.dust.spect;

import ru.spbu.astro.commons.spect.SpectTable;

/**
 * User: amosov-f
 * Date: 18.01.15
 * Time: 1:24
 */
public final class DustSpectTables {
    public static final SpectTable COMPOSITE = SpectTable.read("composite", SpectTable.class.getResourceAsStream("/table/min(tsvetkov,max-5%).txt"));
    public static final SpectTable MAX_5 =  SpectTable.read("max-5%", SpectTable.class.getResourceAsStream("/table/max-5%.txt"));
}
