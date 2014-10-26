package ru.spbu.astro.dust.model.table;

import org.jetbrains.annotations.NotNull;
import ru.spbu.astro.dust.model.SpectralType;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import static ru.spbu.astro.dust.model.SpectralType.LuminosityClass.*;

/**
 * User: amosov-f
 * Date: 26.10.14
 * Time: 14:24
 */
public class IIIM2SpectTableCombinator implements SpectTableCombinator {
    private static final double CODE = SpectTable.code("M2");

    @NotNull
    @Override
    public SpectTable combine(@NotNull final SpectTable... tables) {
        final SpectTable base = new SpectTable(tables[0].getName() + "+" + tables[1].getName());
        for (final SpectralType.LuminosityClass lumin : tables[0].table.keySet()) {
            base.table.put(lumin, new ArrayList<>());
            for (final Point2D.Double p : tables[0].table.get(lumin)) {
                if (!lumin.equals(III) || p.x < CODE) {
                    base.table.get(lumin).add(p);
                }
            }
        }
        tables[1].table.get(III).stream().filter(p -> p.x >= CODE).forEach(p -> base.table.get(III).add(p));
        return base;
    }
}
