package ru.spbu.astro.dust;

import java.awt.geom.Point2D;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HipparcosReaderEngine {
    
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
    
    public static Double bvCol2bvObs(String bvCol) {
        if (bvCol == null || bvCol.isEmpty()) {
            return null;
        }
        
        return Double.valueOf(bvCol);
    }
    
    public static void main(String[] args) {
        getIntrinsics();
        
        Scanner fin;
        PrintWriter fout;
        
        try {
            fin = new Scanner(new FileInputStream("/Datasets/hipparcos.txt"));
            fout = new PrintWriter(new FileOutputStream("/Datasets/data.txt"));
        } catch (Exception e) {
            return;
        }
        
        fin.nextLine();
        while (fin.hasNextLine()) {
            String[] fields = fin.nextLine().trim().split("\\|");
            String name = fields[1].trim();
            String ra = fields[2].trim();
            String dec = fields[3].trim();
            String parallax = fields[4].trim();
            String spectType = fields[5].trim();
            String bvColor = fields[6].trim();
            
            name = name2name(name);
            Double l = toGalaxyLongitude(hours2rad(ra), deg2rad(dec));
            Double b = toGalaxyLatitude(hours2rad(ra), deg2rad(dec));
            Double r = parallax2r(parallax);
            Double bvInt = spectType2bvInt(spectType);
            Double bvObs = bvCol2bvObs(bvColor);


            if (name != null && l != null && b != null && r != null && bvInt != null && bvObs != null) {
                if (!Double.isInfinite(r)) {
                    fout.printf("%s\t%f\t%f\t%f\t%f\n", name, l, b, r, bvObs - bvInt);
                }
            }
            fout.flush();
        }

        fout.close();
    }
}
