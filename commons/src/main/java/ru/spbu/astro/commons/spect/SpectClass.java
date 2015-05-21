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
public final class SpectClass {
    @NotNull
    private final TypeSymbol symbol;
    @NotNull
    private final Double number;

    private SpectClass(@NotNull final TypeSymbol symbol, @NotNull final Double number) {
        this.symbol = symbol;
        this.number = number;
    }

    @Nullable
    public static SpectClass parse(@NotNull final String str) {
        final TypeSymbol symbol = TypeSymbol.parse(str.charAt(0));
        if (symbol == null) {
            return null;
        }
        try {
            return new SpectClass(symbol, Double.valueOf(str.substring(1)));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    @NotNull
    public static SpectClass valueOf(@NotNull final String str) {
        return Objects.requireNonNull(parse(str));
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
        return 10 * symbol.ordinal() + number;
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final SpectClass that = (SpectClass) o;
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
    public enum TypeSymbol {
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
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }
    }
}
