package ru.spbu.astro.dust.model.spect;

import java.util.Arrays;
import java.util.List;

/**
* User: amosov-f
* Date: 13.11.14
* Time: 1:59
*/
public enum LuminosityClass {
    I, Ia, Ib, Iab, II, IIb, III, IIIa, IIIb, IV, IVa, V, Va, Vb, VI, VII;

    public static boolean containsSymbol(final char c) {
        for (final LuminosityClass luminosityClass : LuminosityClass.values()) {
            if (luminosityClass.name().contains(String.valueOf(c))) {
                return true;
            }
        }
        return false;
    }
}
