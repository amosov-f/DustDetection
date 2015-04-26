package ru.spbu.astro.util;

import net.sf.epsgraphics.ColorMode;
import net.sf.epsgraphics.EpsGraphics;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * User: amosov-f
 * Date: 26.04.15
 * Time: 14:53
 */
public final class ImageTools {
    private ImageTools() {
    }

    public static void saveAsPNG(@NotNull final Component c, @NotNull final String path) throws IOException {
        final BufferedImage image = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_ARGB);
        c.paint(image.getGraphics());
        ImageIO.write(image, "png", new File(path));
    }

    public static void saveAsEPS(@NotNull final Component c, @NotNull final String path) throws IOException {
        final EpsGraphics g = new EpsGraphics(path, new FileOutputStream(path), 0, 0, c.getWidth(), c.getHeight(), ColorMode.COLOR_RGB);
        g.scale(0.24, 0.24);
        c.paint(g);
        g.flush();
        g.close();
    }
}
