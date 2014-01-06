package ru.spbu.astro.dust.graph;

import org.math.plot.Plot2DPanel;
import org.math.plot.plotObjects.BaseLabel;
import ru.spbu.astro.dust.algo.DustDetector;
import ru.spbu.astro.dust.model.Spheric;
import ru.spbu.astro.dust.model.Star;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class PixPlot extends Plot2DPanel {

    DustDetector dustDetector;

    public PixPlot(DustDetector dustDetector) {
        this.dustDetector = dustDetector;
        plot(new Spheric(0, 0));
    }

    public void plot(final Spheric dir) {
        removeAllPlots();
        removeAllPlotables();

        addLegend("SOUTH");
        setFont(new Font(Font.SERIF, Font.TYPE1_FONT, 15));
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

        List<Star> supportStars = dustDetector.getSupportStars(dir);
        List<Star> missStars = dustDetector.getMissStars(dir);

        if (!supportStars.isEmpty()) {
            double[] x = new double[supportStars.size()];
            double[] y = new double[supportStars.size()];
            for (int i = 0; i < supportStars.size(); ++i) {
                x[i] = supportStars.get(i).getR();
                y[i] = supportStars.get(i).getExt();
            }
            addScatterPlot("Звезды, по которым строится тренд", new Color(17, 18, 255), getX(x, y), getY(x, y));
        }

        if (!missStars.isEmpty()) {
            double[] x = new double[missStars.size()];
            double[] y = new double[missStars.size()];
            for (int i = 0; i < missStars.size(); ++i) {
                x[i] = missStars.get(i).getR();
                y[i] = missStars.get(i).getExt();
            }
            addScatterPlot("Выбросы", new Color(78, 77, 75), getX(x, y), getY(x, y));
        }

        double a = dustDetector.getSlope(dir);
        double b = dustDetector.getIntercept(dir);

        List<Star> stars = supportStars;
        stars.addAll(missStars);
        Collections.sort(stars);

        double[] x = new double[stars.size() + 1];
        double[] y = new double[stars.size() + 1];
        x[0] = 0;
        y[0] = b;
        for (int i = 0; i < stars.size(); ++i) {
            x[i + 1] = stars.get(i).getR();
            y[i + 1] = a * stars.get(i).getR() + b;
        }
        addLinePlot("Тренд", new Color(255, 9, 17), getX(x, y), getY(x, y));
    }

    private double[] getX(double[] x, double[] y) {
        if (x.length == 2) {
            return new double[]{x[0], y[0]};
        }
        return x;
    }

    private double[] getY(double[] x, double[] y) {
        if (x.length == 2) {
            return new double[]{x[1], y[1]};
        }
        return y;
    }
}
