package ru.spbu.astro.dust;

import java.awt.geom.Point2D;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HipparcosReadingEngine {
    
    public static Map<String, List<Point2D.Double>> intrinsics = new HashMap();
    
    public static void getIntrinsics() {
        Scanner fin;
        try {
            fin = new Scanner(new FileInputStream("Datasets/intrinsic.txt"));
        } catch (Exception e) {
            return;
        }
        
        String[] titles = fin.nextLine().trim().split("\\s+");
        for (int i = 2; i < titles.length; ++i) {
            intrinsics.put(titles[i], new ArrayList<Point2D.Double>());
        }
        
        while (fin.hasNextLine()) {
            String[] fields = fin.nextLine().trim().split("\\s+");
            double code = Double.valueOf(fields[1]);
            for (int i = 2; i < titles.length; ++i) {
                if (!fields[i].equals("-")) {
                    double bv = Double.valueOf(fields[i]);
                    intrinsics.get(titles[i]).add(new Point2D.Double(code, bv));
                }
            }
        }
    }
    
    public static String name2name(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        
        return name.replaceAll(" ", "_");
    }

    public static Double hours2rad(String hours) {
        if (hours == null || hours.isEmpty()) {
            return null;
        }

        String[] ras = hours.split(" ");

        int h = Integer.valueOf(ras[0]);
        int m = Integer.valueOf(ras[1]);
        double s = Double.valueOf(ras[2]);

        return (h * 3600 + m * 60 + s) / 43200 * Math.PI;
    }
    
    public static Double deg2rad(String deg) {
        if (deg == null || deg.isEmpty()) {
            return null;
        }

        String[] degs = deg.split(" ");

        int d = Integer.valueOf(degs[0]);
        int m = Integer.valueOf(degs[1]);
        double s = Double.valueOf(degs[2]);

        if (degs[0].startsWith("-")) {
            m = -m;
            s = -s;
        }

        return (d * 3600 + m * 60 + s) / 324000 * Math.PI / 2;
    }

    public static Double I = 1.0973190197481;
    public static Double LEO = 4.9368292609653;
    public static Double L0 = 0.57477039907417;

    public static Double toGalaxyLongitude(double ra, double dec) {
        double l = Math.atan2(
                Math.sin(dec) * Math.sin(I) + Math.cos(dec) * Math.cos(I) * Math.sin(ra - LEO),
                Math.cos(dec) * Math.cos(ra - LEO)
        ) + L0;
        if (l < 0) {
            l += 2 * Math.PI;
        }
        return l;
    }

    public static Double toGalaxyLatitude(double ra, double dec) {
        return Math.asin(Math.sin(dec) * Math.cos(I) - Math.cos(dec) * Math.sin(I) * Math.sin(ra - LEO));
    }
    
    public static Double parallax2r(String parallax) {
        if (parallax == null || parallax.isEmpty()) {
            return null;
        }
        
        return 1000 / Double.valueOf(parallax);
    }
    
    public static double interpolate(Point2D.Double p1, Point2D.Double p2, double x) {
        return (p2.y - p1.y) / (p2.x - p1.x) * (x - p1.x) + p1.y;
    }

    public static Double spectType2bvInt(String spectType) {
        if (spectType == null || spectType.isEmpty()) {
            return null;
        }

        Matcher m = Pattern.compile("(([OBAFGKM][0-9](\\.5)?)(([IV]*)a?b?)?)(.*)").matcher(spectType);
        if (!m.matches()) {
            return null;
        }
        
        spectType = m.group(1);
        
        Map<String, Integer> start = new HashMap();
        
        start.put("O", -10);
        start.put("B", 0);
        start.put("A", 10);
        start.put("F", 20);
        start.put("G", 30);
        start.put("K", 40);
        start.put("M", 48);
        
        m = Pattern.compile("(?:[OBAFGKM])([0-9](\\.5)?)(.*)").matcher(spectType);
        if (!m.matches()) {
            return null;
        }

        String[] parts = spectType.split(m.group(1));

        String lumin;
        if (parts.length < 2 || !intrinsics.containsKey(parts[1])) {
            lumin = "V";
        } else {
            lumin = parts[1];
        }
        
        List<Point2D.Double> intrinsic = intrinsics.get(lumin);
        
        double code = start.get(parts[0]) + Double.valueOf(m.group(1));   
        for (int i = 0; i < intrinsic.size() - 1; ++i) {
            if (intrinsic.get(i).x <= code && code < intrinsic.get(i + 1).x) {
                //System.out.println(interpolate(intrinsic.get(i), intrinsic.get(i + 1), code) + "\n");
                
                return interpolate(intrinsic.get(i), intrinsic.get(i + 1), code);
            }
        }
        
        return null;
    }
    
    public static Double string2double(String bvCol) {
        if (bvCol == null || bvCol.isEmpty()) {
            return null;
        }
        
        return Double.valueOf(bvCol);
    }

    public static class Star {
        public String name;
        public String ra;
        public String raError;
        public String dec;
        public String decError;
        public String parallax;
        public String parallaxError;
        public String spectType;
        public String bvColor;
        public String bvColorError;

        public Star(
                String name,
                String ra, String raError,
                String dec, String decError,
                String parallax, String parallaxError,
                String spectType,
                String bvColor, String bvColorError
        ) {
            this.name = name;
            this.ra = ra;
            this.raError = raError;
            this.dec = dec;
            this.decError = decError;
            this.parallax = parallax;
            this.parallaxError = parallaxError;
            this.spectType = spectType;
            this.bvColor = bvColor;
            this.bvColorError = bvColorError;
        }

        @Override
        public String toString() {
            return "Star(" +
                    "name = '" + name + '\'' +
                    ", ra = '" + ra + '\'' +
                    ", raError = '" + raError + '\'' +
                    ", dec = '" + dec + '\'' +
                    ", decError = '" + decError + '\'' +
                    ", parallax = '" + parallax + '\'' +
                    ", parallaxError = '" + parallaxError + '\'' +
                    ", spectType = '" + spectType + '\'' +
                    ", bvColor = '" + bvColor + '\'' +
                    ", bvColorError = '" + bvColorError + '\'' +
                    ')';
        }
    }

    public static List<Star> getStars() {
        Scanner fin;
        try {
            fin = new Scanner(new FileInputStream("Datasets/hipparcos.txt"));

        } catch (Exception e) {
            return null;
        }

        List<Star> stars = new ArrayList();

        fin.nextLine();
        while (fin.hasNextLine()) {
            String[] fields = fin.nextLine().trim().split("\\|");
            String name = fields[1].trim();
            String ra = fields[2].trim();
            String dec = fields[3].trim();
            String parallax = fields[4].trim();
            String spectType = fields[5].trim();
            String raError = fields[6].trim();
            String decError = fields[7].trim();
            String parallaxError = fields[8].trim();
            String bvColor = fields[9].trim();
            String bvColorError = fields[10].trim();


            stars.add(new Star(
                    name,
                    ra, raError,
                    dec, decError,
                    parallax, parallaxError,
                    spectType,
                    bvColor, bvColorError
            ));
        }

        return stars;
    }
    
    public static void main(String[] args) {
        getIntrinsics();

        PrintWriter fout;
        try {
            fout = new PrintWriter(new FileOutputStream("Datasets/data.txt"));
        } catch (Exception e) {
            return;
        }

        for (Star star : getStars()) {
            String name = name2name(star.name);
            Double l = toGalaxyLongitude(hours2rad(star.ra), deg2rad(star.dec));
            Double b = toGalaxyLatitude(hours2rad(star.ra), deg2rad(star.dec));
            Double r = parallax2r(star.parallax);
            Double bvInt = spectType2bvInt(star.spectType);
            Double bvObs = string2double(star.bvColor);
            Double extError = string2double(star.bvColorError);

            if (name != null && l != null && b != null && r != null && bvInt != null && bvObs != null && extError != null) {
                if (!Double.isInfinite(r)) {
                    fout.printf("%s\t%f\t%f\t%f\t%f\t%f\n", name, l, b, r, bvObs - bvInt, extError);
                }
            }
            fout.flush();
        }

        fout.close();
    }
}
