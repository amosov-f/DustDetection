package ru.spbu.astro.core.spect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: amosov-f
 * Date: 13.11.14
 * Time: 1:38
 */
public final class SpectClass {
    @NotNull
    private final TypeSymbol symbol;
    @NotNull
    private final Double number;

    SpectClass(@NotNull final TypeSymbol symbol, @NotNull final Double number) {
        this.symbol = symbol;
        this.number = number;
    }

    @Nullable
    public static SpectClass parse(@NotNull final String str) {
        final TypeSymbol symbol = TypeSymbol.parse(str.charAt(0));
        if (symbol == null) {
            return null;
        }
        final Double number;
        try {
            number = Double.valueOf(str.substring(1));
        } catch (NumberFormatException e) {
            return null;
        }
        return new SpectClass(symbol, number);
    }

    @NotNull
    public static SpectClass valueOf(final int code) {
        return new SpectClass(TypeSymbol.values()[code / 10], (double) (code % 10));
    }

    @NotNull
    public TypeSymbol getSymbol() {
        return symbol;
    }

    @NotNull
    public Double getNumber() {
        return number;
    }

    public int getCode() {
        return (int) getDoubleCode();
    }

    public double getDoubleCode() {
        return 10 * symbol.ordinal() + number;
    }

    @NotNull
    @Override
    public String toString() {
        if (number == number.intValue()) {
            return symbol + "" + number.intValue();
        }
        return symbol + "" + number;
    }

    public static enum TypeSymbol {
        O /* 0 */,
        B /* 10 */,
        A /* 20 */,
        F /* 30 */,
        G /* 40 */,
        K /* 50 */,
        M /* 60 */;

        @Nullable
        public static TypeSymbol parse(final char c) {
            try {
                return valueOf(String.valueOf(c));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }
}
