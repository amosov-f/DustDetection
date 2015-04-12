package ru.spbu.astro.util;

import org.jetbrains.annotations.NotNull;

/**
 * User: amosov-f
 * Date: 12.04.15
 * Time: 19:27
 */
public final class Point {
    @NotNull
    private final Value x;
    @NotNull
    private final Value y;

    public Point(@NotNull final Value x, @NotNull final Value y) {
        this.x = x;
        this.y = y;
    }

    public Point(final double x, final double y) {
        this(Value.of(x), Value.of(y));
    }

    @NotNull
    public Value x() {
        return x;
    }

    @NotNull
    public Value y() {
        return y;
    }

    public interface Weight {
        double value(@NotNull Point p);

        class Sqrt implements Weight {
            @Override
            public double value(@NotNull final Point p) {
                return 1 / Math.sqrt(1 + p.x().err() * p.y().err());
            }
        }

        class Quad implements Weight {
            @Override
            public double value(@NotNull final Point p) {
                return 1 / Math.pow(1 + p.x().err() * p.y().err(), 0.25);
            }
        }
    }
}
