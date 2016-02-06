package ru.spbu.astro.commons.spect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.util.MathTools;
import ru.spbu.astro.util.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SpectType {
    @NotNull
    private final List<TempClass> temps;
    @NotNull
    private final List<LuminosityClass> lumins;
    @NotNull
    private final Relation spectsRelation;
    @NotNull
    private final Relation luminsRelation;

    SpectType(@NotNull final List<TempClass> temps,
              @NotNull final Relation spectsRelation,
              @NotNull final List<LuminosityClass> lumins,
              @NotNull final Relation luminsRelation)
    {
        this.temps = temps;
        this.spectsRelation = spectsRelation;
        this.lumins = lumins;
        this.luminsRelation = luminsRelation;
    }

    @NotNull
    public static SpectType valueOf(@NotNull final TempClass temp, @Nullable final LuminosityClass lumin) {
        return new SpectType(
                Collections.singletonList(temp),
                Relation.INTERMEDIATE,
                lumin != null ? Collections.singletonList(lumin) : Collections.emptyList(),
                Relation.INTERMEDIATE
        );
    }

    @NotNull
    @Override
    public String toString() {
        String s = "";
        for (int i = 0; i < temps.size(); ++i) {
            s += temps.get(i);
            if (i < temps.size() - 1) {
                s += spectsRelation.getSymbol();
            }
        }
        for (int i = 0; i < lumins.size(); ++i) {
            s += lumins.get(i);
            if (i < lumins.size() - 1) {
                s += luminsRelation.getSymbol();
            }
        }
        return s;
    }

    @Nullable
    public Value toBV() {
        final List<Value> bvs = new ArrayList<>();
        for (final TempClass spect : temps) {
            for (final LuminosityClass lumin : lumins) {
                final Value bv = SpectTable.getInstance().getBV(spect, lumin);
                if (bv != null) {
                    bvs.add(bv);
                }
            }
        }
        if (bvs.isEmpty()) {
            return null;
        }
        return MathTools.average(bvs.toArray(new Value[bvs.size()]));
    }

    @Nullable
    public LuminosityClass getLumin() {
        for (final LuminosityClass mainLumin : LuminosityClass.MAIN) {
            if (lumins.contains(mainLumin)) {
                return mainLumin;
            }
        }
        return hasLumin() ? lumins.get(0) : null;
    }

    public boolean hasLumin() {
        return !lumins.isEmpty();
    }

    public boolean hasOneLumin() {
        return lumins.size() == 1;
    }

    public SpectType setLumin(@NotNull final LuminosityClass lumin) {
        if (hasLumin()) {
            throw new RuntimeException("Already has luminosity class!");
        }
        return new SpectType(temps, spectsRelation, Collections.singletonList(lumin), luminsRelation);
    }

    @NotNull
    public TempClass getTemp() {
        return TempClass.valueOf((int) temps.stream().mapToInt(TempClass::getCode).average().getAsDouble());
    }

    enum Relation {
        OR('/'), INTERMEDIATE('-');

        private final char symbol;

        Relation(final char symbol) {
            this.symbol = symbol;
        }

        @Nullable
        public static Relation parse(final char c) {
            for (final Relation relation : values()) {
                if (relation.getSymbol() == c) {
                    return relation;
                }
            }
            return null;
        }

        public char getSymbol() {
            return symbol;
        }
    }
}