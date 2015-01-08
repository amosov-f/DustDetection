package ru.spbu.astro.core.spect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.util.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SpectType {
    @NotNull
    private final List<SpectClass> spects;
    @NotNull
    private final List<LuminosityClass> lumins;
    @NotNull
    private final Relation spectsRelation;
    @NotNull
    private final Relation luminosRelation;

    SpectType(@NotNull final List<SpectClass> spects,
              @NotNull final Relation spectsRelation,
              @NotNull final List<LuminosityClass> lumins,
              @NotNull final Relation luminosRelation) {
        this.spects = spects;
        this.spectsRelation = spectsRelation;
        this.lumins = lumins;
        this.luminosRelation = luminosRelation;
    }

    @NotNull
    @Override
    public String toString() {
        String s = "";
        for (int i = 0; i < spects.size(); ++i) {
            s += spects.get(i);
            if (i < spects.size() - 1) {
                switch (spectsRelation) {
                    case INTERMEDIATE:
                        s += "-";
                        break;
                    case OR:
                        s += "/";
                        break;
                }
            }
        }
        for (int i = 0; i < lumins.size(); ++i) {
            s += lumins.get(i);
            if (i < lumins.size() - 1) {
                switch (luminosRelation) {
                    case INTERMEDIATE:
                        s += "-";
                        break;
                    case OR:
                        s += "/";
                        break;
                }
            }
        }
        return s;
    }

    @Nullable
    public Value toBV() {
        final List<Double> bvs = new ArrayList<>();
        for (final SpectClass spect : spects) {
            for (final LuminosityClass lumin : lumins) {
                final Double bv = SpectTable.getInstance().getBV(spect, lumin);
                if (bv != null) {
                    bvs.add(bv);
                }
            }
        }
        if (bvs.isEmpty()) {
            return null;
        }
        double bv = 0.0;
        for (final double bvEntry : bvs) {
            bv += bvEntry;
        }
        bv /= bvs.size();
        return new Value(bv, Collections.max(bvs) - bv);
    }

    @Nullable
    public LuminosityClass getLumin() {
        if (lumins.isEmpty()) {
            return null;
        }
        return lumins.get(0);
    }

    public void setLumin(@NotNull final LuminosityClass lumin) {
//        assert lumins.isEmpty();
        lumins.add(lumin);
    }

    @NotNull
    public SpectClass getSpect() {
        int sum = 0;
        int count = 0;
        for (final SpectClass spect : spects) {
            sum += spect.getCode();
            count++;
        }
        return SpectClass.valueOf(sum / count);
    }

    static enum Relation {
        OR, INTERMEDIATE
    }
}