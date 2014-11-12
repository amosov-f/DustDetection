package ru.spbu.astro.dust.model.spect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * User: amosov-f
 * Date: 13.11.14
 * Time: 1:38
 */
public class SpectClass {
    public static enum TypeSymbol {
        O, B, A, F, G, K, M;

        @Nullable
        public static TypeSymbol parse(final char c) {
            try {
                return valueOf(String.valueOf(c));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    @NotNull
    private final TypeSymbol symbol;
    @NotNull
    private final Number number;

    @Nullable
    public static SpectClass valueOf(@NotNull final String str) {
        final TypeSymbol symbol = TypeSymbol.parse(str.charAt(0));
        assert symbol != null;
        Number number;
        try {
            number = Integer.valueOf(str.substring(1));
        } catch (NumberFormatException e) {
            number = Double.valueOf(str.substring(1));
        }
        return new SpectClass(symbol, number);
    }

    @NotNull
    public static SpectClass valueOf(final int code) {
        return new SpectClass(TypeSymbol.values()[code / 10], code % 10);
    }

    SpectClass(@NotNull TypeSymbol symbol, @NotNull Number number) {
        this.symbol = symbol;
        this.number = number;
    }

    @NotNull
    public TypeSymbol getSymbol() {
        return symbol;
    }

    @NotNull
    public Number getNumber() {
        return number;
    }

    public int getCode() {
        return 10 * Arrays.asList(TypeSymbol.values()).indexOf(getSymbol()) + getNumber().intValue();
    }

    @NotNull
    @Override
    public String toString() {
        return getSymbol() + "" + getNumber();
    }
}
