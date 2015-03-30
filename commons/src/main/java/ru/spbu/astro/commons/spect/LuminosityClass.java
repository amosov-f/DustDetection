package ru.spbu.astro.commons.spect;

import org.apache.commons.lang3.ArrayUtils;

/**
 * User: amosov-f
 * Date: 13.11.14
 * Time: 1:59
 */
@SuppressWarnings("all")
public enum LuminosityClass {
    I, Ia, Ib, Iab, II, IIb, III, IIIa, IIIb, IV, IVa, V, Va, Vb, VI, VII, III_V;

    public static final LuminosityClass[] MAIN = {III, V};

    public final boolean isMain() {
        return ArrayUtils.contains(MAIN, this);
    }
}
