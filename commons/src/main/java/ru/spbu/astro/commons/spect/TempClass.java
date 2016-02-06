package ru.spbu.astro.commons.spect;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * User: amosov-f
 * Date: 13.11.14
 * Time: 1:38
 */
public final class TempClass {
    @NotNull
    private final Symbol symbol;
    @NotNull
    private final Double number;

    private TempClass(@NotNull final Symbol symbol, @NotNull final Double number) {
        this.symbol = symbol;
        this.number = number;
    }

    @Nullable
    public static TempClass parse(@NotNull final String str) {
        final Symbol symbol = Symbol.parse(str.charAt(0));
        if (symbol == null) {
            return null;
        }
        try {
            return new TempClass(symbol, Double.valueOf(str.substring(1)));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    @NotNull
    public static TempClass valueOf(@NotNull final String str) {
        return Objects.requireNonNull(parse(str));
    }

    @NotNull
    public static TempClass valueOf(final int code) {
        return new TempClass(Symbol.valueOf(code), (double) (code % 10));
    }

    @NotNull
    public Symbol getSymbol() {
        return symbol;
    }

    @NotNull
    public Double getDoubleNumber() {
        return number;
    }

    public boolean hasIntCode() {
        return number == number.intValue();
    }

    public int getNumber() {
        return number.intValue();
    }

    public int getCode() {
        return (int) getDoubleCode();
    }

    public double getDoubleCode() {
        return symbol.code() + number;
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final TempClass that = (TempClass) o;
        return new EqualsBuilder().append(symbol, that.symbol).append(number, that.number).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(symbol).append(number).toHashCode();
    }

    @NotNull
    @Override
    public String toString() {
        if (number == number.intValue()) {
            return symbol + "" + number.intValue();
        }
        return symbol + "" + number;
    }

    @SuppressWarnings("all")
    public enum Symbol {
        O /* 10 */,
        B /* 20 */,
        A /* 30 */,
        F /* 40 */,
        G /* 50 */,
        K /* 60 */,
        M /* 70 */;

        public int code() {
            return 10 * (ordinal() + 1);
        }

        @Nullable
        public static Symbol parse(final char c) {
            try {
                return valueOf(String.valueOf(c));
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }

        @NotNull
        public static Symbol valueOf(final int code) {
            return Symbol.values()[code / 10 - 1];
        }
    }
}
