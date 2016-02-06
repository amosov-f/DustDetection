package ru.spbu.astro.commons.spect;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

/**
 * User: amosov-f
 * Date: 13.11.14
 * Time: 1:59
 */
public enum LuminosityClass {
    I, Ia, Ib, Iab, II, IIb, III, IIIa, IIIb, IV, IVa, V, Va, Vb, VI, VII, III_V;

    public static final LuminosityClass[] MAIN = {III, V};

    public static final LuminosityClass[] INTEGER = {I, II, III, IV, V, VI};

    public final boolean isMain() {
        return ArrayUtils.contains(MAIN, this);
    }

    @NotNull
    public static LuminosityClass valueOf(final int code) {
        return INTEGER[code - 1];
    }
}
