package ru.spbu.astro.core.spect;

/**
 * User: amosov-f
 * Date: 13.11.14
 * Time: 1:59
 */
public enum LuminosityClass {
    I, Ia, Ib, Iab, II, IIb, IIIa, IIIb, IV, IVa, V, III, Va, Vb, VI, VII;

    public static final LuminosityClass[] MAIN = {III, V};

    public static boolean contains(final char c) {
        for (final LuminosityClass luminosityClass : values()) {
            if (luminosityClass.name().contains(String.valueOf(c))) {
                return true;
            }
        }
        return false;
    }
}
