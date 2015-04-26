package ru.spbu.astro.commons.graph;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbu.astro.commons.Spheric;
import ru.spbu.astro.commons.func.SphericDistribution;
import ru.spbu.astro.util.MathTools;
import ru.spbu.astro.util.TextUtils;
import ru.spbu.astro.util.Value;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import static java.lang.Math.*;

public final class HammerProjection extends JWindow {
    private static final Logger LOGGER = Logger.getLogger(HammerProjection.class.getName());

    private static final int PARALLEL_COUNT = 6;
    private static final int MERIDIAN_COUNT = 12;
    private static final int SIZE = 500;
    @NotNull
    private final SphericDistribution distribution;
    @NotNull
    private final Mode mode;
    @NotNull
    private DirectionProcessor processor = dir -> {
    };
    
    @Nullable
    private final Double min;
    @Nullable
    private final Double max;

    private double minValue;
    private double maxValue;
    private double maxError;

    public HammerProjection(@NotNull final SphericDistribution distribution) {
        this(distribution, Mode.DEFAULT);
    }

    public HammerProjection(@NotNull final SphericDistribution distribution, @NotNull final Mode mode) {
        this(distribution, null, null, mode);
    }

    public HammerProjection(@NotNull final SphericDistribution distribution, @Nullable final Double min, @Nullable final Double max) {
        this(distribution, min, max, Mode.DEFAULT);
    }

    public HammerProjection(@NotNull final SphericDistribution distribution, @Nullable final Double min, @Nullable final Double max, @NotNull final Mode mode) {
        this.distribution = distribution;
        this.min = min;
        this.max = max;
        this.mode = mode;
        setSize(2 * SIZE, SIZE);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(@NotNull final MouseEvent e) {
                final Spheric dir = toSpheric(fromWindow(getMousePosition()));
                if (dir != null) {
                    LOGGER.info(dir.toString());
                    processor.process(dir);
                }
            }
        });
        setBackground(Color.WHITE);
    }
    
    @NotNull
    public static Point2D.Double toPlane(@NotNull final Spheric dir) {
        double l = dir.getL();
        final double b = dir.getB();

        if (l > PI) {
            l -= 2 * PI;
        }

        final double denominator = sqrt(1 + cos(b) * cos(l / 2));

        final double x = -2 * cos(b) * sin(l / 2) / denominator;
        final double y = sin(b) / denominator;

        return new Point2D.Double(x, y);
    }

    @Nullable
    public static Spheric toSpheric(@NotNull final Point2D.Double p) {
        double x = p.getX();
        double y = p.getY();

        if (1 - pow(x / 2, 2) - pow(y, 2) < 0) {
            return null;
        }

        x *= sqrt(2);
        y *= sqrt(2);

        final double z = sqrt(1 - pow(x / 4, 2) - pow(y / 2, 2));

        double l = 2 * atan2(z * x, (2 * (2 * pow(z, 2) - 1)));
        final double b = asin(z * y);

        l = -l;
        if (l < 0) {
            l += 2 * PI;
        }

        return new Spheric(l, b);
    }

    @Override
    public void paint(@NotNull final Graphics g) {
        final Value[][] f = new Value[min(getWidth(), 2 * getHeight())][min(getHeight(), getWidth() / 2)];

        final List<Double> values = new ArrayList<>();
        final List<Double> errors = new ArrayList<>();

        for (int x = 0; x < f.length; ++x) {
            for (int y = 0; y < f[x].length; ++y) {
                final Spheric dir = toSpheric(fromWindow(new Point(x, y)));
                if (dir == null) {
                    continue;
                }

                f[x][y] = distribution.get(dir);

                if (f[x][y] != null) {
                    if (!Double.isNaN(f[x][y].val()) && !Double.isInfinite(f[x][y].val())) {
                        values.add(f[x][y].val());
                    }
                    if (!Double.isNaN(f[x][y].err()) && !Double.isInfinite(f[x][y].err())) {
                        errors.add(f[x][y].err());
                    }
                }
            }
        }

        minValue = Collections.min(values);
        if (min != null) {
            minValue = min;
        }
        maxValue = Collections.max(values);
        if (max != null) {
            maxValue = max;
        }
        maxError = Collections.max(errors);

        LOGGER.info("min value = " + minValue);
        LOGGER.info("max value = " + maxValue);
        LOGGER.info("max error = " + maxError);

        for (int x = 0; x < f.length; ++x) {
            for (int y = 0; y < f[x].length; ++y) {
                final Spheric dir = toSpheric(fromWindow(new Point(x, y)));
                if (dir == null) {
                    continue;
                }
                g.setColor(color(f[x][y]));
                g.drawLine(x, y, x, y);
            }
        }

        paintCircles(g);
        paintRange(g);
    }

    public void setProcessor(@NotNull final DirectionProcessor processor) {
        this.processor = processor;
    }

    @SuppressWarnings("MagicNumber")
    private void paintCircles(@NotNull final Graphics g) {
        g.setFont(new Font("TimesRoman", Font.PLAIN, 24));
        g.setColor(new Color(117, 148, 187));
        for (int i = 0; i < MERIDIAN_COUNT; i++) {
            final double l = 2 * PI / MERIDIAN_COUNT * i;
            for (double b = -PI / 2; b < PI / 2; b += 0.001) {
                final Point p = toWindow(toPlane(new Spheric(l, b)));
                g.drawLine(p.x, p.y, p.x, p.y);
            }
            final Point p = toWindow(toPlane(new Spheric(l, 0)));
            g.drawString(round(toDegrees(l)) + "°", p.x + 2, p.y - 2);
        }
        for (double b = -PI / 2; b < PI / 2; b += 0.001) {
            final Point p = toWindow(toPlane(new Spheric(PI + 0.000001, b)));
            g.drawLine(p.x, p.y, p.x, p.y);
        }
        for (int i = 0; i < PARALLEL_COUNT; i++) {
            final double b = PI * (1.0 * i / PARALLEL_COUNT - 0.5);
            for (double l = 0; l < 2 * PI; l += 0.0001) {
                final Point p = toWindow(toPlane(new Spheric(l, b)));
                g.drawLine(p.x, p.y, p.x, p.y);
            }
            final Point p = toWindow(toPlane(new Spheric(PI + 0.000001, b)));
            if (b < 0) {
                p.translate(0, g.getFontMetrics().getHeight() - 2);
            } else {
                p.translate(0, -2);
            }
            g.drawString(round(toDegrees(b)) + "°", p.x, p.y);
        }
    }

    @SuppressWarnings("MagicNumber")
    private void paintRange(@NotNull final Graphics g) {
        final String minText = TextUtils.format("%.4f", minValue);
        final Font font = new Font("TimesRoman", Font.BOLD, 25);
        final FontMetrics metrics = g.getFontMetrics(font);
        final int x1 = metrics.stringWidth(minText) / 2;
        final int x2 = 215;
        final int y1 = getHeight() - 15;
        final int y2 = getHeight();
        for (int x = x1; x <= x2; ++x) {
            final Color color = color(Value.of(MathTools.interpolate(x1, minValue, x2, maxValue, x)));
            g.setColor(color);
            for (int y = y1; y <= y2; ++y) {
                drawPoint(g, x, y);
            }
        }
        g.setColor(Color.BLACK);
        g.setFont(font);
        g.drawString(minText, 0, y1 - 2);
        final String zeroText = "0";
        final int zeroX = (int) MathTools.interpolate(minValue, x1, maxValue, x2, 0) - metrics.stringWidth(zeroText) / 2;
        if (metrics.stringWidth(minText) < zeroX) {
            g.drawString(zeroText, zeroX, y1 - 2);
        }
        final String maxText = TextUtils.format("%.4f", maxValue);
        g.drawString(maxText, x2 - metrics.stringWidth(maxText) / 2, y1 - 2);
    }

    @NotNull
    private Color color(@Nullable final Value value) {
        if (value == null) {
            return Color.BLACK;
        }
        final double val = MathTools.shrink(value.val(), minValue, maxValue);
        final double err;
        if (mode == Mode.WITH_ERRORS) {
            err = MathTools.shrink(value.err(), 0, maxError);
        } else {
            err = 0;
        }

        if (val >= 0) {
            return Color.getHSBColor(0, (float) val, (float) (1.0 - err));
        }
        return Color.getHSBColor(240f / 360, (float) abs(val), (float) (1.0 - err));
    }

    private void drawPoint(@NotNull final Graphics g, final int x, final int y) {
        g.drawLine(x, y, x, y);
    }

    @NotNull
    public Point toWindow(@NotNull final Point2D.Double p) {
        double x = p.getX();
        double y = p.getY();

        x = (x / 2 + 1) * getHeight();
        y = (1 - y) * getHeight() / 2;

        return new Point((int) x, (int) y);
    }

    @NotNull
    public Point2D.Double fromWindow(@NotNull final Point p) {
        final double x = 2 * (p.getX() / getHeight() - 1);
        final double y = 1 - 2 * p.getY() / getHeight();

        return new Point2D.Double(x, y);
    }

    public enum Mode {
        DEFAULT, WITH_ERRORS
    }

    public interface DirectionProcessor {
        void process(@NotNull Spheric dir);
    }
}