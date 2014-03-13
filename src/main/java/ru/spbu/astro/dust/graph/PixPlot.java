package ru.spbu.astro.dust.graph;

import org.math.plot.Plot2DPanel;
import org.math.plot.plotObjects.BaseLabel;
import org.math.plot.plotObjects.Line;
import ru.spbu.astro.dust.algo.DustDetector;
import ru.spbu.astro.dust.model.Spheric;
import ru.spbu.astro.dust.model.Star;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PixPlot extends Plot2DPanel {

    private final DustDetector dustDetector;

    public PixPlot(final DustDetector dustDetector) {
        this.dustDetector = dustDetector;
        plot(new Spheric(0, 0));
    }

    public void plot(final Spheric dir) {
        removeAllPlots();
        removeAllPlotables();

        addLegend("SOUTH");
        setFont(new Font(Font.SERIF, Font.BOLD, 15));
        setAxisLabels("r [пк]", "E(r)\n[зв. вел.]");

        BaseLabel title = new BaseLabel(
                "Покраснение в направлении " + dir,
                Color.BLACK,
                0.5,
                1.1
        );
        title.setFont(new Font("Courier", Font.BOLD, 20));
        addPlotable(title);


        getAxis(0).setLabelFont(new Font(Font.SERIF, Font.ITALIC, 25));
        getAxis(0).setLabelPosition(0.5, -0.10);
        getAxis(0).setLightLabelFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 15));

        getAxis(1).setLabelFont(new Font(Font.SERIF, Font.ITALIC, 25));
        //getAxis(1).setLabelAngle(-Math.PI / 2);
        getAxis(1).setLabelPosition(-0.12, 0.5);
        getAxis(1).setLightLabelFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 15));

        if (dir == null) {
            return;
        }

        final List<Star> supportStars = dustDetector.getSupportStars(dir);
        final List<Star> missStars = dustDetector.getMissStars(dir);

        if (!supportStars.isEmpty()) {
            double[] x = new double[supportStars.size()];
            double[] y = new double[supportStars.size()];
            for (int i = 0; i < supportStars.size(); ++i) {
                x[i] = supportStars.get(i).getR().value;
                y[i] = supportStars.get(i).getExtinction().value;
                plotErrs(supportStars.get(i));
            }
            addScatterPlot("Звезды, по которым строится тренд", new Color(17, 18, 255), getX(x, y), getY(x, y));
        }

        if (!missStars.isEmpty()) {
            double[] x = new double[missStars.size()];
            double[] y = new double[missStars.size()];
            for (int i = 0; i < missStars.size(); ++i) {
                x[i] = missStars.get(i).getR().value;
                y[i] = missStars.get(i).getExtinction().value;
                plotErrs(missStars.get(i));
            }
            addScatterPlot("Выбросы", Color.BLACK, getX(x, y), getY(x, y)); //new Color(78, 77, 75)
        }

        double a = dustDetector.getSlope(dir).value;
        double b = dustDetector.getIntercept(dir).value;

        final List<Star> stars = new ArrayList<>(supportStars);
        stars.addAll(missStars);
        Collections.sort(stars);

        final double[] x = new double[stars.size() + 1];
        final double[] y = new double[stars.size() + 1];
        x[0] = 0;
        y[0] = b;
        for (int i = 0; i < stars.size(); ++i) {
            x[i + 1] = stars.get(i).getR().value;
            y[i + 1] = a * stars.get(i).getR().value + b;
        }
        addLinePlot("Тренд", new Color(255, 9, 17), getX(x, y), getY(x, y));


    }

    static double[] getX(double[] x, double[] y) {
        if (x.length == 2) {
            return new double[]{x[0], y[0]};
        }
        return x;
    }

    static double[] getY(double[] x, double[] y) {
        if (x.length == 2) {
            return new double[]{x[1], y[1]};
        }
        return y;
    }

    private void plotErrs(final Star star) {
        double r = star.getR().value;
        double dr = star.getR().error;
        double ext = star.getExtinction().value;
        double dExt = star.getExtinction().error;
        final Color color = new Color(150, 150, 150);
        addPlotable(new Line(color, new double[]{r - dr, ext}, new double[]{r + dr, ext}));
        addPlotable(new Line(color, new double[]{r, ext - dExt}, new double[]{r, ext + dExt}));
    }
}
