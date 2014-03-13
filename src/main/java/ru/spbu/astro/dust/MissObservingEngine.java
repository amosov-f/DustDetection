package ru.spbu.astro.dust;

import org.math.plot.FrameView;
import org.math.plot.Plot2DPanel;
import ru.spbu.astro.dust.model.Star;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.List;

@Deprecated
public class MissObservingEngine {

    private static final List<String> types = Arrays.asList("O", "B", "A", "F", "G", "K", "M");

    private static void processSpectTypeHistogram(List<Star> missStars, int prefixLength) throws Exception {
        Map<Integer, CatalogueReadingEngine.Star> id2star = new HashMap<>();
        final Map<String, Integer> spectTypeCount = new HashMap<>();
        for (CatalogueReadingEngine.Star star : CatalogueReadingEngine.getStars()) {

            id2star.put(1, star);

            String spectTypePrefix = star.spectType.substring(0, Math.min(prefixLength, star.spectType.length()));
            if (!spectTypeCount.containsKey(spectTypePrefix)) {
                spectTypeCount.put(spectTypePrefix, 1);
            } else {
                spectTypeCount.put(spectTypePrefix, spectTypeCount.get(spectTypePrefix) + 1);
            }
        }

        PrintWriter fout10 = new PrintWriter(new FileOutputStream("results/10.txt"));
        PrintWriter fout7 = new PrintWriter(new FileOutputStream("results/7.txt"));

        final Map<String, Integer> missSpectTypeCount = new HashMap<>();
        for (Star star : missStars) {
            CatalogueReadingEngine.Star primitiveStar = id2star.get(star.getId());
            fout10.println(primitiveStar.name.substring(4));

            String spectTypePrefix = primitiveStar.spectType.substring(0, Math.min(prefixLength, primitiveStar.spectType.length()));
            if (primitiveStar.spectType.contains("III")) {
                fout7.println(star.getId() + " " + spectTypePrefix + "III");
            } else if (primitiveStar.spectType.contains("V") && !primitiveStar.spectType.contains("IV")) {
                fout7.println(star.getId() + " " + spectTypePrefix + "V");
            }


            if (!missSpectTypeCount.containsKey(spectTypePrefix)) {
                missSpectTypeCount.put(spectTypePrefix, 1);
            } else {
                missSpectTypeCount.put(spectTypePrefix, missSpectTypeCount.get(spectTypePrefix) + 1);
            }
        }
        fout7.close();
        fout10.close();

        final List<String> spectTypes = new ArrayList<>(spectTypeCount.keySet());

        Collections.sort(spectTypes, new Comparator<String>() {
            private Integer getMissCount(String spectType) {
                if (missSpectTypeCount.containsKey(spectType)) {
                    return missSpectTypeCount.get(spectType);
                }
                return 0;
            }

            private Double getMissPart(String spectType) {
                return 1.0 * getMissCount(spectType) / spectTypeCount.get(spectType);
            }

            @Override
            public int compare(String spectType1, String spectType2) {
                Integer m1 = getMissCount(spectType1);
                Integer m2 = getMissCount(spectType2);
                Integer n1 = spectTypeCount.get(spectType1);
                Integer n2 = spectTypeCount.get(spectType2);
                if (m1 * n2 == m2 * n1) {
                    if (m1.equals(m2)) {
                        return n1.compareTo(n2);
                    }
                    return m2.compareTo(m1);
                }
                return getMissPart(spectType2).compareTo(getMissPart(spectType1));
            }
        });

        final List<String> mainSpectTypes = new ArrayList<>();

        int ym[] = new int[types.size() * 2];
        int yn[] = new int[types.size() * 2];


        for (String spectType : spectTypes) {
            int m = 0;
            if (missSpectTypeCount.containsKey(spectType)) {
                m = missSpectTypeCount.get(spectType);
            }
            int n = spectTypeCount.get(spectType);
            if (prefixLength == 2 && spectType.length() == 2) {
                if (types.contains(spectType.substring(0, 1)) && Character.isDigit(spectType.charAt(1))) {
                    mainSpectTypes.add(spectType);
                    ym[types.indexOf(spectType.substring(0, 1)) * 2 + (spectType.charAt(1) - '0') / 5] += m;
                    yn[types.indexOf(spectType.substring(0, 1)) * 2 + (spectType.charAt(1) - '0') / 5] += n;
                    System.out.println(spectType + " " + 100 * m / n + "% (" + m + " / " + n + ")");
                }
            }
        }

        double[][] xy = new double[types.size() * 2][3];
        double[] x = new double[types.size() * 2];
        double[] y = new double[types.size() * 2];
        for (int i = 0; i < y.length; ++i) {
            y[i] = 1.0 * ym[i] / yn[i];
            x[i] = i;
            xy[i][0] = i + 0.5;
            xy[i][1] = y[i];
            xy[i][2] = 1;
        }



        Plot2DPanel missSpectTypeHistogram = new Plot2DPanel();

        //missSpectTypeHistogram.addBarPlot("miss", y);
        missSpectTypeHistogram.addHistogramPlot("Выбросы", xy);

        missSpectTypeHistogram.getAxis(0).setLightLabelFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 0));
        missSpectTypeHistogram.getAxis(0).setLabelText("");

        missSpectTypeHistogram.getAxis(1).setLabelText("Доля выбросов");
        missSpectTypeHistogram.getAxis(1).setLabelFont(new Font(Font.DIALOG, Font.TYPE1_FONT, 20));
        missSpectTypeHistogram.getAxis(1).setLabelPosition(0, 1.1);
        missSpectTypeHistogram.getAxis(1).setLightLabelFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 15));

                      /*
        HashMap<String, Double> lightLabels = new HashMap();
        for (int i = 0; i < types.size(); ++i) {
            lightLabels.put(types.get(i) + "0-4", 2.0 * i);
            lightLabels.put(types.get(i) + "5-9", 2.0 * i + 1);
        }
        missSpectTypeHistogram.getAxis(0).setStringMap(lightLabels);
                                                                       */
        new FrameView(missSpectTypeHistogram);
    }

    private static List<Star> getNativeMissStars() throws FileNotFoundException {
        final List<Star> stars = DustDetectionEngine.getStars();
        final List<Star> missStars = new ArrayList<>();
        for (Star star : stars) {
            if (star.getExtinction().value + 2 * star.getExtinction().error < 0) {
                missStars.add(star);
            }
        }
        return missStars;
    }

    private static List<Star> getLeastSquaresMissStars() throws FileNotFoundException {
        return DustDetectionEngine.getDustDetector().getMissStars();
    }

    public static void main(String[] args) throws Exception {
        processSpectTypeHistogram(getNativeMissStars(), 2);
    }
}
